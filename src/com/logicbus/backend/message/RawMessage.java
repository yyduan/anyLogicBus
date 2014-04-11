package com.logicbus.backend.message;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.anysoft.util.IOTools;

/**
 * Raw消息
 * 
 * @author duanyy
 * 
 * @since 1.0.4
 */
public class RawMessage extends Message {
	/**
	 * 消息文本
	 */
	protected StringBuffer buf = null;
	
	protected RawMessage(MessageDoc _doc,StringBuffer _buf,String _encoding) {
		super(_doc,_encoding);
		buf = _buf;
	}

	@Override
	public void output(OutputStream out,HttpServletResponse response) {
		try {
			String returnCode = msgDoc.getReturnCode();
			if (returnCode.equals("core.ok")){
				out.write(buf.toString().getBytes(encoding));
			}else{
				try {
					response.setCharacterEncoding(encoding);
					response.sendError(404,msgDoc.getReason());
				} catch (IOException e) {
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			IOTools.closeStream(out);
		}
	}
	/**
	 * 获取消息文本
	 * @return 消息文本
	 */
	public StringBuffer getBuffer(){
		return buf;
	}
	
	/**
	 * to string
	 */
	public String toString(){
		return buf.toString();
	}

}
