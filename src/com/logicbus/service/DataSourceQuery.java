package com.logicbus.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.datasource.ConnectionFactory;
import com.logicbus.datasource.ConnectionPool;
import com.logicbus.datasource.ConnectionPoolFactory;
import com.logicbus.datasource.DataSourceStat;
import com.logicbus.datasource.NamedDataSource;

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
 */
public class DataSourceQuery extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {				
		XMLMessage msg = (XMLMessage) msgDoc.asMessage(XMLMessage.class);
		
		String name = getArgument("name", msgDoc, ctx);
		
		ConnectionPool cp = ConnectionPoolFactory.getPool();
		
		NamedDataSource ds = cp.getDataSource(name);
		
		if (ds == null){
			throw new ServantException("user.data_not_found","Can not find a datasource named " + name);
		}
		
		Element root = msg.getRoot();
		
		Document doc = msg.getDocument();
		
		Element dsElem = doc.createElement("datasource");
		
		ConnectionFactory cf = ds.getFactory();
		
		cf.report(dsElem);
		
		DataSourceStat stat = ds.getStat();
		
		stat.toXML(dsElem);
		
		root.appendChild(dsElem);
		
		return 0;
	}

}
