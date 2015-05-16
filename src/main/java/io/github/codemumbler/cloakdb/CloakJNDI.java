package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class CloakJNDI {

	private static final String CONTEXT_FACTORY = CloakContextFactory.class.getCanonicalName();
	private static final String DEFAULT_ENV_CONTEXT = "java:/comp/env";

	static {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
	}

	CloakJNDI(){
		try {
			InitialContext ic = new InitialContext();
			if (ic.lookup(DEFAULT_ENV_CONTEXT) == null) {
				ic.createSubcontext(DEFAULT_ENV_CONTEXT);
			}
		} catch (NamingException e){
			throw new CloakDBException("Failed to establish an InitialContext.");
		}
	}

	void bind(String jndiName, Object value) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(DEFAULT_ENV_CONTEXT);
			env.bind(jndiName, value);
		} catch (NamingException e) {
			throw new CloakDBException("Bind failed on JNDI name: " + jndiName);
		}
	}

	Object lookup(String jndiName) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(DEFAULT_ENV_CONTEXT);
			return env.lookup(jndiName);
		} catch (NamingException e) {
			throw new CloakDBException("Lookup failed for JNDI name: " + jndiName);
		}
	}

	void unbind(String jndiName) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(DEFAULT_ENV_CONTEXT);
			env.unbind(jndiName);
		} catch (NamingException e) {
			throw new CloakDBException("Failed to unbind for JNDI name: " + jndiName);
		}
	}
}
