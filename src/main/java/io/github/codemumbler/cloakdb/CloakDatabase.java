package io.github.codemumbler.cloakdb;

import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CloakDatabase {

	public static final int HSQLDB = 0;
	public static final int ORACLE = 1;
	private static final String DROP_SCHEMA = "DROP SCHEMA PUBLIC CASCADE";

	private final String jndiName;
	private final CloakJNDI jndi;
	private final String initializationSQL;
	private final int dialect;
	private final File sqlFile;

	public CloakDatabase(String jndiName) {
		this(jndiName, HSQLDB, "");
	}

	public CloakDatabase(String jndiName, String initializeSQL) {
		this(jndiName, HSQLDB, initializeSQL);
	}

	public CloakDatabase(String jndiName, int dialect, String initializeSQL) {
		this(jndiName, dialect, initializeSQL, null);
	}

	public CloakDatabase(String jndiName, File file) {
		this(jndiName, HSQLDB, "", file);
	}

	private CloakDatabase(String jndiName, int dialect, String initializationSQL, File sqlFile) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		this.dialect = dialect;
		this.initializationSQL = initializationSQL;
		this.sqlFile = sqlFile;
		if ( jndi.lookup(this.jndiName) != null )
			throw new CloakDBException("Database is already initialized. Please use destroy() first.");
		initializeDatabase();
	}

	public CloakDatabase(String jndiName, int dialect, File sql) {
		this(jndiName, dialect, "", sql);
	}

	private void createSchema() {
		if ( sqlFile == null && (initializationSQL == null || initializationSQL.isEmpty()) )
			return;
		SchemaBuilder schemaBuilder;
		if ( dialect == ORACLE )
			schemaBuilder = new SchemaBuilder(getDataSource(), new OracleDialect());
		else
			schemaBuilder = new SchemaBuilder(getDataSource());
		if ( sqlFile != null )
			schemaBuilder.executeScript(this.sqlFile);
		else
			schemaBuilder.executeScript(this.initializationSQL);
	}

	private void initializeDatabase() {
		if ( jndi.lookup(this.jndiName) == null ) {
			jndi.bind(jndiName, getDataSource());
			createSchema();
		}
	}

	DataSource getDataSource() {
		JDBCDataSource dataSource = (JDBCDataSource) jndi.lookup(this.jndiName);
		if (dataSource == null) {
			dataSource = new JDBCDataSource();
			dataSource.setDatabaseName("jdbc:hsqldb:mem:" + jndiName);
			try {
				dataSource.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dataSource;
	}

	public void reset() {
		dropDatabase();
		initializeDatabase();
	}

	private void dropDatabase() {
		jndi.unbind(jndiName);
		JDBCDataSource dataSource = (JDBCDataSource) getDataSource();
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()){
			statement.execute(DROP_SCHEMA);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		dropDatabase();
		jndi.unbind(jndiName);
	}
}
