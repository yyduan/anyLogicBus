package com.logicbus.backend;

import java.util.Map;

import org.w3c.dom.Element;

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
	
	/**
	 * 生成XML报告
	 * @param root 
	 */
	public void report(Element root);
	
	/**
	 * 生成JSON报告
	 * @param json
	 */
	public void report(Map<String,Object> json);
	
	/**
	 * 工厂类
	 * @author duanyy
	 *
	 */
	public static class TheFactory extends Factory<BizLogger>{

	}	
}
