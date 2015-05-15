package io.github.codemumbler.cloakdb;

import org.junit.Assert;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

public class CloakedJNDITest {

	@Test
	public void createInitialContext() throws NamingException {
		new CloakedJNDI();
		try {
			new InitialContext();
		} catch ( NoInitialContextException e ) {
			Assert.fail("ContextFactory is not set up correctly: " + e.getMessage());
		}
	}

	@Test
	public void setEnvContext() throws NamingException {
		new CloakedJNDI();
		InitialContext context = new InitialContext();
		Assert.assertNotNull(context.lookup("java:/comp/env"));
	}
}
