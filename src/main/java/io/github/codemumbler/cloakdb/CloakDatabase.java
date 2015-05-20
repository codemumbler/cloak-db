package io.github.codemumbler.cloakdb;

import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * In-memory db and jndi management class.  Invoke to use in test classes or use @see CloakAbstractTestCase.
 */
public class CloakDatabase {

	public static final int HSQLDB = 0;
	public static final int ORACLE = 1;

	private static final String DROP_SCHEMA = "DROP SCHEMA PUBLIC CASCADE";

	private final String jndiName;
	private final CloakJNDI jndi;
	private final String initializationSQL;
	private final int dialect;
	private final File sqlFile;

	/**
	 * Initialize the in-memory database with no structure and using HSQLDB syntax.
	 * @param jndiName the jndi db name.
	 */
	public CloakDatabase(String jndiName) {
		this(jndiName, HSQLDB, "");
	}

	/**
	 * Initialize the in-memory database with a String of SQL statements using HSQLDB syntax.
	 * @param jndiName the jndi db name.
	 * @param initializeSQL the initialization SQL statements.
	 */
	public CloakDatabase(String jndiName, String initializeSQL) {
		this(jndiName, HSQLDB, initializeSQL);
	}

	/**
	 * Initialize the in-memory database with a String of SQL statements using a specific syntax.
	 * @param jndiName the jndi db name.
	 * @param dialect the database syntax/dialect to use.
	 * @param initializeSQL the initialization SQL statements.
	 */
	public CloakDatabase(String jndiName, int dialect, String initializeSQL) {
		this(jndiName, dialect, initializeSQL, null);
	}

	/**
	 * Initialize the in-memory database with a SQL file using HSQLDB syntax.
	 * @param jndiName the jndi db name.
	 * @param file a SQL file to initialize the db.
	 */
	public CloakDatabase(String jndiName, File file) {
		this(jndiName, HSQLDB, "", file);
	}

	/**
	 * Initialize the in-memory database with a SQL file using a specific syntax/dialect.
	 * @param jndiName the jndi db name.
	 * @param dialect the SQL syntax to use.
	 * @param sql a SQL file to initialize the db.
	 */
	public CloakDatabase(String jndiName, int dialect, File sql) {
		this(jndiName, dialect, "", sql);
	}

	private CloakDatabase(String jndiName, int dialect, String initializationSQL, File sqlFile) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		this.dialect = dialect;
		this.initializationSQL = initializationSQL;
		this.sqlFile = sqlFile;
		if (jndi.lookup(this.jndiName) != null)
			throw new CloakDBException("Database is already initialized. Please use destroy() first.");
		initializeDatabase();
	}

	/**
	 * Drops the schema and reinitialize the db with the originally constructed parameter.
	 */
	public void reset() {
		dropDatabase();
		initializeDatabase();
	}

	/**
	 * Drop the schema and unbind it from the jndi context.
	 */
	public void destroy() {
		dropDatabase();
		jndi.unbind(jndiName);
	}

	private void createSchema() {
		if (sqlFile == null && (initializationSQL == null || initializationSQL.isEmpty()))
			return;
		SchemaBuilder schemaBuilder;
		if (dialect == ORACLE)
			schemaBuilder = new SchemaBuilder(getDataSource(), new OracleDialect());
		else
			schemaBuilder = new SchemaBuilder(getDataSource());
		if (sqlFile != null)
			schemaBuilder.executeScript(this.sqlFile);
		else
			schemaBuilder.executeScript(this.initializationSQL);
	}

	private void initializeDatabase() {
		if (jndi.lookup(this.jndiName) == null) {
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

	private void dropDatabase() {
		jndi.unbind(jndiName);
		JDBCDataSource dataSource = (JDBCDataSource) getDataSource();
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute(DROP_SCHEMA);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
