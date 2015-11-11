package com.github.anderskolsson.regserver;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import com.github.anderskolsson.regserver.datastore.DataStore;
import com.github.anderskolsson.regserver.datastore.DerbyDataStore;

public class DerbyDbTest extends DbTest {
	
	@Before
    public void setDb() {
    	setDbName(UUID.randomUUID().toString());
    }
	
	@After
	public void removeDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		try {
			DriverManager.getConnection("jdbc:derby:" + getDbName() + ";shutdown=true");
		}
		catch(SQLException e){
			// Ignore
		}
		File dbDir = new File(getDbName());
		if(dbDir.exists()) {
			FileUtils.deleteDirectory(dbDir);
		}
	}

	@Override
	public DataStore createDb(String dbName, int hashLength) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		return new DerbyDataStore(dbName, hashLength);
	}
}
