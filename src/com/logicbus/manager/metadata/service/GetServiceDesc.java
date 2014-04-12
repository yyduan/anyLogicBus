package com.logicbus.manager.metadata.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServantManager;
import com.logicbus.models.servant.ServiceDescription;

public class GetServiceDesc extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		String serviceId = ctx.GetValue("service", "");
		if (serviceId == null || serviceId.length() <= 0) {
			throw new ServantException("client.args_not_found",
					"Can not find parameter:service");
		}
		
		ServantManager sm = ServantManager.get();
		ServiceDescription sd = sm.get(new Path(serviceId));
		if (sd == null){
			throw new ServantException("user.data_not_found","Service does not exist:" + serviceId);
		}
		
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);	
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		
		Element eService = doc.createElement("service");
		
		sd.toXML(eService);
		
		root.appendChild(eService);
		return 0;
	}

}
