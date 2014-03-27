package com.logicbus.backend.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * tomcat的数据库连接池
 * 
 * @author duanyy
 *
 */
public class TomcatPoolConn extends PoolConn {
	@Override 
	public Connection getConnection(String name) {
		Context jndiCntx;
		try {
				jndiCntx = (Context) (new InitialContext()).lookup("java:comp/env");
				DataSource ds =  (DataSource)jndiCntx.lookup( name); 
		   return ds.getConnection(); 
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Throwable t){
			t.printStackTrace();
		}
		return null;
		}
	
	@Override
	public void Recycle(Connection conn)
	{
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
