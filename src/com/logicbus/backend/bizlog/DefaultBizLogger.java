package com.logicbus.backend.bizlog;

import com.anysoft.util.Properties;
import com.logicbus.backend.BizLogItem;
import com.logicbus.backend.BizLogger;


/**
 * 缺省的BizLogger实现
 * @author duanyy
 * @since 1.2.3
 */
public class DefaultBizLogger implements BizLogger {

	public DefaultBizLogger(Properties props){
		
	}


	@Override
	public void close() {
	}


	@Override
	public void log(BizLogItem item) {

	}
}
