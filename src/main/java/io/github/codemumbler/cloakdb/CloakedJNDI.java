package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class CloakedJNDI {

	private static final String CONTEXT_FACTORY = CloakedContextFactory.class.getCanonicalName();
	static {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
	}

	CloakedJNDI(){
		try {
			InitialContext ic = new InitialContext();
			if (ic.lookup("java:/comp/env") == null) {
				ic.createSubcontext("java:/comp/env");
			}
		} catch (NamingException e){
			e.printStackTrace();
		}
	}
}
