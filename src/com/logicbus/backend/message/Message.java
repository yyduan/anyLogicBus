package com.logicbus.backend.message;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;


/**
 * 消息
 * @author duanyy
 * @version 1.0.4 [20140410 duanyy] <br>
 * - 增加encoding成员
 * - {@link com.logicbus.backend.message.Message#output(OutputStream, HttpServletResponse) out}函数
 * 增加response参数，以便Message直接操作.
 */
abstract public class Message {

	/**
	 * 文档
	 */
	protected MessageDoc msgDoc = null;
	
	protected Message(MessageDoc _doc,String _encoding){
		msgDoc = _doc;
		encoding = _encoding;
	}

	/**
	 * 编码
	 * 
	 * @since 1.0.4
	 */
	protected String encoding = "utf-8";
	
	/**
	 * 设置encoding
	 * @param _encoding
	 * 
	 * @since 1.0.4
	 */
	public void setEncoding(String _encoding){
		encoding = _encoding;
	}
	
	/**
	 * 获取encoding
	 * @return
	 * 
	 * @since 1.0.4
	 */
	public String getEncoding(){
		return encoding;
	}
	/**
	 * 输出消息到输出流
	 * @param out 输出流
	 */
	abstract public void output(OutputStream out,HttpServletResponse response);

	/**
	 * content-type
	 */
	protected String contentType = "text/xml;charset=utf-8";
	
	/**
	 * 设置content-type
	 * @param type content-type
	 */
	public void setContentType(String type){contentType = type;}
	
	/**
	 * 获取content-type
	 * 
	 * @return content-type
	 */
	public String getContentType(){return contentType;}
}
