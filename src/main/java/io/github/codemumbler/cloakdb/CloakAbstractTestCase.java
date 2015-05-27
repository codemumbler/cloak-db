package io.github.codemumbler.cloakdb;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Extend test classes with this class for automated jndi and in-memory database management.
 */
public abstract class CloakAbstractTestCase {

	private static CloakDatabase db;

	public CloakAbstractTestCase() {
		if (db != null) {
			db.destroy();
		}
		if (schemaFile() == null || schemaFile().length == 0)
			db = new CloakDatabase(jdbcName(), dialect(), schemaSQL());
		else
			db = new CloakDatabase(jdbcName(), dialect(), schemaFile());
	}

	/**
	 * The Dialect to initialize the database schema.
	 * @return
	 */
	protected int dialect() {
		return CloakDatabase.HSQLDB;
	}

	/**
	 * The SQL script String to initialize the database schema.
	 * @return
	 */
	protected String schemaSQL() {
		return "";
	}

	/**
	 * The SQL script File to initialize database schema.
	 * @return
	 */
	protected File[] schemaFile() {
		return null;
	}

	/**
	 * The "jdbc/db" name of the database in the context to lookup.  Replace db with the name of the database.
	 * @return
	 */
	protected abstract String jdbcName();

	/**
	 * Resets the in-memory database.
	 */
	public void reset() {
		db.reset();
	}
}
