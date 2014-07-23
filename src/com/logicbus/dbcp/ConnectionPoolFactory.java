package com.logicbus.dbcp;

import com.anysoft.util.XMLConfigurable;

/**
 * 连接池工厂
 * @author duanyy
 * @since 1.2.5
 */
public interface ConnectionPoolFactory extends XMLConfigurable{
	/**
	 * 获取数据库连接池
	 * @param name 名称
	 * @return
	 */
	public ConnectionPool getPool(String name);
}
