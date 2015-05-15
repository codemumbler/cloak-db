package io.github.codemumbler.cloakdb;

import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.sql.DataSource;

public class CloakDataBase {

	private final String jndiName;
	private final CloakJNDI jndi;

	public CloakDataBase(String jndiName) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		initializeDatabase();
	}

	private void initializeDatabase() {
		jndi.bind(jndiName, getDataSource());
	}

	public DataSource getDataSource() {
		EmbeddedDataSource dataSource = new EmbeddedDataSource();
		dataSource.setDataSourceName(jndiName);
		dataSource.setDatabaseName("memory:" + jndiName);
		dataSource.setCreateDatabase("create");
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
}
