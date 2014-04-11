package com.logicbus.client;

/**
 * 服务器响应
 * @author duanyy
 * @since 1.0.4
 */
public interface Response {

	/**
	 * 设置服务器响应的属性
	 * @param name 属性名
	 * @param value 属性值
	 */
	public void setResponseAttribute(String name,String value);
	
	/**
	 * 获取缓冲区对象
	 * @return StringBuffer
	 */
	public StringBuffer getBuffer();	
}
