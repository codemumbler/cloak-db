package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class CloakDataBaseTest {

	private CloakDataBase dataBase;

	@Before
	public void setUp() {
		dataBase = new CloakDataBase("jdbc/app_db");
	}

	@Test
	public void initializeDBContext() throws NamingException {
		Context initialContext = new InitialContext();
		Context envContext = (Context) initialContext.lookup("java:/comp/env");
		Assert.assertNotNull(envContext.lookup("jdbc/app_db"));
	}

	@Test
	public void dbDataSource() {
		Assert.assertTrue(dataBase.getDataSource() instanceof DataSource);
	}

	@Test
	public void canLookupDataSource() throws NamingException {
		Context initialContext = new InitialContext();
		Context envContext = (Context) initialContext.lookup("java:/comp/env");
		Assert.assertEquals(dataBase.getDataSource(), envContext.lookup("jdbc/app_db"));
	}
}
