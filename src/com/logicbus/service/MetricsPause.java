package com.logicbus.service;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Settings;
import com.logicbus.backend.AbstractServant;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.JsonMessage;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.backend.metrics.core.MetricsHandler;
import com.logicbus.models.servant.ServiceDescription;

public class MetricsPause extends AbstractServant{

	@Override
	protected void onDestroy() {
	}

	@Override
	protected void onCreate(ServiceDescription sd) throws ServantException{
	}

	@Override
	protected int onXml(MessageDoc msgDoc, Context ctx) throws Exception{
		XMLMessage msg = (XMLMessage) msgDoc.asMessage(XMLMessage.class);
		
		Settings settings = Settings.get();
		
		MetricsHandler handler = (MetricsHandler) settings.get("metricsHandler");
		
		if (handler != null){
			handler.pause();
			Element root = msg.getRoot();
			Document doc = root.getOwnerDocument();
			
			Element logger = doc.createElement(handler.getHandlerType());
			
			handler.report(logger);
			
			root.appendChild(logger);
		}
		return 0;
	}

	@Override
	protected int onJson(MessageDoc msgDoc, Context ctx) throws Exception{
		JsonMessage msg = (JsonMessage)msgDoc.asMessage(JsonMessage.class);
		
		Settings settings = Settings.get();
		
		MetricsHandler handler = (MetricsHandler) settings.get("metricsHandler");
		if (handler != null){
			handler.pause();
			
			Map<String,Object> root = msg.getRoot();
		
			Map<String,Object> logger = new HashMap<String,Object>();
			
			handler.report(logger);
			
			root.put(handler.getHandlerType(), logger);
		}
		return 0;
	}
}