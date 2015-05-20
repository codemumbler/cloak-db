package io.github.codemumbler.cloakdb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CloakJNDITest {

	private static final String DATABASE_JNDI_NAME = "jdbc/dbName";
	private static final String ENV_CONTEXT = "java:/comp/env";
	private CloakJNDI jndi;

	@Before
	public void setUp() {
		jndi = new CloakJNDI();
	}

	@After
	public void tearDown() {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CloakContextFactory.class.getCanonicalName());
	}

	@Test
	public void setInitialContextFactory() {
		Assert.assertEquals(CloakContextFactory.class.getCanonicalName(), System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
	}

	@Test
	public void setEnvContext() throws Exception {
		InitialContext context = new InitialContext();
		Assert.assertNotNull(context.lookup(ENV_CONTEXT));
	}

	@Test
	public void lookupInEnvContext() throws Exception {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup(ENV_CONTEXT);
		final Object expected = new Object();
		env.bind(DATABASE_JNDI_NAME, expected);
		Assert.assertEquals(expected, jndi.lookup(DATABASE_JNDI_NAME));
	}

	@Test
	public void bindObject() {
		final Object expected = new Object();
		jndi.bind(DATABASE_JNDI_NAME, expected);
		Assert.assertEquals(expected, jndi.lookup(DATABASE_JNDI_NAME));
	}

	@Test
	public void unbind() {
		jndi.bind(DATABASE_JNDI_NAME, new Object());
		jndi.unbind(DATABASE_JNDI_NAME);
		Assert.assertNull(jndi.lookup(DATABASE_JNDI_NAME));
	}

	@Test(expected = CloakDBException.class)
	public void failToBindInitialContext() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockContextFactory.class.getCanonicalName());
		jndi = new CloakJNDI();
	}

	@Test(expected = CloakDBException.class)
	public void failToBind() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockContextFactory.class.getCanonicalName());
		jndi.bind(DATABASE_JNDI_NAME, new Object());
	}

	@Test(expected = CloakDBException.class)
	public void failToLookup() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockContextFactory.class.getCanonicalName());
		jndi.lookup(DATABASE_JNDI_NAME);
	}

	@Test(expected = CloakDBException.class)
	public void failToUnbind() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockContextFactory.class.getCanonicalName());
		jndi.unbind(DATABASE_JNDI_NAME);
	}
}
