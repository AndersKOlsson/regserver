package com.github.anderskolsson.regserver;

import java.util.Date;
import java.util.UUID;

import com.github.anderskolsson.regserver.datastore.DataStore;
import com.github.anderskolsson.regserver.datastore.datamodel.User;
import com.github.anderskolsson.regserver.exceptions.UserCreationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public abstract class DbTest {
	private String dbName = "";
	private DataStore store;

	void setDbName(final String dbName) {
		this.dbName = dbName;
	}

	String getDbName() {
		return this.dbName;
	}

	abstract public DataStore createDb(String dbName, int HashLength) throws Exception;

	@Before
	abstract public void setDb();

	@After
	abstract public void removeDB() throws Exception;

	@Test
	public void createAndGetUser() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String userName = "test";
		String passwordHash = "test123";
		this.store = createDb(this.dbName, passwordHash.length());
		store.createUser(userUuid, userName, passwordHash);
		User userNameUser = store.getUser(userName);
		assertEquals(userName, userNameUser.name);
		assertEquals(passwordHash, userNameUser.passwordHash);
		assertEquals(userUuid, userNameUser.uuid);

		User uuidUser = store.getUser(userUuid);
		assertEquals(userName, uuidUser.name);
		assertEquals(passwordHash, uuidUser.passwordHash);
		assertEquals(userUuid, uuidUser.uuid);
	}

	@Test
	public void createAndLogin() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String userName = "test";
		String passwordHash = "test123";
		this.store = createDb(this.dbName, passwordHash.length());
		User createdUser = store.createUser(userUuid, userName, passwordHash);

		store.registerLogin(createdUser);
	}

	@Test
	public void duplicateUserName() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String userName = "test";
		String passwordHash = "test123";
		this.store = createDb(this.dbName, passwordHash.length());
		store.createUser(userUuid, userName, passwordHash);
		try {
			store.createUser(UUID.randomUUID(), userName, passwordHash);
			fail("Duplicate user created");
		} catch (UserCreationException e) {
			// Ignore
		}
	}

	@Test
	public void duplicateUUID() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String userName = "test";
		String passwordHash = "test123";
		this.store = createDb(this.dbName, passwordHash.length());
		store.createUser(userUuid, userName, passwordHash);
		try {
			store.createUser(userUuid, userName + 2, passwordHash);
			fail("Duplicate user created");
		} catch (UserCreationException e) {
			// Ignore
		}
	}

	@Test
	public void countLogins() throws Exception {
		UUID userUuid = UUID.randomUUID();
		String userName = "test";
		String passwordHash = "test123";
		this.store = createDb(this.dbName, passwordHash.length());
		User createdUser = store.createUser(userUuid, userName, passwordHash);
		store.registerLogin(createdUser);
		Date[] log = store.getAccessLog(createdUser, 0);
		assertEquals(1, log.length);
		store.registerLogin(createdUser);
		Date[] log2 = store.getAccessLog(createdUser, 0);
		assertEquals(2, log2.length);
	}

	@Test
	public void getWrongUser() throws Exception {
		this.store = createDb(this.dbName, 1);
		User user = this.store.getUser("someone");
		assertNull(user);
		User user2 = this.store.getUser(UUID.randomUUID());
		assertNull(user2);
	}

	@Test
	public void getAccessLogForWrongUser() throws Exception {
		this.store = createDb(this.dbName, 1);
		User testUser = new User(UUID.randomUUID(), "test", "e");
		Date[] log = this.store.getAccessLog(testUser, 0);
		assertEquals(0, log.length);
	}

	@Test
	public void wrongHashLength() throws Exception {
		this.store = createDb(this.dbName, 1);
		try {
			this.store.createUser(UUID.randomUUID(), "test", "ee");
			fail("Wrong hash length ignored");
		}
		catch(UserCreationException e){
			// Ignore
		}
		
	}
}
