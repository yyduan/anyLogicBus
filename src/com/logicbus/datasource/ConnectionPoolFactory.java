package com.logicbus.datasource;

import com.anysoft.util.Factory;
import com.anysoft.util.Settings;

/**
 * 连接池工厂
 * 
 * <br>
 * 用于创建连接池
 * 
 * @author duanyy
 * @since 1.0.6
 * 
 */
public class ConnectionPoolFactory extends Factory<ConnectionPool> {
	
	protected static ConnectionPool instance = null;
	
	synchronized static public ConnectionPool getPool(){
		if (instance == null){
			Settings settings = Settings.get();
			String module = settings.GetValue("dbcp.module",
					"com.logicbus.datasource.XmlResourceConnectionPool");
			
			ConnectionPoolFactory factory = new ConnectionPoolFactory();
			instance = factory.newInstance(module, settings);
		}
		return instance;
	}
}
