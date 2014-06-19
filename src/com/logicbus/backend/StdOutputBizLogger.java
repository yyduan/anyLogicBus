package com.logicbus.backend;

import com.anysoft.util.Properties;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 标准输出的BizLogger
 * 
 * @author duanyy
 * 
 * @since 1.2.3
 * 
 */
public class StdOutputBizLogger implements BizLogger {

	public StdOutputBizLogger(Properties props){
		
	}
	@Override
	public void log(Path id,ServiceDescription desc, MessageDoc mDoc, Context ctx) {
		System.out.println("Service:" + id.toString());
		System.out.println("Request:" + ctx.getRequestURI());
		System.out.println("Client:" + ctx.getClientIp());
		System.out.println("Duration(ms):" + mDoc.getDuration());
		System.out.println("Code/Reason:[" + mDoc.getReturnCode() + "]" + mDoc.getReason());		

		if (desc != null){
			ServiceDescription.LogType logType = desc.getLogType();
			
			if (logType == ServiceDescription.LogType.detail){
				System.out.println("Document:" + mDoc.toString());
			}
		}
	}

}
