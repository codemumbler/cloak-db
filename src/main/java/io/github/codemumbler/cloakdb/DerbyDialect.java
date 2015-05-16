package io.github.codemumbler.cloakdb;

class DerbyDialect implements Dialect {

	@Override
	public String prepareSQL(String sql) {
		return sql;
	}
}
