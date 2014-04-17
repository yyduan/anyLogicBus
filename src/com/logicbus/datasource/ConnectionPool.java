package com.logicbus.datasource;

import java.sql.Connection;

/**
 * 数据库连接池
 * 
 * <br>
 * 管理数据库连接
 * 
 * @author duanyy
 * @since 1.0.6
 */
public interface ConnectionPool {

	/**
	 * 按名称获取数据库连接
	 * 
	 * @param name 数据源ID
	 * @param timeout 超时时间
	 * @return 数据库连接
	 */
	public Connection getConnection(String name,long timeout);

	/**
	 * 归还数据库连接
	 * 
	 * @param conn 数据库连接
	 */
	public void recycle(Connection conn);
	
	/**
	 * 查找数据源
	 * @param name　数据源的名称
	 * @return
	 */
	public NamedDataSource getDataSource(String name);
}
