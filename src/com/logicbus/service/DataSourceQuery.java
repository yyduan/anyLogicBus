package com.logicbus.service;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.AbstractServant;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.JsonMessage;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.DataSource;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 查询指定数据源的信息
 * 
 * <br>
 * 实现了一个内部核心服务，定义在/com/logicbus/service/servant.xml中，具体配置如下:<br>
 * 
 * {@code
 * <service 
 *     id="DataSourceQuery" 
 *     name="DataSourceQuery" 
 *     note="查询数据源信息"
 *     visible="protected"
 *     module="com.logicbus.service.DataSourceQuery"
 * />	
 * }
 * 
 * 本服务需要客户端传送参数，包括：<br>
 * - name 要查询数据源的名称,本参数属于必选项<br>
 * 
 * 如果配置在服务器中，访问地址为：<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/core/manager/DataSourceQuery?name=<名称>
 * }
 * 
 * @author duanyy
 * @since 1.0.6
 * @version 1.2.5 [20140723 duanyy]
 * - dbcp的实现更新
 * 
 */
public class DataSourceQuery extends AbstractServant {

	@Override
	protected void onDestroy() {
	}

	@Override
	protected void onCreate(ServiceDescription sd) throws ServantException {
	}

	@Override
	protected int onXml(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage) msgDoc.asMessage(XMLMessage.class);		
		String name = getArgument("name", msgDoc, ctx);	
		
		DataSource ds = DataSource.get();				
		ConnectionPool pool = ds.getPool(name);		
		if (pool == null){
			throw new ServantException("user.data_not_found","Can not find a connection pool named " + name);
		}
		
		Element root = msg.getRoot();		
		Document doc = msg.getDocument();		
		Element dbcp = doc.createElement("dbcp");
		
		pool.report(dbcp);
		
		root.appendChild(dbcp);
		
		return 0;
	}

	@Override
	protected int onJson(MessageDoc msgDoc, Context ctx) throws Exception {
		JsonMessage msg = (JsonMessage) msgDoc.asMessage(JsonMessage.class);
		String name = getArgument("name",msgDoc,ctx);
		DataSource ds = DataSource.get();				
		ConnectionPool pool = ds.getPool(name);		
		if (pool == null){
			throw new ServantException("user.data_not_found","Can not find a connection pool named " + name);
		}
		
		Map<String,Object> root = msg.getRoot();		
		Map<String,Object> dbcp = new HashMap<String,Object>();
		
		pool.report(dbcp);
		
		root.put("dbcp", dbcp);
		return 0;
	}

}
