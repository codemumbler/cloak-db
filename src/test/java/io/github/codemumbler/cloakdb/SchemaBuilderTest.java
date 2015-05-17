package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class SchemaBuilderTest {

	private static final String JDBC_APP_DB = "jdbc/script_db";
	private static final String CREATE_TABLE = "CREATE TABLE test_table ( id INT NOT NULL )";
	private static final String SIMPLE_DB_SCHEMA = CREATE_TABLE + ";\nINSERT INTO test_table(id) VALUES (1);";

	private static CloakDatabase db;
	private SchemaBuilder schemaBuilder;

	@BeforeClass
	public static void setUpClass() {
		db = new CloakDatabase(JDBC_APP_DB);
	}

	@Before
	public void setUp() {
		db.reset();
		schemaBuilder = new SchemaBuilder(db.getDataSource());
	}

	@Test
	public void createTable() throws Exception {
		schemaBuilder.executeScript(CREATE_TABLE);
		Assert.assertEquals(0, queryTable(db.getDataSource()));
	}

	@Test
	public void multipleStatements() throws Exception {
		schemaBuilder.executeScript(SIMPLE_DB_SCHEMA);
		Assert.assertEquals(1, queryTable(db.getDataSource()));
	}

	@Test
	public void runOracleSQL() throws Exception {
		schemaBuilder = new SchemaBuilder(db.getDataSource(), new OracleDialect());
		schemaBuilder.executeScript("CREATE TABLE test_table ( id NUMBER(5) NOT NULL );");
		Assert.assertEquals(0, queryTable(db.getDataSource()));
	}

	@Test
	public void runCompleteDirectory() throws Exception {
		schemaBuilder.executeScript(new File(getClass().getClassLoader().getResource("db/migration").toURI()));
		Assert.assertEquals(2, queryTable(db.getDataSource()));
	}

	@Test
	public void runMultipleFiles() throws Exception {
		schemaBuilder.executeScript(new File(getClass().getClassLoader().getResource("db/migration/V1_1__create_table.sql").toURI()),
				new File(getClass().getClassLoader().getResource("db/migration/V1_2__insert_data.sql").toURI()));
		Assert.assertEquals(2, queryTable(db.getDataSource()));
	}

	@Test(expected = CloakDBException.class)
	public void badSQLStatement() {
		schemaBuilder.executeScript("CREATE UNKNOWN object;");
	}

	@Test
	public void nullFile() {
		Assert.assertFalse(schemaBuilder.executeScript((File[]) null));
	}

	@Test
	public void ignoreComments() throws Exception {
		schemaBuilder.executeScript(SIMPLE_DB_SCHEMA.replace("INSERT", "--INSERT"));
		Assert.assertEquals(0, queryTable(db.getDataSource()));
	}

	@Test
	public void ignoreEmptyLines() throws Exception {
		schemaBuilder.executeScript(SIMPLE_DB_SCHEMA + "\n");
		Assert.assertEquals(1, queryTable(db.getDataSource()));
	}

	private int queryTable(DataSource dataSource) throws Exception {
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			ResultSet set = statement.executeQuery("SELECT * FROM test_table");
			int count = 0;
			while ( set.next() )
				count++;
			set.close();
			return count;
		}
	}
}