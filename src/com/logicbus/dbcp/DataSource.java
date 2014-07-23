package com.logicbus.dbcp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.Factory;
import com.anysoft.util.IOTools;	
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 数据源
 * 
 * <br>
 * 针对数据库的数据源
 * 
 * @author duanyy
 * @since 1.2.5
 */
public class DataSource {
	protected static final Logger logger = LogManager.getLogger(DataSource.class);
	/**
	 * 获取数据库连接池
	 * @param name 名称
	 * @return
	 */
	public ConnectionPool getPool(String name){
		ConnectionPool found = pools.get(name);
		if (found != null){
			return found;
		}
		
		synchronized (lock){
			for (ConnectionPoolFactory f:factories){
				ConnectionPool newPool = f.getPool(name);
				if (newPool != null){
					pools.put(name, newPool);
					return newPool;
				}
			}
			return null;
		}
	}
	
	protected Hashtable<String,ConnectionPool> pools = 
			new Hashtable<String,ConnectionPool>();
	
	/**
	 * 唯一实例
	 */
	protected static DataSource instance = null;
	
	/**
	 * 锁
	 */
	protected static Object lock = new Object();
	
	/**
	 * 获取实例
	 * @return
	 */
	public static DataSource get(){
		if (instance == null){
			synchronized (lock){
				if (instance == null){
					instance = new DataSource();
					instance.reload(Settings.get());
				}
			}	
		}
		return instance;
	}

	public void reload(Properties props){						
		String configFile = props.GetValue("dbcp.ds.master", 
				"java:///com/logicbus/dbcp/ds.xml#com.logicbus.dbcp.DataSource");

		String secondaryFile = props.GetValue("dbcp.ds.secondary", 
				"java:///com/logicbus/dbcp/ds.xml#com.logicbus.dbcp.DataSource");
		
		Settings profile = Settings.get();
		ResourceFactory rm = (ResourceFactory) profile.get("ResourceFactory");
		if (null == rm){
			rm = new ResourceFactory();
		}
		
		InputStream in = null;
		try {
			in = rm.load(configFile,secondaryFile, null);
			Document doc = XmlTools.loadFromInputStream(in);
			if (doc != null){
				loadConfig(doc.getDocumentElement(),props);
			}
		} catch (Exception ex){
			logger.error("Error occurs when load xml file,source=" + configFile, ex);
		}finally {
			IOTools.closeStream(in);
		}
	}

	private void loadConfig(Element root,Properties props) {		
		TheFactory factory = new TheFactory();
		
		factories.clear();
		
		NodeList children = XmlTools.getNodeListByPath(root, "ds");
		for (int i = 0; i < children.getLength() ; i++){
			Node item = children.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			Element e = (Element)item;			
			try {
				ConnectionPoolFactory cpf = factory.newInstance(e, props);
				factories.add(cpf);
			}catch (Exception ex){
				logger.error(ex.getMessage(),ex);
			}
		}
	}
	
	protected List<ConnectionPoolFactory> factories = 
			new ArrayList<ConnectionPoolFactory>();
	
	public static class TheFactory extends Factory<ConnectionPoolFactory>{
		
	}
}

