package com.logicbus.together;

import java.util.concurrent.TimeUnit;


/**
 * 执行过程监视器
 * 
 * @author duanyy
 *
 */
public interface ExecuteWatcher {
	
	/**
	 * Logiclet执行完成
	 * @param logiclet logiclet
	 * @param duration 耗时
	 */
	public void executed(Logiclet logiclet,long duration,TimeUnit timeUnit);
}
