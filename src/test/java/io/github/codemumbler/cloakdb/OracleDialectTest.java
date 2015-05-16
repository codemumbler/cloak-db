package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OracleDialectTest {

	private OracleDialect dialect;

	@Before
	public void setUp() {
		dialect = new OracleDialect();
	}

	@Test
	public void numberToINT() {
		Assert.assertEquals("id INT", dialect.prepareSQL("id NUMBER(9)"));
	}

	@Test
	public void VARCHAR2() {
		Assert.assertEquals("label VARCHAR(100)", dialect.prepareSQL("label VARCHAR2(100)"));
	}

	@Test
	public void NVARCHAR() {
		Assert.assertEquals("label VARCHAR(100)", dialect.prepareSQL("label nVARCHAR(100)"));
	}

	@Test
	public void alterColumn() {
		Assert.assertEquals("ALTER TABLE test_table ADD COLUMN id INT;",
				dialect.prepareSQL("ALTER TABLE test_table ADD id Number(5);"));
	}
}