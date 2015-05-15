package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

public class CloakedJNDITest {

	public static final String DATABASE_JNDI_NAME = "jdbc/dbName";
	private CloakedJNDI jndi;

	@Before
	public void setUp() {
		jndi = new CloakedJNDI();
	}

	@Test
	public void createInitialContext() throws NamingException {
		try {
			new InitialContext();
		} catch ( NoInitialContextException e ) {
			Assert.fail("ContextFactory is not set up correctly: " + e.getMessage());
		}
	}

	@Test
	public void setEnvContext() throws NamingException {
		InitialContext context = new InitialContext();
		Assert.assertNotNull(context.lookup("java:/comp/env"));
	}

	@Test
	public void lookupInEnvContext() throws NamingException {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		final Object expected = new Object();
		env.bind(DATABASE_JNDI_NAME, expected);
		Assert.assertEquals(expected, jndi.lookup(DATABASE_JNDI_NAME));
	}

	@Test
	public void bindObject() throws NamingException {
		final Object expected = new Object();
		jndi.bind("jdbc/dbName", expected);
		Assert.assertEquals(expected, jndi.lookup("jdbc/dbName"));
	}
}
