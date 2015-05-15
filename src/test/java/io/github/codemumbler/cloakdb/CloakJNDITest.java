package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CloakJNDITest {

	public static final String DATABASE_JNDI_NAME = "jdbc/dbName";
	private CloakJNDI jndi;

	@Before
	public void setUp() {
		jndi = new CloakJNDI();
	}

	@Test
	public void setInitialContextFactory() throws NamingException {
		Assert.assertEquals(CloakContextFactory.class.getCanonicalName(), System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
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
		jndi.bind(DATABASE_JNDI_NAME, expected);
		Assert.assertEquals(expected, jndi.lookup(DATABASE_JNDI_NAME));
	}
	
	@Test
	public void unbind() throws NamingException {
		jndi.bind(DATABASE_JNDI_NAME, new Object());
		jndi.unbind(DATABASE_JNDI_NAME);
		Assert.assertNull(jndi.lookup(DATABASE_JNDI_NAME));
	}
}
