package io.github.codemumbler.cloakdb;

import org.junit.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;

public class CloakDatabaseTest {

	private static final String JDBC_APP_DB = "jdbc/app_db";
	private static final String CREATE_TABLE = "CREATE TABLE test_table ( id INT NOT NULL )";
	private static final String SIMPLE_DB_SCHEMA = CREATE_TABLE + ";\nINSERT INTO test_table(id) VALUES (1);";
	private static CloakDatabase database;

	@BeforeClass
	public static void setUpClass() {
		database = new CloakDatabase(JDBC_APP_DB);
	}

	@After
	public void tearDown() {
		database.reset();
	}

	@Test
	public void initializeDBContext() throws Exception {
		Assert.assertNotNull(lookupDataSource());
	}

	@Test
	public void canLookupDataSource() throws Exception {
		Assert.assertEquals(database.getDataSource(), lookupDataSource());
	}

	@Test(expected = SQLSyntaxErrorException.class)
	public void resetDatabaseAfterTest() throws Exception {
		reinitializeDB(JDBC_APP_DB, "");
		addTable();
		database.reset();
		queryTable();
		Assert.fail("Should have thrown an exception for a missing table");
	}

	@Test
	public void afterResetCanUseDataSource() throws Exception {
		reinitializeDB(JDBC_APP_DB, "");
		database.reset();
		Assert.assertTrue(addTable());
	}

	@Test
	public void initializeDatabaseWithSingleSQLStatement() throws Exception {
		reinitializeDB(JDBC_APP_DB, CREATE_TABLE);
		Assert.assertEquals(0, queryTable());
	}

	@Test
	public void initializeDatabaseWithMultipleSQL() throws Exception {
		reinitializeDB(JDBC_APP_DB, SIMPLE_DB_SCHEMA);
		Assert.assertEquals(1, queryTable());
	}

	@Test(expected = CloakDBException.class)
	public void newInstanceWithoutDestroyPrevious() throws Exception {
		database = new CloakDatabase(JDBC_APP_DB, SIMPLE_DB_SCHEMA.replaceAll("test_table", "test_table2"));
		try (Connection connection = lookupDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			ResultSet set = statement.executeQuery("SELECT * FROM test_table2");
			set.close();
		}
	}

	@Test
	public void destroyDatabase() throws Exception {
		database.destroy();
		Assert.assertNull(lookupDataSource());
	}

	@Test
	public void resetRestoresToOriginalSchema() throws Exception {
		reinitializeDB(JDBC_APP_DB, SIMPLE_DB_SCHEMA);
		database.reset();
		Assert.assertEquals(1, queryTable());
	}

	@Test
	public void useOracleSQL() throws Exception {
		database.destroy();
		database = new CloakDatabase(JDBC_APP_DB, "CREATE TABLE test_table ( id NUMBER(5) NOT NULL );\n" +
				"INSERT INTO test_table(id) VALUES (1);", CloakDatabase.ORACLE);
		Assert.assertEquals(1, queryTable());
	}

	private void reinitializeDB(String name, String SQL) {
		database.destroy();
		database = new CloakDatabase(name, SQL);
	}

	private DataSource lookupDataSource() throws Exception {
		Context initialContext = new InitialContext();
		Context envContext = (Context) initialContext.lookup("java:/comp/env");
		return (DataSource) envContext.lookup(JDBC_APP_DB);
	}

	private int queryTable() throws Exception {
		try (Connection connection = lookupDataSource().getConnection();
			Statement statement = connection.createStatement()) {
			ResultSet set = statement.executeQuery("SELECT * FROM test_table");
			int count = 0;
			while ( set.next() )
				count++;
			set.close();
			return count;
		}
	}

	private boolean addTable() throws Exception {
		try (Connection connection = lookupDataSource().getConnection();
			Statement statement = connection.createStatement()) {
			statement.executeUpdate(CREATE_TABLE);
		}
		return true;
	}
}
