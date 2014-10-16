package com.logicbus.dbcp.impl;

import com.anysoft.context.Context;
import com.anysoft.context.XMLResource;
import com.logicbus.dbcp.core.ConnectionPool;


/**
 * 基于外部XRC的Context实现
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class XRC extends XMLResource<Context<ConnectionPool>> {

	@Override
	public String getObjectName() {
		return "dbcp";
	}

	@Override
	public String getDefaultClass() {
		return XMLConfigurableImpl.class.getName();
	}

	@Override
	public String getDefaultXrc() {
		return "java:///com/logicbus/dbcp/context/dbcp.xrc.default.xml#" + XRC.class.getName();
	}

}