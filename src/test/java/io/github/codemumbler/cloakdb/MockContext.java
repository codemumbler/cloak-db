package io.github.codemumbler.cloakdb;

import javax.naming.NamingException;

public class MockContext extends CloakContext {

	@Override
	public Object lookup(String name) throws NamingException {
		throw new NamingException("Mocks throw exceptions");
	}
}
