package com.logicbus.manager.monitor.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.manager.monitor.Cell;
import com.logicbus.manager.monitor.CellManager;

public class CellQuery extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		String cellId = ctx.GetValue("cell", "");
		if (cellId == null || cellId.length() <= 0) {
			throw new ServantException("client.args_not_found",
					"Can not find parameter:cell");
		}

		CellManager cm = CellManager.get();
		Cell cell = cm.get(cellId);
		if (cell == null) {
			throw new ServantException("client.result_not_found",
					"Can not find a Cell named " + cellId);
		}
		XMLMessage msg = msgDoc.asXML();
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();

		Element eCell = doc.createElement("cell");
		cell.toXML(eCell);
		root.appendChild(eCell);
		return 0;
	}

}
