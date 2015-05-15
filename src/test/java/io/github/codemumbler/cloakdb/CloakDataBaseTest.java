package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;

public class CloakDataBaseTest {

	private CloakDataBase dataBase;

	@Before
	public void setUp() {
		dataBase = new CloakDataBase("jdbc/app_db");
	}

	@Test
	public void initializeDBContext() throws Exception {
		Assert.assertNotNull(lookupDataSource());
	}

	@Test
	public void dbDataSource() {
		Assert.assertTrue(dataBase.getDataSource() instanceof DataSource);
	}

	@Test
	public void canLookupDataSource() throws Exception {
		Assert.assertEquals(dataBase.getDataSource(), lookupDataSource());
	}

	@Test(expected = SQLSyntaxErrorException.class)
	public void resetDatabaseAfterTest() throws Exception {
		addTable();
		dataBase.reset();
		queryTable();
		Assert.fail("Should have thrown an exception for a missing table");
	}

	@Test
	public void afterResetCanUseDataSource() throws Exception {
		dataBase.reset();
		Assert.assertTrue(addTable());
	}

	private DataSource lookupDataSource() throws Exception {
		Context initialContext = new InitialContext();
		Context envContext = (Context) initialContext.lookup("java:/comp/env");
		return (DataSource) envContext.lookup("jdbc/app_db");
	}

	private void queryTable() throws Exception {
		try (Connection connection = lookupDataSource().getConnection();
			Statement statement = connection.createStatement()) {
			statement.executeQuery("SELECT * FROM test_table");
		} catch (SQLSyntaxErrorException e){
			throw e;
		}
	}

	private boolean addTable() throws Exception {
		try (Connection connection = lookupDataSource().getConnection();
			Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE test_table ( id INT NOT NULL )");
		} catch (Exception e) {
			throw e;
		}
		return true;
	}
}
