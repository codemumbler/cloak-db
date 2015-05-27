package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;
import java.io.File;
import java.io.LineNumberReader;
import java.io.StringReader;
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
		StringBuilder sql = null;
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(true);
			LineNumberReader lineReader = new LineNumberReader(new StringReader(sqlScript));
			String line;
			int closeableStatements = 0;
			while ((line = lineReader.readLine()) != null) {
				line = line.trim();
				if (sql == null)
					sql = new StringBuilder();
				if (line.isEmpty() || line.startsWith("--") || line.startsWith("/"))
					continue;
				else {
					sql.append(line).append("\n");
					if (line.contains("FOR ") && !line.contains("END;"))
						closeableStatements++;
					else if (line.contains("IF") && !line.contains("END IF;"))
						closeableStatements++;
					if (line.contains("END IF;"))
						closeableStatements--;
					else if (line.contains("END;"))
						closeableStatements--;
				}
				if (line.contains(DEFAULT_DELIMITER) && closeableStatements <= 0) {
					Statement statement = connection.createStatement();
					statement.execute(sql.toString());
					statement.close();
					sql = null;
					closeableStatements = 0;
				}

			}
		} catch (Exception e) {
			if (sql == null)
				sql = new StringBuilder();
			throw new CloakDBException("Failed to execute SQL statement: " + sql.toString() + " because of " + e.getMessage());
		}
	}

	boolean executeScript(File... sqlFiles) {
		try {
			if (sqlFiles == null)
				return false;
			for (File sqlFile : sqlFiles) {
				if (!sqlFile.isDirectory())
					executeScript(new Scanner(sqlFile).useDelimiter("\\Z").next());
				else {
					File[] files = sqlFile.listFiles();
					if (files == null)
						continue;
					for (File file : files) {
						executeScript(new Scanner(file).useDelimiter("\\Z").next());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
