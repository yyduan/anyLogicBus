package com.logicbus.manager.monitor.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Settings;
import com.anysoft.util.SystemStatus;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantFactory;
import com.logicbus.backend.ServantPool;
import com.logicbus.backend.ServantStat;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 查询工作节点的信息
 * @author duanyy
 *
 */
public class NodeQuery extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = msgDoc.asXML();		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		//创建节点
		Element location = doc.createElement("cell");
		Settings settings = Settings.get();
		
		String host = settings.GetValue("server.host", "0");
		String port = settings.GetValue("server.port", "0");
		location.setAttribute("id", host + ":" + port);
		location.setAttribute("host", host);
		location.setAttribute("port", port);	

		{
			//创建runtime
			Element eRuntime = doc.createElement("runtime");
			SystemStatus status = new SystemStatus();
			eRuntime.setAttribute("freemem", String.valueOf(status.getFreeMem()));
			eRuntime.setAttribute("totalmem", String.valueOf(status.getTotalMem()));
			eRuntime.setAttribute("maxmem", String.valueOf(status.getMaxMem()));
			eRuntime.setAttribute("processorcnt", String.valueOf(status.getProcessorCount()));
			eRuntime.setAttribute("threadcnt", String.valueOf(status.getThreadCount()));
			eRuntime.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
			location.appendChild(eRuntime);
		}
		{
			ServantFactory sf = ServantFactory.get();
			ServantPool [] pools = sf.getPools();
			if (pools.length > 0){
				Element eServices = doc.createElement("assets");
				for (ServantPool pool:pools){	
					Element eService = doc.createElement("asset");
					ServiceDescription sd = pool.getDescription();
					sd.toXML(eService);
					ServantStat ss = pool.getStat();
					ss.toXML(eService);
					eServices.appendChild(eService);
				}
				location.appendChild(eServices);
			}
		}
		root.appendChild(location);
		return 0;

	}

}
