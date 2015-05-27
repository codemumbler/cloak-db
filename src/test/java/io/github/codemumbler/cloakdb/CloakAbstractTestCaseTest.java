package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CloakAbstractTestCaseTest {

	public static final String JDBC_SIMPLE_DB = "jdbc/simple_db";
	private MockProductionCode productionCode;

	@Before
	public void setUp() {
		productionCode = new MockProductionCode();
	}

	private DataSource getDataSource() throws NamingException {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		return (DataSource) env.lookup(JDBC_SIMPLE_DB);
	}

	@Test
	public void productionCodeAbleToGetDataSource() throws Exception {
		CloakAbstractTestCase cloakAbstractTestCase = new CloakAbstractTestCase();
		cloakAbstractTestCase.testDataSource();
	}

	@Test
	public void testCodeBuildsSchemaForProdCode() throws Exception {
		CloakAbstractTestCase cloakAbstractTestCase = new CloakAbstractTestCase();
		cloakAbstractTestCase.runQueryTest();
	}

	@Test
	public void productionCodeInsertionResetAfterTest() throws Exception {
		CloakAbstractTestCase cloakAbstractTestCase = new CloakAbstractTestCase();
		productionCode.insertNewId();
		cloakAbstractTestCase.tearDown();
		cloakAbstractTestCase.runQueryTest();
	}

	@Test
	public void useADifferentDialect() throws Exception {
		CloakDialectAbstractTestCase tests = new CloakDialectAbstractTestCase();
		tests.runDialectTest();
	}

	@Test
	public void executeTableCreateUsingOracle() throws Exception {
		CloakDialectCreationAbstractTestCase tests = new CloakDialectCreationAbstractTestCase();
		tests.runCreateTable();
	}

	@Test
	public void useAFileToBuildInitialSchema() throws Exception {
		CloakFileAbstractTestCase tests = new CloakFileAbstractTestCase();
		tests.runFileTest();
	}

	@Test
	public void useFilesToBuildInitialSchema() throws Exception {
		CloakMultiFileAbstractTestCase tests = new CloakMultiFileAbstractTestCase();
		tests.runMultiFileTest();
	}

	@Test
	public void nonInitializedSchema() throws Exception {
		PlainCloakAbstractTestCase tests = new PlainCloakAbstractTestCase();
		tests.runQueryTest();
	}

	private class CloakAbstractTestCase extends io.github.codemumbler.cloakdb.CloakAbstractTestCase {

		private static final String CREATE_SCHEMA =
				"CREATE TABLE test_table ( id INT NOT NULL );\nINSERT INTO test_table(id) VALUES (1);";

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}

		@Override
		protected String schemaSQL() {
			return CREATE_SCHEMA;
		}

		public void testDataSource() throws NamingException {
			Assert.assertNotNull(productionCode.getDataSource());
		}

		public void runQueryTest() throws Exception {
			Assert.assertEquals(1, productionCode.runQuery());
		}

		public void tearDown() {
			super.reset();
		}
	}

	private class PlainCloakAbstractTestCase extends io.github.codemumbler.cloakdb.CloakAbstractTestCase {

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}

		public void runQueryTest() throws Exception {
			productionCode.initializeDatabase();
			Assert.assertEquals(2, productionCode.runQuery());
		}
	}

	private class CloakDialectCreationAbstractTestCase extends io.github.codemumbler.cloakdb.CloakAbstractTestCase {

		public void runCreateTable() throws Exception {
			productionCode.createNewOracleTable();
			Assert.assertEquals(0, runQuery());
		}

		private int runQuery() throws Exception {
			try (Connection connection = getDataSource().getConnection();
				 Statement statement = connection.createStatement()) {
				ResultSet set = statement.executeQuery("SELECT COUNT(*) FROM TEST_TABLE2");
				int count = 0;
				while ( set.next() )
					count = set.getInt(1);
				set.close();
				return count;
			}
		}

		@Override
		protected int dialect() {
			return CloakDatabase.ORACLE;
		}

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}
	}

	private class CloakDialectAbstractTestCase extends CloakAbstractTestCase {

		private static final String CREATE_SCHEMA =
				"CREATE TABLE test_table ( id NUMBER(5) NOT NULL );\nINSERT INTO test_table(id) VALUES (1);";

		public void runDialectTest() throws Exception {
			Assert.assertEquals(1, productionCode.runQuery());
		}

		@Override
		protected String schemaSQL() {
			return CREATE_SCHEMA;
		}

		@Override
		protected int dialect() {
			return CloakDatabase.ORACLE;
		}

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}
	}

	private class CloakFileAbstractTestCase extends CloakAbstractTestCase {

		public void runFileTest() throws Exception {
			Assert.assertEquals(2, productionCode.runQuery());
		}

		@Override
		protected File[] schemaFile() {
			try {
				return new File[]{
						new File(getClass().getClassLoader().getResource("db/migration").toURI())
				};
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}
	}

	private class CloakMultiFileAbstractTestCase extends CloakAbstractTestCase {

		public void runMultiFileTest() throws Exception {
			Assert.assertEquals(2, productionCode.runQuery());
		}

		@Override
		protected File[] schemaFile() {
			try {
				return new File[]{
						new File(getClass().getClassLoader().getResource("db/migration/V1_1__create_table.sql").toURI()),
						new File(getClass().getClassLoader().getResource("db/migration/V1_2__insert_data.sql").toURI())
				};
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected String jdbcName() {
			return JDBC_SIMPLE_DB;
		}
	}
}