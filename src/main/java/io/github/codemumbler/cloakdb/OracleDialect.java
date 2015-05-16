package io.github.codemumbler.cloakdb;

class OracleDialect implements Dialect {

	@Override
	public String prepareSQL(String sql) {
		sql = sql.replaceAll("(?i)N?VARCHAR2?", "VARCHAR");
		sql = sql.replaceAll("(?i)NUMBER\\([1-9]\\)", "INT");
		sql = sql.replaceAll("(?i)(ALTER\\s+TABLE\\s+[\\w_]+\\s+ADD)", "$1 COLUMN");
		return sql;
	}
}
