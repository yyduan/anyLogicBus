package com.logicbus.backend;

import com.anysoft.util.Properties;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 缺省的BizLogger实现
 * @author duanyy
 * @since 1.2.3
 */
public class DefaultBizLogger implements BizLogger {

	public DefaultBizLogger(Properties props){
		
	}
	
	@Override
	public void log(Path id,ServiceDescription description, MessageDoc mDoc, Context ctx) {
		
	}
}
