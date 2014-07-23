package com.logicbus.dbcp.jndi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.w3c.dom.Element;

import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.ConnectionPoolStat;
import com.logicbus.dbcp.sql.SQLTools;

/**
 * 基于JNDI实现的ConnectionPool
 * 
 * @author duanyy 
 * @since 1.2.5
 *
 */
public class JNDIConnectionPool implements ConnectionPool {
	protected String name;
	protected DataSource datasource = null;
	
	public JNDIConnectionPool(String _name,DataSource _ds){
		name = _name;
		datasource = _ds;
	}
	
	@Override
	public ConnectionPoolStat getStat() {
		return null;
	}

	@Override
	public Connection getConnection(int timeout) {
		if (datasource != null){
			try {
				return datasource.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void recycle(Connection conn) {
		SQLTools.close(conn);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void report(Element root) {
		root.setAttribute("module", JNDIConnectionPool.class.getName());
	}

	@Override
	public void report(Map<String, Object> json) {
		json.put("module", JNDIConnectionPool.class.getName());
	}
}
