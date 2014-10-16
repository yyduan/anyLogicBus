package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.pool.QueuedPool2;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.stats.core.MetricsCollector;
import com.logicbus.dbcp.core.ConnectionPool;
import com.logicbus.dbcp.util.ConnectionPoolStat;

/**
 * ConnectionPool 虚基类
 * @author duanyy
 * 
 * @since 1.2.9
 *
 */
abstract public class AbstractConnectionPool extends QueuedPool2<Connection> implements ConnectionPool{
	protected ConnectionPoolStat stat = null;
	
	@Override
	public void create(Properties props){
		boolean enableStat = true;
		
		enableStat = PropertiesConstants.getBoolean(props, "dbcp.stat.enable", enableStat);
		
		if (enableStat){
			stat = new ConnectionPoolStat(props);
		}else{
			stat = null;
		}
	}
	
	@Override
	public void report(Element xml) {
		if (xml != null){
			Document doc = xml.getOwnerDocument();
			
			//pool
			{
				Element _pool = doc.createElement("pool");
				super.report(_pool);
				xml.appendChild(_pool);
			}
			
			// stat
			if (stat != null){
				Element _stat = doc.createElement("stat");
				super.report(_stat);
				xml.appendChild(_stat);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			//pool
			{
				Map<String,Object> _pool = new HashMap<String,Object>();
				super.report(_pool);
				json.put("pool", _pool);
			}
			
			if (stat != null){
				Map<String,Object> _stat = new HashMap<String,Object>();
				super.report(_stat);
				json.put("stat", _stat);
			}
		}
	}

	@Override
	public void report(MetricsCollector collector) {
		// to be define
	}

	@Override
	public Connection getConnection(int timeout, boolean enableRWS) {
		long start = System.currentTimeMillis();
		Connection conn = null;
		try {
			conn = borrowObject(0,
				timeout > getMaxWait() ? getMaxWait() : timeout);			
		}finally{
			if (stat != null){
				stat.visited(System.currentTimeMillis() - start, conn == null);
			}
		}
		return conn;
	}

	@Override
	public void recycle(Connection conn) {
		if (conn != null)
			returnObject(conn);
	}
	
	@Override
	protected String getIdOfMaxQueueLength() {
		return "maxActive";
	}

	@Override
	protected String getIdOfIdleQueueLength() {
		return "maxIdle";
	}
	
	/**
	 * 获取争抢连接时的最大等待时间
	 * @return
	 */
	abstract protected int getMaxWait();
}
