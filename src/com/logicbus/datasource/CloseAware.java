package com.logicbus.datasource;

import java.sql.Connection;


/**
 * 数据库关闭感知接口
 * 
 * @author duanyy
 * 
 * @since 1.0.6
 */
public interface CloseAware {
	
	/**
	 * 数据库连接被关闭
	 * @param conn
	 */
	public void connectionClosed(Connection conn);
}
