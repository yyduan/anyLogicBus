package com.logicbus.backend.bizlog;

import com.anysoft.util.Properties;
import com.logicbus.backend.BizLogItem;


/**
 * 标准输出的BizLogger
 * 
 * @author duanyy
 * 
 * @since 1.2.3
 * 
 */
public class StdOutputBizLogger extends AbstractBizLogger {

	public StdOutputBizLogger(Properties props){
		super(props);
	}

	@Override
	public void close() {
	}

	@Override
	public void onLog(BizLogItem item) {
		System.out.println("Service:" + item.id);
		System.out.println("Request:" + item.url);
		System.out.println("Client:" + item.clientIP);
		System.out.println("Duration(ms):" + item.duration);
		System.out.println("Code/Reason:[" + item.result + "]" + item.reason);		

		if (item.content != null && item.content.length() > 0){
			System.out.println("Document:" + item.content);
		}
	}

}
