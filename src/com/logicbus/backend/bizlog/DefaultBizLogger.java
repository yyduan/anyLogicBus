package com.logicbus.backend.bizlog;


import com.anysoft.util.Properties;
import com.logicbus.backend.BizLogItem;



/**
 * 缺省的BizLogger实现
 * @author duanyy
 * @since 1.2.3
 */
public class DefaultBizLogger extends AbstractBizLogger {

	public DefaultBizLogger(Properties props){
		super(props);
	}


	@Override
	public void close() {
	}


	@Override
	public void onLog(BizLogItem item) {

	}

}
