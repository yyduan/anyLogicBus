package com.logicbus.client;

/**
 * 服务调用缓冲区 
 * 
 * <br>
 * 用于保存输入输出数据。
 * 
 * @author duanyy
 * @since 1.0.4
 */
public class Buffer {
	
	/**
	 * 构造函数
	 * <br>
	 * 仅提供给子类调用
	 * @param bufSize 
	 */
	protected Buffer(int bufSize){
		content = new StringBuffer(bufSize);
	}
	
	/**
	 * Content Type
	 */
	protected String contentType;

	/**
	 * 设置Content Type
	 * @param _contentType
	 */
	public void setContentType(String _contentType){
		contentType = _contentType;
	}
	
	/**
	 * 获取content type
	 * @return content type
	 */
	public String getContentType(){
		return contentType;
	}
	
	/**
	 * Content
	 */
	protected StringBuffer content = null;
	
	/**
	 * 获取content
	 * @return
	 */
	public String getContent(){return content == null ? "":content.toString();}
	
	/**
	 * 获取缓冲区对象
	 * @return StringBuffer
	 */
	public StringBuffer getBuffer(){return content;}
	
	/**
	 * Encoding
	 */
	protected String encoding = "utf-8";
	
	/**
	 * 设置encoding
	 * @param _encoding
	 */
	public void setEncoding(String _encoding){
		encoding = _encoding;
	}
	
	/**
	 * 获取encoding
	 * @return
	 */
	public String getEncoding(){return encoding;}
}
