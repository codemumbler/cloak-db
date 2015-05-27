package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;

class HSQLDBDialect implements Dialect {

	@Override
	public String prepareSQL(String sql) {
		return sql;
	}

	@Override
	public void enableSyntax(DataSource dataSource) {

	}

	@Override
	public void disableSyntax(DataSource dataSource) {

	}
}
