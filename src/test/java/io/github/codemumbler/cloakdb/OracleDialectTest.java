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
		Assert.assertEquals("id INT", prepareSQL("id NUMBER(9)"));
	}

	@Test
	public void numberToDOUBLE() {
		Assert.assertEquals("id DOUBLE", prepareSQL("id NUMBER(9,2)"));
	}

	@Test
	public void VARCHAR2() {
		Assert.assertEquals("label VARCHAR(100)", prepareSQL("label VARCHAR2(100)"));
	}

	@Test
	public void NVARCHAR() {
		Assert.assertEquals("label VARCHAR(100)", prepareSQL("label nVARCHAR(100)"));
	}

	@Test
	public void alterColumn() {
		Assert.assertEquals("ALTER TABLE test_table ADD COLUMN id INT;",
				prepareSQL("ALTER TABLE test_table ADD id Number(5);"));
	}

	@Test
	public void noCycle() {
		Assert.assertEquals("CREATE SEQUENCE test_seq MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 264 NO CYCLE;",
				prepareSQL("CREATE SEQUENCE test_seq MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 264 NOCYCLE;"));
	}

	@Test
	public void noCacheNoOrder() {
		Assert.assertEquals("CREATE SEQUENCE test_seq MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 264;",
				prepareSQL("CREATE SEQUENCE test_seq MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 264 NOCACHE NOORDER;"));
	}

	@Test
	public void oracleTriggers() {
		Assert.assertEquals("CREATE TRIGGER test_trig\n" +
						"BEFORE INSERT ON test_table\n" +
						"REFERENCING NEW AS newrow FOR EACH ROW BEGIN ATOMIC\n" +
						"\tIF newrow.id IS NULL THEN\n" +
						"\t\tSET newrow.id = NEXT VALUE FOR test_seq;\n" +
						"\tEND IF;\n" +
						"END;\n/\n",
				prepareSQL("CREATE OR REPLACE TRIGGER test_trig\n" +
						"BEFORE INSERT ON test_table\n" +
						"FOR EACH ROW BEGIN\n" +
						"\tIF :NEW.id IS NULL THEN\n" +
						"\t\tSELECT test_seq.nextVal\n" +
						"\t\tINTO :NEW.id\n" +
						"\t\tFROM dual;\n" +
						"\tEND IF;\n" +
						"END;\n" +
						"/\n"));
	}

	@Test
	public void enableTrigger() {
		Assert.assertEquals("", prepareSQL("ALTER TRIGGER test_trig ENABLE;"));
	}

	@Test
	public void addConstraint() {
		Assert.assertEquals("ALTER TABLE CANCELLATIONS ADD CONSTRAINT test_pk PRIMARY KEY (id);",
				prepareSQL("ALTER TABLE CANCELLATIONS ADD CONSTRAINT test_pk PRIMARY KEY (id) ENABLE;"));
	}

	@Test
	public void plsqlBlock() {
		Assert.assertEquals("create procedure temp1() MODIFIES SQL DATA\nBEGIN ATOMIC\n" +
				"\tDECLARE\nstr VARCHAR(32767);\n\n" +
				"\tSET str = '';\n" +
				"UPDATE test_table SET test_column = str;\n" +
				"END;\nCALL temp1();\nDROP PROCEDURE temp1;\n",
				prepareSQL("DECLARE\n" +
				"str varchar2(32767);\n" +
				"BEGIN\n" +
				"\tstr := '';\n" +
				"UPDATE test_table SET test_column = str;\n" +
				"END;\n" +
				"/"));
	}

	private String prepareSQL(String sql) {
		return dialect.prepareSQL(sql);
	}
}
