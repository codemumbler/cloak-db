package io.github.codemumbler.cloakdb;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class OracleDialect implements Dialect {

	@Override
	public String prepareSQL(String sql) {
		sql = sql.replaceAll("(?i)N?VARCHAR2?", "VARCHAR");
		sql = sql.replaceAll("(?i)NUMBER\\([1-9]\\)", "INT");
		sql = sql.replaceAll("(?i)(ALTER\\s+TABLE\\s+[\\w_]+\\s+ADD)\\s+(?!.*CONSTRAINT)", "$1 COLUMN ");
		sql = sql.replaceAll("(?i)NOCYCLE", "NO CYCLE");
		sql = sql.replaceAll("(?i)\\s+(NOCACHE|NOORDER)", "");
		sql = sql.replaceAll("(?i)FOR\\s+EACH", "REFERENCING NEW AS newrow FOR EACH");
		sql = sql.replaceAll("(?i)\\s+BEGIN", " BEGIN ATOMIC");
		sql = sql.replaceAll("(?i)\\s+:NEW", " newrow");
		sql = sql.replaceAll("(?i)ALTER TRIGGER [_A-Z]+ ENABLE;", "");
		sql = sql.replaceAll("(?i)\\s+ENABLE", "");
		return sql;
	}

	@Override
	public void enableSyntax(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection();
			Statement statement = connection.createStatement()){
			statement.execute("SET DATABASE SQL SYNTAX ORA TRUE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
