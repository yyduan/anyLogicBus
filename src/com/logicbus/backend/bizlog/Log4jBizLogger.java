package com.logicbus.backend.bizlog;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.anysoft.util.Properties;
import com.logicbus.backend.BizLogItem;


/**
 * 输出到Log4j的BizLogger
 * 
 * @author duanyy
 *
 * @since 1.2.3
 */
public class Log4jBizLogger extends AbstractBizLogger {
	protected Logger logger = LogManager.getLogger(Log4jBizLogger.class);

	public Log4jBizLogger(Properties props) {
		super(props);		
	}

	@Override
	protected void onLog(BizLogItem item) {
		
	}

}
