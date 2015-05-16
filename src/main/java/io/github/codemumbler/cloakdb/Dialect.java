package io.github.codemumbler.cloakdb;

interface Dialect {

	String prepareSQL(String sql);
}
