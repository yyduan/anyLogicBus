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
import com.logicbus.models.servant.impl.ServantCatalogNodeImpl;


/**
 * 获取指定服务目录节点下的服务信息列表
 * 
 * <br>
 * 实现了一个内部核心服务,定义在/com/logicbus/service/servant.xml中,具体配置如下:
 * {@code 
 * <service 
 * id="GetCatalogServices" 
 * name="GetCatalogServices" 
 * note="获取指定路径下的服务列表"
 * visible="protected"
 * module="com.logicbus.manager.metadata.service.GetCatalogServices"
 * />
 * }
 * 
 * <br>
 * 本服务需要客户端传送参数，包括：<br>
 * - path 要查询的服务目录节点的路径<br>
 * 
 * 如果配置在服务器中,访问地址为:<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/core/manager/GetCatalogServices?path=[路径]
 * }
 * 
 * @author duanyy
 * 
 *
 */
public class GetCatalogServices extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		String path = ctx.GetValue("path", "");
		if (path == null || path.length() <= 0) {
			throw new ServantException("client.args_not_found",
					"Can not find parameter:path");
		}

		ServantManager sm = ServantManager.get();
		ServantCatalogNodeImpl n = (ServantCatalogNodeImpl)sm.getCatalogNode(new Path(path));
		if (n == null) {
			throw new ServantException("user.data_not_found",
					"path does not exist:" + path);
		}
		
		XMLMessage msg = msgDoc.asXML();
		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		
		ServiceDescription [] sds = n.getServices();
		
		for (ServiceDescription sd:sds){
			Element eService = doc.createElement("service");
			sd.toXML(eService);
			root.appendChild(eService);
		}
		return 0;
	}

}
