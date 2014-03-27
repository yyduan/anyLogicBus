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
 */
public class Context extends DefaultProperties{
	
	/**
	 * a db connection
	 */
	private Connection m_conn;
	/**
	 * the start time
	 */
	private long m_start_time;
	/**
	 * the end time
	 */
	private long m_end_time;
	
	/**
	 * the return code
	 */
	private String m_return_code = "core.ok";
	
	/**
	 * the error reason
	 */
	private String m_reason = "It is ok.";
	
	/**
	 * the client ip
	 */
	private String m_ip;
	
	/**
	 * to get the client ip
	 * @return client ip
	 */
	public String getClientIp(){return m_ip;}
	
	/**
	 * to set the client ip
	 * @param _ip client ip
	 */
	public void setClientIp(String _ip){m_ip = _ip;}
	
	/**
	 * to get the start time
	 * @return start time
	 */
	public long getStartTime(){return m_start_time;}
	
	/**
	 * to set the start time
	 * @param start_time start time
	 */
	public void setStartTime(long start_time){m_start_time = start_time;}
	
	/**
	 * to get the end time
	 * @return end time
	 */
	public long getEndTime(){return m_end_time;}

	/**
	 * to set the end time
	 * @param end_time end time
	 */
	public void setEndTime(long end_time){m_end_time = end_time;}
	
	/**
	 * to set the return code
	 * @param code return code
	 */
	public void setReturnCode(String code){m_return_code = code;}
	
	/**
	 * to set the error reason
	 * @param reason error reason
	 */
	public void setReason(String reason){ m_reason = reason;}

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
	
	/**
	 * to get the return code
	 * @return return code
	 */
	public String getReturnCode(){return m_return_code;}
	
	/**
	 * to get the error reason
	 * @return error reason
	 */
	public String getReason(){return m_reason;}
}
