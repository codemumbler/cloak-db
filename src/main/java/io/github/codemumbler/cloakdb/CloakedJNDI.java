package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class CloakedJNDI {

	private static final String CONTEXT_FACTORY = CloakedContextFactory.class.getCanonicalName();
	public static final String ENV_CONTEXT = "java:/comp/env";

	static {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
	}

	CloakedJNDI(){
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
			Object value = env.lookup(jndiName);
			return value;
		} catch (NamingException e) {
		}
		return null;
	}
}
