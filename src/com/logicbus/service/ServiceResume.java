package com.logicbus.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.QueuedServantFactory;
import com.logicbus.backend.QueuedServantPool;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.ServantStat;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServantManager;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 恢复服务
 * 
 * <br>
 * 将已经暂停的服务恢复,恢复成功的服务将对外服务.<br>
 * 
 * 实现了一个内部核心服务，定义在/com/logicbus/service/servant.xml中，具体配置如下:<br>
 * 
 * {@code 
 * <service 
 * id="ServiceResume" 
 * module="com.logicbus.service.ServiceResume" 
 * name="ServiceResume" 
 * note="恢复服务"
 * visible="protected"
 * />	
 * }
 * 
 * <br>
 * 本服务需要客户端传送参数，包括：<br>
 * - service 要查询的服务ID,本参数属于必选项<br>
 * 
 * 本服务属于系统核心管理服务，内置了快捷访问，在其他服务的URL中加上resume参数即可直接访问，
 * 例如：要暂停/demo/logicbus/Helloworld服务，可输入URL为：<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/demo/logicbus/Helloworld?resume
 * }
 * <br>
 * 如果配置在服务器中，访问地址为：<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/core/ServiceResume?service=[服务ID] 
 * }
 * 
 * @author duanyy
 *
 */
public class ServiceResume extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		String id = ctx.GetValue("service", "");
		if (id == null || id.length() <= 0) {
			throw new ServantException("client.args_not_found",
					"Can not find parameter:service");
		}	
		ServantManager sm = ServantManager.get();
		Path path = new Path(id);
		ServiceDescription sd = sm.get(path);
		if (sd == null){
			throw new ServantException("user.data_not_found","Service does not exist:" + id);
		}
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);	
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		Element service = doc.createElement("service");			
		sd.toXML(service);		
		QueuedServantFactory sf = QueuedServantFactory.get();
		QueuedServantPool pool = sf.getPool(path);
		if (pool == null) {
			// 没有找到相应的pool，应该是该服务没有一次调用
		} else {
			pool.resume();
			ServantStat ss = pool.getStat();
			ss.toXML(service);
		}
		root.appendChild(service);
		return 0;
	}

}
