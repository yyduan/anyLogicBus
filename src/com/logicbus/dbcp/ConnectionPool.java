package com.logicbus.dbcp;

import java.sql.Connection;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * 数据库连接池
 * 
 * <br>
 * 管理数据库连接
 * 
 * @author duanyy
 * @since 1.2.5
 */
public interface ConnectionPool {
	/**
	 * 获取统计信息
	 * @return
	 */
	public ConnectionPoolStat getStat();
	
	/**
	 * 按名称获取数据库连接
	 * 
	 * @param timeout 超时时间 
	 * @return 数据库连接实例
	 * 
	 */
	public Connection getConnection(int timeout);

	/**
	 * 归还数据库连接
	 * 
	 * @param conn 数据库连接
	 */
	public void recycle(Connection conn);
	
	/**
	 * 获取连接池名称
	 * 
	 * @return
	 */
	public String getName();
	
	public void report(Element root);
	
	public void report(Map<String,Object> json);
}
