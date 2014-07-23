package com.logicbus.dbcp.impl;

import java.util.Hashtable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.XmlTools;
import com.logicbus.dbcp.ConnectionPool;
import com.logicbus.dbcp.ConnectionPoolFactory;


/**
 * 在DataSource配置文件内部实现的ConnectionPoolFactory
 * 
 * @author duanyy
 * 
 * @since 1.2.5
 *
 */
public class Inner implements ConnectionPoolFactory {

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		load(_e);
	}
	
	protected void load(Element root){
		NodeList dbcp = XmlTools.getNodeListByPath(root, "dbcp");
		
		for (int i = 0 ;i < dbcp.getLength() ; i ++){
			Node n = dbcp.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element e = (Element)n;
			ConnectionModel model = new ConnectionModel();
			model.fromXML(e);
			
			String name = model.getName();
			if (name == null || name.length() <= 0){
				continue;
			}
			
			addModel(name,model);
		}
	}
	public ConnectionPool getPool(String name) {
		ConnectionModel model = models.get(name);
		if (model != null){
			return new ConnectionPoolImpl(model);
		}
		return null;
	}

	/**
	 * 增加model
	 * @param name
	 * @param model
	 */
	public void addModel(String name,ConnectionModel model){
		models.put(name, model);
	}
	
	protected Hashtable<String,ConnectionModel> models = 
			new Hashtable<String,ConnectionModel>();
}
