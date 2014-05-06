package com.logicbus.jms;

import java.io.InputStream;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.CommandLine;
import com.anysoft.util.IOTools;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;


/**
 * 缺省的ContextFactory
 * 
 * @author duanyy
 *
 */
public class DefaultContextFactory implements JmsContextFactory {
	
	protected static Logger logger = LogManager.getLogger(DefaultContextFactory.class);
	
	@Override
	public JmsContext getContext(String id)  throws JMSException {
		if (!loaded){
			loadResource();
			loaded = true;
		}
		
		JmsModel model = models.get(id);
		
		if (model == null){
			throw new JMSException("Can not find a jms model named " + id);
		}
		
		return new DefaultJmsContext(model);
	}

	private void loadResource() {
		Settings settings = Settings.get();
		
		String xrcURI = settings.GetValue("jms.master","java:///com/logicbus/jms/jms.xml#com.logicbus.jms.DefaultContextFactory");
		String xrcURI2 = settings.GetValue("jms.secondary","java:///com/logicbus/jms/jms.xml#com.logicbus.jms.DefaultContextFactory");
		
		Document doc = loadDocument(xrcURI,xrcURI2);
		
		if (doc != null){
			NodeList nodeList = XmlTools.getNodeListByPath(doc.getDocumentElement(),"context");
			
			
			for (int i = 0 ,length = nodeList.getLength() ; i < length ; i ++){
				Node n = nodeList.item(i);
				
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				
				Element e = (Element)n;
				
				String id = e.getAttribute("id");
				if (id == null || id.length() <= 0){
					continue;
				}
				
				JmsModel model = new JmsModel(id);
				
				model.fromXML(e);
				
				models.put(id, model);
			}
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
	
	/**
	 * 是否已经装入过资源
	 */
	protected boolean loaded = false;
	
	/**
	 * 配置列表，以hashtable形式存储
	 */
	protected HashMap<String,JmsModel> models = new HashMap<String,JmsModel>();
	
	public static void main(String [] args){
		CommandLine cmd = new CommandLine(args);
		Settings settings = Settings.get();
		settings.addSettings(cmd);
		
		JmsContext context = null;	
		
		try {
			context = JmsContextFactory.TheFactory.getContext("Default");
			
			context.open();			
			
			JmsDestination queue = context.getDestination("Default");			
			queue.send(new MsgProvider(){

				@Override
				public Message[] message(Session session)
						throws Exception {
					Message [] msgs = new Message[5];
					for (int i = 0 ; i < 5 ; i ++){
						Message msg = session.createTextMessage("Helloworld " + i);
						msgs[i] = msg;
					}
					return msgs;
				}
				
			});					
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			context.close();
		}
		
		
		try {
			context.open();
			
			JmsDestination queue = context.getDestination("Default");
			
			queue.receive(new MsgHandler(){

				@Override
				public void message(Message msg) throws Exception {
					System.out.println(msg);
				}
				
			},5000);
						
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			context.close();
		}	
	}
}
