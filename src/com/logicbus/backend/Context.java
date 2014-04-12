package com.logicbus.backend;

import java.sql.Connection;
import com.anysoft.util.DefaultProperties;

/**
 * 服务访问的上下文
 * 
 * <p>
 * 记录了本次访问的一些上下文信息，例如服务参数、客户端IP等
 * 
 * @author duanyy
 * 
 * @version 1.0.5 [20140412 duanyy]
 * - 改进消息传递模型
 *
 */
abstract public class Context extends DefaultProperties{
	
	/**
	 * a db connection
	 */
	private Connection m_conn;
	
	
	/**
	 * to get the client ip
	 * @return client ip
	 */
	abstract public String getClientIp();
	
	
	/**
	 * 获取主机信息
	 * @return
	 */
	abstract public String getHost();
	
	/**
	 * 获取请求路径
	 * @return
	 */
	abstract public String getRequestURI();
	
	/**
	 * to get the db connection
	 * @return db connection
	 */
	public Connection getConnection(){return m_conn;}
	
	/**
	 * to set the db connection
	 * @param conn connection
	 */
	public void setConnection(Connection conn){m_conn = conn;}
}
