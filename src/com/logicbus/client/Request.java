package com.logicbus.client;

/**
 * 客户端请求
 * @author duanyy
 * @since 1.0.4
 */
public interface Request{
	/**
	 * 获取服务器请求的属性
	 * @param name 属性名
	 * @param defaultValue 缺省值
	 * @return 属性值
	 */
	public String getRequestAttribute(String name,String defaultValue);
	
	/**
	 * 获取缓冲区对象
	 * @return StringBuffer
	 */
	public StringBuffer getBuffer();
}
