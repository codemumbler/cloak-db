package io.github.codemumbler.cloakdb;

import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CloakDataBase {

	public static final int HSQLDB = 0;
	public static final int ORACLE = 1;

	private final String jndiName;
	private final CloakJNDI jndi;
	private final String initializationSQL;
	private final int dialect;

	public CloakDataBase(String jndiName) {
		this(jndiName, "", HSQLDB);
	}

	public CloakDataBase(String jndiName, String initializeSQL) {
		this(jndiName, initializeSQL, HSQLDB);
	}

	public CloakDataBase(String jndiName, String initializeSQL, int dialect) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		this.dialect = dialect;
		this.initializationSQL = initializeSQL;
		if ( jndi.lookup(this.jndiName) != null )
			throw new CloakDBException("Database is already initialized. Please use destroy() first.");
		initializeDatabase();
	}

	private void createSchema() {
		SchemaBuilder schemaBuilder;
		if ( dialect == ORACLE )
			schemaBuilder = new SchemaBuilder(getDataSource(), new OracleDialect());
		else
			schemaBuilder = new SchemaBuilder(getDataSource());
		schemaBuilder.executeScript(this.initializationSQL);
	}

	private void initializeDatabase() {
		if ( jndi.lookup(this.jndiName) == null ) {
			jndi.bind(jndiName, getDataSource());
			if (!initializationSQL.isEmpty())
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
			statement.execute("DROP SCHEMA PUBLIC CASCADE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		dropDatabase();
		jndi.unbind(jndiName);
	}
}
