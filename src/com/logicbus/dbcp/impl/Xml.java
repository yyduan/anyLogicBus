package com.logicbus.dbcp.impl;

import java.io.InputStream;
import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.cache.ChangeAware;
import com.anysoft.util.BaseException;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 基于XmlResource的Provider
 * 
 * @author duanyy
 *
 * @since 1.2.5
 * 
 * @version 1.2.5.3 [20140731 duanyy]
 * -  基础包的Cacheable接口修改 
 */
public class Xml implements DBCMProvider {
	protected static final Logger logger = LogManager.getLogger(Xml.class);
	@Override
	public ConnectionModel load(String id) {
		return models.get(id);
	}

	@Override
	public void addChangeListener(ChangeAware<ConnectionModel> listener) {
	}
	@Override
	public void removeChangeListener(ChangeAware<ConnectionModel> listener) {
		
	}	

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		XmlElementProperties p = new XmlElementProperties(_e,_properties);
		
		String configFile = p.GetValue("xrc.master", "${dbcp.xml.master}");
		configFile = configFile != null && configFile.length() > 0 ?
				configFile : "java:///com/logicbus/dbcp/dbcp.xml#com.logicbus.dbcp.DataSource";
		
		String secondaryConfigFile = p.GetValue("xrc.secondary","${dbcp.xml.secondary}");
		secondaryConfigFile = secondaryConfigFile != null && secondaryConfigFile.length() > 0 ?
				secondaryConfigFile : "java:///com/logicbus/dbcp/dbcp.xml#com.logicbus.dbcp.DataSource";
		
		Document doc = loadDocument(configFile,secondaryConfigFile);		
		load(doc.getDocumentElement());
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


	@Override
	public ConnectionModel load(String id, boolean cacheAllowed) {
		return load(id);
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
	
	/**
	 * 从主/备地址中装入文档
	 * 
	 * @param master 主地址
	 * @param secondary 备用地址
	 * @return XML文档
	 */
	protected static Document loadDocument(String master,String secondary){
		Settings profile = Settings.get();
		ResourceFactory rm = (ResourceFactory) profile.get("ResourceFactory");
		if (null == rm){
			rm = new ResourceFactory();
		}
		
		Document ret = null;
		InputStream in = null;
		try {
			in = rm.load(master,secondary, null);
			ret = XmlTools.loadFromInputStream(in);		
		} catch (Exception ex){
			logger.error("Error occurs when load xml file,source=" + master, ex);
		}finally {
			IOTools.closeStream(in);
		}		
		return ret;
	}
	
}
