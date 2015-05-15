package io.github.codemumbler.cloakdb;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class CloakedContextFactory implements InitialContextFactory {

	private static final Context context = new CloakedContext();

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return context;
	}
}
