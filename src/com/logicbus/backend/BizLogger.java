package com.logicbus.backend;

import com.anysoft.util.Factory;

/**
 * 业务日志接口
 * 
 * @author duanyy
 * @since 1.2.3
 */
public interface BizLogger {
	
	
	/**
	 * 记录日志
	 * @param item 日志项
	 */
	public void log(BizLogItem item);
	
	/**
	 * 关闭
	 */
	public void close();
	
	public static class TheFactory extends Factory<BizLogger>{

	}	
}
