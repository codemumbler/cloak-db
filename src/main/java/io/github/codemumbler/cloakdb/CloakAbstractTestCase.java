package io.github.codemumbler.cloakdb;

import java.io.File;

public abstract class CloakAbstractTestCase {

	private static CloakDatabase db;

	CloakAbstractTestCase() {
		if (db != null) {
			db.destroy();
		}
		if (schemaFile() == null)
			db = new CloakDatabase(jdbcName(), dialect(), schemaSQL());
		else
			db = new CloakDatabase(jdbcName(), dialect(), schemaFile());
	}

	protected int dialect() {
		return CloakDatabase.HSQLDB;
	}

	protected String schemaSQL() {
		return "";
	}

	protected File schemaFile() {
		return null;
	}

	protected abstract String jdbcName();

	public void reset() {
		db.reset();
	}
}
