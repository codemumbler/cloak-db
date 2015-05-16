package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

class SchemaBuilder {

	private static final String DEFAULT_DELIMITER = ";";

	private final DataSource dataSource;
	private final Dialect dialect;

	SchemaBuilder(DataSource dataSource) {
		this(dataSource, new HSQLDBDialect());
	}

	SchemaBuilder(DataSource dataSource, Dialect dialect) {
		this.dataSource = dataSource;
		this.dialect = dialect;
	}

	void executeScript(String sqlScript) {
		sqlScript = dialect.prepareSQL(sqlScript);
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			connection.setAutoCommit(true);
			String[] sqlStatements = sqlScript.split(DEFAULT_DELIMITER);
			for ( String sql : sqlStatements ) {
				statement.execute(sql);
			}
		} catch (Exception e) {
			throw new CloakDBException("Failed to execute SQL statement: " + e.getMessage());
		}
	}

	void executeScript(File sqlFile) {
		try {
			executeScript(new Scanner(sqlFile).useDelimiter("\\Z").next());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
