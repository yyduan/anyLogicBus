package com.logicbus.datasource;

import java.sql.Connection;
import java.util.Hashtable;

import com.anysoft.util.Properties;

/**
 * ConnectionPool虚类
 * 
 * <br>
 * 由anyLogicBus自己管理连接。
 * 
 * @author duanyy
 * @since 1.0.6
 */
abstract public class AbstractConnectionPool implements ConnectionPool {

	public AbstractConnectionPool(Properties props) {	
		loadConfig(props);		
	}

	/**
	 * 装入配置
	 * @param props 变量集
	 */
	abstract protected void loadConfig(Properties props);

	@Override
	public Connection getConnection(String name,long timeout) {
		Connection conn = null;
		
		//查找是否存在名称为name的connector
		NamedDataSource connector = datasources.get(name);
		if (connector != null){
			//存在
			conn = connector.getConnection(timeout);
		}		
		return conn;
	}

	@Override
	public void recycle(Connection conn) {
		SQLTools.close(conn);
	}

	@Override
	public NamedDataSource getDataSource(String name) {
		return get(name);
	}	
	
	/**
	 * 返回连接池中所有名称
	 * @return
	 */
	public String [] names(){
		return datasources.keySet().toArray(new String[0]);
	}
	
	/**
	 * 获取指定名称的datasource
	 * @param name 名称
	 * @return
	 */
	public NamedDataSource get(String name){
		return datasources.get(name);
	}
	
	/**
	 * 加入Connector
	 * @param name
	 * @param connector
	 */
	public void add(String name,NamedDataSource connector){
		if (name != null && connector != null){
			datasources.put(name, connector);
		}
	}
	
	/**
	 * 删除指定名称的Connector
	 * @param name
	 */
	public void remove(String name){
		datasources.remove(name);
	}
	
	/**
	 * 连接池中的链接器
	 */
	protected Hashtable<String,NamedDataSource> datasources = new Hashtable<String,NamedDataSource>();
}
