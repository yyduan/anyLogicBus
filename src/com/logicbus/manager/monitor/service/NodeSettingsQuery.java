package com.logicbus.manager.monitor.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Settings;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;

public class NodeSettingsQuery extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		//创捷worker节点
		Element eSettings = doc.createElement("settings");
		{
			Settings settings = Settings.get();
			settings.toXML(eSettings);
			root.appendChild(eSettings);
		}
		return 0;
	}

}
