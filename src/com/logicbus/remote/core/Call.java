package com.logicbus.remote.core;

import com.anysoft.util.XMLConfigurable;

/**
 * 远程服务调用
 * 
 * @author duanyy
 *
 * 
 * @since 1.2.9
 * 
 */
public interface Call extends AutoCloseable,XMLConfigurable{
	
	/**
	 * 创建参数实例
	 * @return
	 */
	public Parameters createParameter();
	
	/**
	 * 执行运程调用
	 * @param paras
	 * @return
	 * @throws CallException
	 */
	public Result execute(Parameters paras) throws CallException;
}
