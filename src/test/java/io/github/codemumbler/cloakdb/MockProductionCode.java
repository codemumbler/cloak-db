package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MockProductionCode {

	public DataSource getDataSource() throws NamingException {
		Context context = new InitialContext();
		Context envContext = (Context) context.lookup("java:/comp/env");
		return (DataSource) envContext.lookup("jdbc/simple_db");
	}

	public void initializeDatabase() throws Exception {
		SchemaBuilder builder = new SchemaBuilder(getDataSource());
		File dbMigrationDirectory = new File(getClass().getClassLoader().getResource("db/migration").toURI());
		builder.executeScript(dbMigrationDirectory);
	}

	public int runQuery() throws Exception {
		try (Connection connection = getDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			ResultSet set = statement.executeQuery("SELECT * FROM test_table");
			int count = 0;
			while ( set.next() )
				count++;
			set.close();
			return count;
		}
	}

	public void insertNewId() throws Exception {
		try (Connection connection = getDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			statement.executeUpdate("INSERT INTO test_table(id) VALUES (2);");
		}
	}

	public void createNewOracleTable() throws Exception {
		try (Connection connection = getDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE TEST_TABLE2 (" +
					" TEST_COLUMN_ID NUMBER(5) " +
					");");
		}
	}

	public void executeSimplePLSQL() throws Exception {
		createNewOracleTable();
		try (Connection connection = getDataSource().getConnection();
			 CallableStatement statement = connection.prepareCall("BEGIN\n" +
					 "INSERT INTO TEST_TABLE2(TEST_COLUMN_ID) VALUES (1);\n" +
					 "INSERT INTO TEST_TABLE2(TEST_COLUMN_ID) VALUES (2);\n" +
					 "INSERT INTO TEST_TABLE2(TEST_COLUMN_ID) VALUES (3);\n" +
					 "INSERT INTO TEST_TABLE2(TEST_COLUMN_ID) VALUES (4);\n" +
					 "INSERT INTO TEST_TABLE2(TEST_COLUMN_ID) VALUES (5);\n" +
					 "END;")) {
			statement.execute();
		}
	}

	public void createTrigger() throws Exception {
		try (Connection connection = getDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("CREATE OR REPLACE TRIGGER test_trig\n" +
					"\t\tBEFORE INSERT ON test_table2\n" +
					"\t\tFOR EACH ROW BEGIN\n" +
					"\t\tIF :NEW.TEST_COLUMN_ID IS NULL THEN\n" +
					"\t\tSELECT 1\n" +
					"\t\tINTO :NEW.TEST_COLUMN_ID\n" +
					"\t\tFROM dual;\n" +
					"\t\tEND IF;\n" +
					"\t\tEND;");
		}
	}
}
