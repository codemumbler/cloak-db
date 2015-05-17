package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;
import java.io.File;
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

	boolean executeScript(File ... sqlFiles) {
		try {
			if ( sqlFiles == null )
				return false;
			for ( File sqlFile : sqlFiles ) {
				if (!sqlFile.isDirectory())
					executeScript(new Scanner(sqlFile).useDelimiter("\\Z").next());
				else {
					for (File file : sqlFile.listFiles()) {
						executeScript(new Scanner(file).useDelimiter("\\Z").next());
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
}
