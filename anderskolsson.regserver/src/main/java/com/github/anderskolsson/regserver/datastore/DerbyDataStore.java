package com.github.anderskolsson.regserver.datastore;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import com.github.anderskolsson.regserver.datastore.datamodel.User;
import com.github.anderskolsson.regserver.exceptions.DataStoreException;
import com.github.anderskolsson.regserver.exceptions.UserCreationException;
import com.github.anderskolsson.regserver.exceptions.UserLookupException;
import com.github.anderskolsson.regserver.rest.UserResource;
import com.github.anderskolsson.regserver.utils.UUIDUtils;

/**
 * Uses the <a href="https://db.apache.org/derby/">Apache Derby</a> database in embedded mode as backend
 *
 */
public class DerbyDataStore extends AbstractStore {
	private final Connection conn;
	private Logger logger;
	private static final String userTableName = "USERS";
	private static final String userAccessTableName = "USER_ACCESS";

	/**
	 * Initializes a new database, if it doesn't already exist
	 * @param dbName the name the Derby database will have
	 * @param hashLength the length required to store the password hash type as string
	 * @throws ClassNotFoundException thrown when the database jdbc driver cannot be found
	 * @throws InstantiationException thrown when the database jdbc driver cannot be instantiated
	 * @throws IllegalAccessException thrown when the database jdbc driver is not allowed to be instantiated
	 * @throws SQLException thrown when the initialization of the database tables fails
	 */
	public DerbyDataStore(final String dbName, final int hashLength) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		this.conn = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true");
		this.checkTables(hashLength);
		this.logger = Logger.getLogger(DerbyDataStore.class.getName());
	}

	private void checkTables(final int hashLength) throws SQLException {
		createTableIfNotExists(userTableName,
				String.format("CREATE TABLE " + userTableName + " (UUID CHAR(16) FOR BIT DATA not null primary key,"
						+ " USER_NAME VARCHAR(255) UNIQUE, " + "PASSWORD_HASH CHAR(%s))", hashLength));
		createTableIfNotExists(userAccessTableName, "CREATE TABLE " + userAccessTableName
				+ "(UUID CHAR(16) FOR BIT DATA NOT NULL, TIME TIMESTAMP NOT NULL, FOREIGN KEY (UUID) REFERENCES " + userTableName + "(UUID))");
		// TODO: Add index to UUID column
	}

	/*
	 * There's no "Create table if not exists" in Derby. Using this suggestion
	 * instead: <a href=
	 * "http://somesimplethings.blogspot.se/2010/03/derby-create-table-if-not-exists.html"/>
	 */
	private void createTableIfNotExists(final String tableName, final String sqlCreateStmt) throws SQLException {
		DatabaseMetaData meta = this.conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, tableName, null);
		if (!rs.next()) {
			Statement createStmt = conn.createStatement();
			createStmt.execute(sqlCreateStmt);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser(final UUID uuid) throws UserLookupException {
		try {
			PreparedStatement selectUserStmt = this.conn
					.prepareStatement("SELECT * FROM " + userTableName + " WHERE UUID=?");
			selectUserStmt.setBytes(1, UUIDUtils.asBytes(uuid));
			selectUserStmt.execute();
			ResultSet result = selectUserStmt.getResultSet();
			if (result.next()) {
				String userName = result.getString("USER_NAME");
				String hash = result.getString("PASSWORD_HASH");
				User user = new User(uuid, userName, hash);
				return user;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser(final String userName) throws UserLookupException {
		try {
			PreparedStatement selectUserStmt = this.conn
					.prepareStatement("SELECT * FROM " + userTableName + " WHERE USER_NAME=?");
			selectUserStmt.setString(1, userName);
			selectUserStmt.execute();
			ResultSet result = selectUserStmt.getResultSet();
			if (result.next()) {
				UUID uuid = UUIDUtils.asUuid(result.getBytes("UUID"));
				String hash = result.getString("PASSWORD_HASH");
				User user = new User(uuid, userName, hash);
				return user;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User createUser(final UUID uuid, final String userName, final String passwordHash)
			throws UserCreationException {
		logger.log(Level.INFO, "Creating DB user: "+userName);
		if (null == userName || userName.isEmpty()) {
			throw new UserCreationException("User name must be set");
		}
		try {
			PreparedStatement createUserStmt = this.conn.prepareStatement(
					"INSERT INTO " + userTableName + " (UUID, USER_NAME, PASSWORD_HASH) VALUES (?, ?, ?)");
			createUserStmt.setBytes(1, UUIDUtils.asBytes(uuid));
			createUserStmt.setString(2, userName);
			createUserStmt.setString(3, passwordHash);
			createUserStmt.execute();

			User user = new User(uuid, userName, passwordHash);
			return user;
		} catch (DerbySQLIntegrityConstraintViolationException ex){
			throw new UserCreationException("User already exists: " + userName, ex);
		}
		catch (SQLException ex) {
			throw new UserCreationException("Error while creating user: " + userName, ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date[] getAccessLog(final User user, final int maxNumRows) throws DataStoreException {
		if (!isValidUserObj(user)) {
			throw new UserLookupException("Wrong user/password format");
		}
		
		PreparedStatement loginStmt;
		try {
			loginStmt = this.conn.prepareStatement("SELECT TIME FROM " + userAccessTableName
					+" WHERE UUID=?"
					+" ORDER BY TIME DESC");
			loginStmt.setBytes(1, UUIDUtils.asBytes(user.uuid));
			loginStmt.setMaxRows(maxNumRows);
			loginStmt.execute();
			ResultSet result = loginStmt.getResultSet();
			List<Date> logins = new ArrayList<Date>();
			while (result.next()) {
				Timestamp time = result.getTimestamp("TIME");
				logins.add(time);
			}
			return logins.toArray(new Date[logins.size()]);
		} catch (SQLException e) {
			throw new DataStoreException("Failure while getting login", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerLogin(final User user) throws UserLookupException {
		if (!isValidUserObj(user)) {
			throw new UserLookupException("Wrong user/password format");
		}

		try {
			// We don't rely on generating time stamps in the DB, since UTC handling is inconsistent
			Timestamp time = Timestamp.from(Instant.now());
			PreparedStatement loginStmt = this.conn.prepareStatement("INSERT INTO " + userAccessTableName
					+ " (UUID, TIME) VALUES (?, ?)");
			loginStmt.setBytes(1, UUIDUtils.asBytes(user.uuid));
			loginStmt.setTimestamp(2, time);
			loginStmt.execute();
		} catch (SQLException e) {
			throw new UserLookupException("Failed", e);
		}
	}

}
