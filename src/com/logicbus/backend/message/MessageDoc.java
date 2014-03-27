package com.logicbus.backend.message;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * 消息文档
 * @author duanyy
 *
 */
public class MessageDoc {	
	/**
	 * 结果代码
	 */
	protected String returnCode = "core.ok";
	
	/**
	 * 原因
	 */
	protected String reason = "It is ok.";
	
	/**
	 * 时长
	 */
	protected long duration = 0;

	/**
	 * 获取结果代码
	 * @return 结果代码
	 */
	String getReturnCode(){return returnCode;}
	
	/**
	 * 获取原因
	 * @return
	 */
	String getReason(){return reason;}
	
	/**
	 * 获取时长
	 * @return 时长
	 */
	long getDuration(){return duration;}
	
	/**
	 * 设置调用结果
	 * 
	 * @param _code 结果代码
	 * @param _reason 原因
	 * @param _duration 调用时间
	 */	
	public void setReturn(String _code,String _reason,long _duration){
		returnCode = _code;
		reason = _reason;
		duration = _duration;
	}
	
	/**
	 * 消息文本
	 */
	protected StringBuffer doc;
	/**
	 * 文档编码
	 */
	protected String encoding = "utf-8";
	
	/**
	 * constructor
	 * 
	 * @param _inDoc 消息文本
	 * @param _encoding 编码
	 */
	public MessageDoc(StringBuffer _inDoc,String _encoding){
		doc = _inDoc;
		encoding = _encoding;
	}
	
	/**
	 * 消息实例
	 */
	protected Message msg = null;
	
	/**
	 * 作为XML消息
	 * 
	 * @return XML消息
	 */
	public XMLMessage asXML(){
		if (msg == null)
			msg = new XMLMessage(this,doc,encoding);
		return (XMLMessage)msg;
	}
	
	/**
	 * 作为JSON消息
	 * 
	 * @return JSON消息
	 */
	public JSONMessage asJSON(){
		if (msg == null){
			msg = new JSONMessage(this,doc,encoding);
		}
		return (JSONMessage)msg;
	}
	
	/**
	 * 获取文档的content-type
	 * @return content-type
	 */
	public String getContentType(){
		if (msg != null){
			return msg.getContentType();
		}
		return "text/xml;charset=utf-8";
	}
	
	/**
	 * 输出文档到输出流
	 * @param out
	 * @param response 
	 */
	public void output(OutputStream out, HttpServletResponse response){
		if (msg != null){
			response.setCharacterEncoding(encoding);
			msg.output(out);
		}else{
			if (!returnCode.equals("core.ok")){
				try {
					response.setCharacterEncoding(encoding);
					response.sendError(404,reason);
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	/**
	 * to string
	 */
	public String toString(){
		if (msg == null)
			return doc.toString();
		return msg.toString();
	}
}
