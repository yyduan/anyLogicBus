package com.logicbus.backend.datasource;

import java.sql.Connection;

import com.anysoft.util.Settings;

/**
 * 数据库连接池
 * 
 * @author duanyy
 *
 */
abstract public class PoolConn {
	/**
	 * 按名称获取数据库连接
	 * @param name 数据源ID
	 * @return
	 */
	abstract public Connection getConnection(String name);

	/**
	 * 归还数据库连接
	 * @param conn 数据库连接
	 */
	abstract public void Recycle(Connection conn);

	/**
	 * 缺省的连接池名称
	 */
	static protected String defaultPoolName = "";

	/**
	 * 获取缺省的数据库连接
	 * @return
	 */
	public Connection getConnection() {
		if (defaultPoolName != null && defaultPoolName.length() > 0) {
			return getConnection(defaultPoolName);
		}
		return null;
	}

	/**
	 * 获取指定数据源的数据库连接，并确定是否设置为缺省
	 * @param name 数据源ID
	 * @param asDefault 是否作为缺省
	 * @return 数据库连接
	 */
	public Connection getConnection(String name, boolean asDefault) {
		if (asDefault) {
			defaultPoolName = name;
		}
		return getConnection(name);
	}

	/**
	 * 获取资源池实例
	 * @return
	 */
	public static PoolConn getInstance() {
		PoolConn ret = null;
		try {
			String class_name = Settings.get().GetValue("module.poolconn",
					"com.logicbus.backend.datasource.TomcatPoolConn");
			ret = (PoolConn) Class.forName(class_name).newInstance();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return ret;
	}
}
