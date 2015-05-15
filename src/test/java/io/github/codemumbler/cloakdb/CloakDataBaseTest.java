package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class CloakDataBaseTest {

	@Test
	public void initializeDBContext() throws NamingException {
		CloakDataBase dataBase = new CloakDataBase("jdbc/app_db");

		Context initialContext = new InitialContext();
		Context envContext = (Context) initialContext.lookup("java:/comp/env");
		Assert.assertNotNull(envContext.lookup("jdbc/app_db"));
	}

	@Test
	public void dbDataSource() {
		CloakDataBase dataBase = new CloakDataBase("jdbc/app_db");
		Assert.assertTrue(dataBase.getDataSource() instanceof DataSource);
//		Assert.assertEquals(dataBase.getDataSource(), envContext.lookUp("jdbc/app_db"));
	}
}
