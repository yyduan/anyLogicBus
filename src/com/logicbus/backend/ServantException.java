package com.logicbus.backend;


/**
 * 服务过程异常类
 * 
 * @author duanyy
 *
 */
public class ServantException extends Exception {
	private static final long serialVersionUID = -5968077876441355718L;
	
	/**
	 * 错误代码
	 */
	private String m_code;
	
	/**
	 * 获取错误代码
	 * @return
	 */
	public String getCode(){return m_code;}

	/**
	 * constructor
	 * 
	 * @param code 错误代码
	 * @param message 错误原因
	 */
	public ServantException(String code,String message){
		super(message);
		m_code = code;		
	}
}
