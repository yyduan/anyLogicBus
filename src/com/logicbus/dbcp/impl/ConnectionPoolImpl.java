package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.pool.QueuedPool;
import com.anysoft.util.BaseException;
import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;
import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.ConnectionPoolStat;

/**
 * ConnectionPool实现
 * @author duanyy
 * @since 1.2.5
 * 
 * @version 1.2.6.3 [20140815 duanyy]
 * - 配合基础类库Pool修改
 */
public class ConnectionPoolImpl extends QueuedPool<Connection> implements ConnectionPool {
	protected ConnectionModel model = null;
	protected ConnectionPoolStatImpl stat = null;
	protected ConnectionPoolImpl(ConnectionModel _model){
		model = _model;
		
		stat = new ConnectionPoolStatImpl();
		stat.setMonitor(model.getMonitor());
		
		Properties props = new DefaultProperties();		
		props.SetValue(getIdOfMaxQueueLength(), 
				String.valueOf(model.getMaxActive()));
		props.SetValue(getIdOfIdleQueueLength(), 
				String.valueOf(model.getMaxIdle()));
		create(props);
	}
	
	@Override
	public ConnectionPoolStat getStat() {
		return stat;
	}

	@Override
	public Connection getConnection(int timeout) {
		long start = System.currentTimeMillis();
		Connection conn = null;
		try {
			conn = borrowObject(0,
				timeout > model.getMaxWait() ? model.getMaxWait() : timeout);			
		}finally{
			visited(System.currentTimeMillis() - start,conn == null);
		}
		return conn;
	}

	@Override
	public void recycle(Connection conn) {
		if (conn != null)
			returnObject(conn);
	}

	@Override
	public String getName() {
		return model.getName();
	}

	@Override
	protected String getIdOfMaxQueueLength() {
		return "queueLength";
	}

	@Override
	protected String getIdOfIdleQueueLength() {
		return "idleLength";
	}	
	
	@Override
	protected Connection createObject() throws BaseException {
		return model.newConnection();
	}

	protected static int count = 0;
	
	protected void visited(long duration, boolean isNull) {
		if (stat != null) {
			stat.visited(getCreatingCnt(),getWorkingCnt(), getIdleCnt(),
					getWaitCnt(), duration, isNull);
		}
	}

	@Override
	public void report(Element root) {
		root.setAttribute("module", ConnectionPoolImpl.class.getName());
		model.report(root);
		
		Document doc = root.getOwnerDocument();
		Element stat = doc.createElement("stat");		
		getStat().toXML(stat);		
		root.appendChild(stat);
	}

	@Override
	public void report(Map<String, Object> json) {
		json.put("module", ConnectionPoolImpl.class.getName());	
		model.report(json);
		
		Map<String,Object> stat = new HashMap<String,Object>();
		getStat().toJson(stat);		
		json.put("stat", stat);
	}


}
