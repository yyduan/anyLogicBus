package com.logicbus.manager.monitor.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.manager.monitor.Asset;
import com.logicbus.manager.monitor.AssetManager;

public class AssetQuery extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		String assetId = ctx.GetValue("asset", "");
		if (assetId == null || assetId.length() <= 0) {
			throw new ServantException("client.args_not_found",
					"Can not find parameter:asset");
		}

		AssetManager cm = AssetManager.get();
		Asset asset = cm.get(assetId);
		if (asset == null) {
			throw new ServantException("client.result_not_found",
					"Can not find an asset named " + assetId);
		}
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();

		Element eAsset = doc.createElement("asset");
		asset.toXML(eAsset);
		root.appendChild(eAsset);
		return 0;
	}

}
