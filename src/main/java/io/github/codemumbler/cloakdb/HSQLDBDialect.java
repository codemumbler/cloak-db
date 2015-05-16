package io.github.codemumbler.cloakdb;

class HSQLDBDialect implements Dialect {

	@Override
	public String prepareSQL(String sql) {
		return sql;
	}
}
