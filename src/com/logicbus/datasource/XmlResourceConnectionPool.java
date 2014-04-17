package com.logicbus.datasource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.CommandLine;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 基于XML配置文件的ConnectorPool
 * 
 * @author duanyy
 * @since 1.0.6
 */
public class XmlResourceConnectionPool extends AbstractConnectionPool {
	/**
	 * a logger of log4j
	 */
	protected static final Logger logger = LogManager.getLogger(XmlResourceConnectionPool.class);
	
	public XmlResourceConnectionPool(Properties props) {
		super(props);
	}

	@Override
	protected void loadConfig(Properties props) {
		String configFile = props.GetValue("dbcp.master", "java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		String secondaryConfigFile = props.GetValue("dbcp.secondary", "java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		
		Document doc = loadDocument(configFile,secondaryConfigFile);		
		loadFromDocument(doc);
	}
	
	
	/**
	 * 从XML文档中装入配置
	 * @param doc XML配置文档
	 */
	protected void loadFromDocument(Document doc) {
		if (doc == null) return ;
		
		Element root = doc.getDocumentElement();
		
		NodeList nodeList = XmlTools.getNodeListByPath(root, "connection");
		
		for (int i = 0 ; i < nodeList.getLength() ; i ++){
			Node n = nodeList.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element e = (Element)n;
			ConnectionFactory cf = new ConnectionFactory();
			cf.fromXML(e);
			String name = cf.getName();
			if (name == null || name.length() <= 0){
				continue;
			}
			
			add(name,new NamedDataSource(cf));
		}
	}

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
	
	public static void main(String [] args){
		CommandLine cmd = new CommandLine(args);		
		Settings settings = Settings.get();
		
		settings.SetValue("dbcp.master","java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		settings.SetValue("dbcp.secondary","java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		
		settings.addSettings(cmd);
		
		final ConnectionPool pool = new XmlResourceConnectionPool(settings);
		final NamedDataSource ds = pool.getDataSource("Default");
		
		for (int j = 0 ; j < 500; j ++){
			(new Thread(){
				public void run(){
					Connection conn = null;
					Statement stmt = null;
					ResultSet rs = null;
					try {
						conn = ds.getConnection(3000);
						if (conn != null){
							
							stmt = conn.createStatement();
							
							rs = stmt.executeQuery("select * from user");
							
							ResultSetMetaData metadata = rs.getMetaData();
							
							while (rs.next()){
								for (int i = 1; i < metadata.getColumnCount() + 1; i ++){
									System.out.println(rs.getObject(i));
								}
							}
						}else{
							logger.error("Can not find a connection within 3 s");
						}
						
						DataSourceStat stat = ds.getStat();
						Document doc = XmlTools.newDocument("root");
						Element root = doc.getDocumentElement();
						
						stat.toXML(root);
						
						XmlTools.saveToOutputStream(root, System.out, false);
						
					}catch (Exception ex){
						ex.printStackTrace();
					}
					finally{
						SQLTools.close(rs,stmt,conn);
					}
				}
			}).start();
		}
	}
}
