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
		sql = sql.replaceAll("(?i)NUMBER\\([1-9],[1-9]\\)", "DOUBLE");
		sql = sql.replaceAll("(?i)(ALTER\\s+TABLE\\s+[\\w_]+\\s+ADD)\\s+(?!.*CONSTRAINT)", "$1 COLUMN ");
		sql = sql.replaceAll("(?i)NOCYCLE", "NO CYCLE");
		sql = sql.replaceAll("(?i)\\s+(NOCACHE|NOORDER)", "");
		sql = sql.replaceAll("(?i)FOR\\s+EACH", "REFERENCING NEW AS newrow FOR EACH");
		sql = sql.replaceAll("(?i)\\s+:NEW", " newrow");
		sql = sql.replaceAll("(?i)ALTER\\s+TRIGGER\\s+[_A-Z]+\\s+ENABLE;", "");
		sql = sql.replaceAll("(?i)CREATE\\s+OR\\s+REPLACE", "CREATE");
		sql = sql.replaceAll("(?i)(CREATE\\s+TRIGGER[\\s\\w]*?IF.*?THEN\\s+)(SELECT\\s+(.*?).NEXTVAL\\s+INTO\\s+(.*?)\\s+FROM\\s+dual)", "$1SET $4 = NEXT VALUE FOR $3");
		sql = sql.replaceAll("(?i)\\s+ENABLE", "");
		sql = sql.replaceAll("(?i)(DECLARE\\s+[\\s0-9A-Z_\\(\\)]*?;)\\s+BEGIN([\\s\\S\\.\\|\\(\\)/\\-#\\$,A-Z0-9:='\";_]*?)END;(\\s+/)?",
				"create procedure temp1() MODIFIES SQL DATA\nBEGIN ATOMIC\n\t$1\n$2END;\nCALL temp1();\nDROP PROCEDURE temp1;\n");
		sql = sql.replaceAll("(?i)([A-Z_]*?)\\s+:=", "SET $1 =");
		sql = sql.replaceAll("(?i)\\s*BEGIN(?!.*ATOMIC)", " BEGIN ATOMIC");
		return sql;
	}

	@Override
	public void enableSyntax(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("SET DATABASE SQL SYNTAX ORA TRUE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disableSyntax(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.execute("SET DATABASE SQL SYNTAX ORA FALSE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CloakPrepareCall prepareCall(String sql) {
		CloakPrepareCall prepareCall = new CloakPrepareCall();
		if ( sql.trim().startsWith("BEGIN") ) {
			sql = prepareSQL("DECLARE x INT; " + sql);
			prepareCall.setPreparationSQL(sql.substring(0, sql.indexOf("CALL")));
			prepareCall.setCleanUpSQL(sql.substring(sql.indexOf("DROP")));
			sql = sql.replace(sql.substring(0, sql.indexOf("CALL")), "");
			prepareCall.setCallStatement(sql.replace(prepareCall.getCleanUpSQL(), ""));
		}
		return prepareCall;
	}
}
