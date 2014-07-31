package com.logicbus.dbcp.impl;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Factory;
import com.anysoft.util.Properties;
import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.ConnectionPoolFactory;

/**
 * 基于Provider的ConnectionPoolFactory
 * 
 * @author duanyy
 * @since 1.2.5
 * @version 1.2.5.3 [20140731 duanyy]
 * -  基础包的Cacheable接口修改
 */
public class Provided implements ConnectionPoolFactory{
	protected static final Logger logger = LogManager.getLogger(Provided.class);
	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		try {
			TheFactory factory = new TheFactory();
			provider = factory.newInstance(_e, _properties, "provider");
		}catch (Exception ex){
			logger.error("Can not create provider",ex);
		}
	}

	@Override
	public ConnectionPool getPool(String name) {
		if (provider != null){
			ConnectionModel model = provider.load(name,true);
			if (model != null){
				return new ConnectionPoolImpl(model);
			}
		}
		return null;
	}

	protected DBCMProvider provider = null;
	
	public static class TheFactory extends Factory<DBCMProvider>{
		
	}
}
