package io.github.codemumbler.cloakdb;

import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.sql.DataSource;

public class CloakDataBase {

	private final String jndiName;
	private final CloakJNDI jndi;

	public CloakDataBase(String jndiName) {
		this.jndiName = jndiName;
		this.jndi = new CloakJNDI();
		jndi.bind(jndiName, getDataSource());
	}

	public DataSource getDataSource() {
		EmbeddedDataSource dataSource = new EmbeddedDataSource();
		dataSource.setDataSourceName(jndiName);
		dataSource.setDatabaseName("memory:" + jndiName);
		dataSource.setCreateDatabase("create");
		return dataSource;
	}
}
