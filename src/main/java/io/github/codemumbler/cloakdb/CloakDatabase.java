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
	private final Dialect dialect;
	private final File[] sqlFiles;

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
		this(jndiName, dialect, initializeSQL, new File[]{});
	}

	/**
	 * Initialize the in-memory database with a SQL file using HSQLDB syntax.
	 * @param jndiName the jndi db name.
	 * @param sqlFiles a SQL file to initialize the db.
	 */
	public CloakDatabase(String jndiName, File ... sqlFiles) {
		this(jndiName, HSQLDB, "", sqlFiles);
	}

	/**
	 * Initialize the in-memory database with a SQL file using a specific syntax/dialect.
	 * @param jndiName the jndi db name.
	 * @param dialect the SQL syntax to use.
	 * @param sqlFiles a SQL file to initialize the db.
	 */
	public CloakDatabase(String jndiName, int dialect, File ... sqlFiles) {
		this(jndiName, dialect, "", sqlFiles);
	}

	private CloakDatabase(String jndiName, int dialect, String initializationSQL, File ... sqlFiles) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		if (dialect == ORACLE)
			this.dialect = new OracleDialect();
		else
			this.dialect = new HSQLDBDialect();
		this.initializationSQL = initializationSQL;
		this.sqlFiles = sqlFiles;
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
		dialect.disableSyntax(getDataSource());
		dropDatabase();
		jndi.unbind(jndiName);
	}

	private void createSchema() {
		if ((sqlFiles == null || sqlFiles.length == 0) &&
				(initializationSQL == null || initializationSQL.isEmpty()))
			return;
		SchemaBuilder schemaBuilder = new SchemaBuilder(getDataSource(), dialect);
		if (sqlFiles != null && sqlFiles.length > 0)
			schemaBuilder.executeScript(this.sqlFiles);
		else
			schemaBuilder.executeScript(this.initializationSQL);
	}

	private void initializeDatabase() {
		if (jndi.lookup(this.jndiName) == null) {
			jndi.bind(jndiName, getDataSource());
			createSchema();
			dialect.enableSyntax(getDataSource());
		}
	}

	DataSource getDataSource() {
		CloakDataSource dataSource = (CloakDataSource) jndi.lookup(this.jndiName);
		if (dataSource == null) {
			dataSource = new CloakDataSource(new JDBCDataSource(), dialect);
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
		CloakDataSource dataSource = (CloakDataSource) getDataSource();
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute(DROP_SCHEMA);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
