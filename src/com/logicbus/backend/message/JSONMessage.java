package com.logicbus.backend.message;

import java.io.OutputStream;
import com.anysoft.util.IOTools;

/**
 * JSON消息
 * 
 * @author duanyy
 *
 */
public class JSONMessage extends Message {
	/**
	 * 消息文本
	 */
	protected StringBuffer buf = null;
	/**
	 * 编码
	 */
	protected String encoding = "utf-8";
	
	/**
	 * constructor 
	 * @param _buf 消息文本
	 * @param _encoding 编码
	 */
	public JSONMessage(MessageDoc doc,StringBuffer _buf,String _encoding){
		super(doc);
		buf = _buf;
		encoding = _encoding;
		setContentType("application/json;charset="+encoding);
	}
	
	@Override
	public void output(OutputStream out) {
		try {
			String returnCode = msgDoc.getReturnCode();
			if (returnCode.equals("core.ok")){
				out.write(buf.toString().getBytes(encoding));
			}else{
				String reason = msgDoc.getReason();
				String retDoc = "{\"code\":\""+ returnCode +"\",\"reason\"=\"" + reason + "\"}";
				out.write(retDoc.getBytes(encoding));
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
