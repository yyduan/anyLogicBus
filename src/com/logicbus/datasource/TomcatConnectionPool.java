package com.logicbus.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.anysoft.util.Properties;


/**
 * Tomcat的数据库连接池
 * 
 * @author duanyy
 * @since 1.0.6
 */
public class TomcatConnectionPool implements ConnectionPool {

	public TomcatConnectionPool(Properties props) {
	}

	@Override
	public Connection getConnection(String name,long timeout) {
		Context jndiCntx = null;
		try {
			jndiCntx = (Context) (new InitialContext()).lookup("java:comp/env");
			DataSource ds = (DataSource) jndiCntx.lookup(name);
			return ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	@Override
	public void recycle(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public NamedDataSource getDataSource(String name) {
		return null;
	}

}
