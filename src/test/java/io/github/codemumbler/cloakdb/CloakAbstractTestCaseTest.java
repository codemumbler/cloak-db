package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.File;
import java.net.URISyntaxException;

public class CloakAbstractTestCaseTest {

	private MockProductionCode productionCode;

	@Before
	public void setUp() {
		productionCode = new MockProductionCode();
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
	public void useAFileToBuildInitialSchema() throws Exception {
		CloakFileAbstractTestCase tests = new CloakFileAbstractTestCase();
		tests.runFileTest();
	}

	private class CloakAbstractTestCase extends io.github.codemumbler.cloakdb.CloakAbstractTestCase {

		private static final String CREATE_SCHEMA =
				"CREATE TABLE test_table ( id INT NOT NULL );\nINSERT INTO test_table(id) VALUES (1);";

		@Override
		protected String jdbcName() {
			return "jdbc/simple_db";
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
			return "jdbc/simple_db";
		}
	}

	private class CloakFileAbstractTestCase extends CloakAbstractTestCase {

		public void runFileTest() throws Exception {
			Assert.assertEquals(2, productionCode.runQuery());
		}

		@Override
		protected File schemaFile() {
			try {
				return new File(getClass().getClassLoader().getResource("db/migration").toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected String jdbcName() {
			return "jdbc/simple_db";
		}
	}
}