package com.logicbus.dbcp.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.ConnectionPoolFactory;

/**
 * 基于JNDI的连接池工厂实现
 * @author duanyy
 *
 */
public class JNDIConnectionPoolFactory implements ConnectionPoolFactory {
	@Override
	public ConnectionPool getPool(String name) {
		try {
			Context jndiCntx = (Context) (new InitialContext())
					.lookup("java:comp/env");
			DataSource ds = (DataSource) jndiCntx.lookup(name);
			if (ds != null)
				return new JNDIConnectionPool(name, ds);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
	}
}
