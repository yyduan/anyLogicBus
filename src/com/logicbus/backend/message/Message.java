package com.logicbus.backend.message;

import java.io.OutputStream;


/**
 * 消息
 * @author duanyy
 *
 */
abstract public class Message {

	/**
	 * 文档
	 */
	protected MessageDoc msgDoc = null;
	
	protected Message(MessageDoc _doc){
		msgDoc = _doc;
	}
	
	/**
	 * 输出消息到输出流
	 * @param out 输出流
	 */
	abstract public void output(OutputStream out);

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
