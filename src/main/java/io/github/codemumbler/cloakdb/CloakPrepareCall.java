package io.github.codemumbler.cloakdb;

class CloakPrepareCall {
	private String preparationSQL;
	private String cleanUpSQL;
	private String callStatement;

	public boolean hasPreparations() {
		return (this.preparationSQL != null && !this.preparationSQL.isEmpty());
	}

	public String preparationsSQL() {
		return this.preparationSQL;
	}

	public String callStatement() {
		return this.callStatement;
	}

	public void setPreparationSQL(String preparationSQL) {
		this.preparationSQL = preparationSQL;
	}

	public void setCleanUpSQL(String cleanUpSQL) {
		this.cleanUpSQL = cleanUpSQL;
	}

	public String getCleanUpSQL() {
		return cleanUpSQL;
	}

	public void setCallStatement(String callStatement) {
		this.callStatement = callStatement;
	}

	public boolean hasCleanUp() {
		return (this.cleanUpSQL != null && !this.cleanUpSQL.isEmpty());
	}
}
