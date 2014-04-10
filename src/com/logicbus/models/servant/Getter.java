package com.logicbus.models.servant;

import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;

/**
 * 服务调用参数Getter
 * 
 * @author duanyy
 *
 * @since 1.0.3
 */
public interface Getter {
	
	/**
	 * 获取参数值
	 * @param msg
	 * @param ctx
	 * @return
	 */
	public String getValue(Argument argu,MessageDoc msg,Context ctx)throws ServantException;
}
