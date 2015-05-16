package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class CloakJNDI {

	private static final String CONTEXT_FACTORY = CloakContextFactory.class.getCanonicalName();
	private static final String ENV_CONTEXT = "java:/comp/env";

	static {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
	}

	CloakJNDI(){
		try {
			InitialContext ic = new InitialContext();
			if (ic.lookup(ENV_CONTEXT) == null) {
				ic.createSubcontext(ENV_CONTEXT);
			}
		} catch (NamingException e){
			e.printStackTrace();
		}
	}

	public void bind(String jndiName, Object value) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(ENV_CONTEXT);
			env.bind(jndiName, value);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public Object lookup(String jndiName) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(ENV_CONTEXT);
			return env.lookup(jndiName);
		} catch (NamingException ignored) {
		}
		return null;
	}

	public void unbind(String jndiName) {
		try {
			Context ctx = new InitialContext();
			Context env = (Context) ctx.lookup(ENV_CONTEXT);
			env.unbind(jndiName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
}
