package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;

interface Dialect {

	String prepareSQL(String sql);

	void enableSyntax(DataSource dataSource);
}
