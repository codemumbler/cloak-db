package io.github.codemumbler.cloakdb;

import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class CloakDataBase {

	private final String jndiName;
	private final CloakJNDI jndi;
	private final String initialzationSQL;

	public CloakDataBase(String jndiName) {
		this(jndiName, "");
	}

	public CloakDataBase(String jndiName, String initializeSQL) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		this.initialzationSQL = initializeSQL;
		if ( jndi.lookup(this.jndiName) != null )
			throw new CloakDBException("Database is already initialized. Please use destroy() first.");
		initializeDatabase();
	}

	private void createSchema() {
		try (Connection connection = getDataSource().getConnection();
			 Statement statement = connection.createStatement()) {
			connection.setAutoCommit(true);
			String[] sqlStatements = this.initialzationSQL.split(";");
			for ( String sql : sqlStatements ) {
				statement.execute(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeDatabase() {
		if ( jndi.lookup(this.jndiName) == null ) {
			jndi.bind(jndiName, getDataSource());
			if (!initialzationSQL.isEmpty())
				createSchema();
		}
	}

	DataSource getDataSource() {
		EmbeddedDataSource dataSource = (EmbeddedDataSource) jndi.lookup(this.jndiName);
		if (dataSource == null) {
			dataSource = new EmbeddedDataSource();
			dataSource.setDataSourceName(jndiName);
			dataSource.setDatabaseName("memory:" + jndiName);
			dataSource.setCreateDatabase("create");
		}
		return dataSource;
	}

	public void reset() {
		dropDatabase();
		initializeDatabase();
	}

	private void dropDatabase() {
		jndi.unbind(jndiName);
		EmbeddedDataSource dataSource = (EmbeddedDataSource) getDataSource();
		dataSource.setCreateDatabase(null);
		dataSource.setConnectionAttributes("drop=true");
		forceDatabaseReset(dataSource);
	}

	private void forceDatabaseReset(EmbeddedDataSource dataSource) {
		try {
			dataSource.getConnection();
		} catch (Exception ignored) {
		}
	}

	public void destroy() {
		dropDatabase();
		jndi.unbind(jndiName);
	}
}
