package com.logicbus.backend;

import com.anysoft.util.Factory;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 业务日志接口
 * 
 * @author duanyy
 * @since 1.2.3
 */
public interface BizLogger {
	
	/**
	 * 记录日志
	 * @param description 服务描述
	 * @param mDoc 文档
	 * @param ctx 上下文
	 */
	void log(ServiceDescription description, MessageDoc mDoc, Context ctx);
	
	public static class TheFactory extends Factory<BizLogger>{
		public TheFactory(ClassLoader cl){
			super(cl);
		}
	}	
}
