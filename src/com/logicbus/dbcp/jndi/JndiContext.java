package com.logicbus.dbcp.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.Watcher;
import com.logicbus.dbcp.core.ConnectionPool;

public class JndiContext implements com.anysoft.context.Context<ConnectionPool>{

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public ConnectionPool get(String id) {
		try {
			Context jndiCntx = (Context) (new InitialContext())
					.lookup("java:comp/env");
			DataSource ds = (DataSource) jndiCntx.lookup(id);
			if (ds != null)
				return new JndiConnectionPool(id, ds);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	@Override
	public void addWatcher(Watcher<ConnectionPool> watcher) {

	}

	@Override
	public void removeWatcher(Watcher<ConnectionPool> watcher) {

	}

}
