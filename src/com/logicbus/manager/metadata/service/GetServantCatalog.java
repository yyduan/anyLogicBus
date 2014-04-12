package com.logicbus.manager.metadata.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.catalog.CatalogNode;
import com.logicbus.models.servant.ServantCatalog;
import com.logicbus.models.servant.ServantCatalogNode;
import com.logicbus.models.servant.ServantManager;
import com.logicbus.models.servant.impl.ServantCatalogNodeImpl;

/**
 * 获取当前服务目录结构
 * 
 * <br>
 * 实现了一个内部核心服务,定义在/com/logicbus/service/servant.xml中,具体配置如下:
 * {@code 
 * <service 
 * id="GetServantCatalog" 
 * name="GetServantCatalog" 
 * note="获取当前的服务目录"
 * visible="protected"
 * module="com.logicbus.manager.metadata.service.GetServantCatalog"
 * />
 * }
 * 
 * 
 * 如果配置在服务器中,访问地址为:<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/core/manager/GetServantCatalog
 * }
 * 
 * @author duanyy
 * 
 *
 */

public class GetServantCatalog extends Servant {

	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		ServantManager sm = ServantManager.get();
		ServantCatalog catalog[] = sm.getServantCatalog();
		
		for (int i = 0 ; i < catalog.length ; i ++){
			Element catalogElem = doc.createElement("catalog");
			
			ServantCatalogNode node = (ServantCatalogNode) catalog[i].getRoot();
			if (node != null){
				outputCatalog(catalog[i],node,catalogElem);
			}
			root.appendChild(catalogElem);
		}
		return 0;
	}
	
	protected void outputCatalog(ServantCatalog catalog,ServantCatalogNode root,Element e){
		((ServantCatalogNodeImpl)root).toXML(e,false);
		Document doc = e.getOwnerDocument();
		CatalogNode [] children = catalog.getChildren(root);
		if (children == null)
			return ;
		for (int i = 0 ; i < children.length ; i ++){
			Element _e = doc.createElement("catalog");
			outputCatalog(catalog,(ServantCatalogNode)children[i],_e);
			e.appendChild(_e);
		}
	}

}
