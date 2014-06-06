package com.logicbus.together;

import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.cache.Cachable;
import com.anysoft.cache.CacheManager;
import com.anysoft.cache.ChangeAware;
import com.anysoft.cache.Provider;
import com.anysoft.util.IOTools;
import com.anysoft.util.JsonTools;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 可缓存的XML资源
 * 
 * <br>
 * XML资源按照URI进行缓存
 * 
 * @author duanyy
 * 
 * @since 1.1.0
 * 
 */
public class XmlResource implements Cachable{
	
	/** 
	 * a logger of log4j
	 * 
	 */
	protected static Logger logger = LogManager.getLogger(XmlResourceProvider.class);
		
	protected String xmlURI;
	
	protected Document xmlDoc = null;
	
	public XmlResource(String id,Document doc){
		xmlURI = id;
		xmlDoc = doc;
	}
	
	public Document getDocument(){return xmlDoc;}
	
	@Override
	public void toXML(Element e) {
		e.setAttribute("uri", xmlURI);
	}

	@Override
	public void fromXML(Element e) {
		xmlURI = e.getAttribute("uri");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void toJson(Map json) {
		JsonTools.setString(json, "uri", xmlURI);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map json) {
		xmlURI = JsonTools.getString(json, "uri", "");
	}

	@Override
	public String getId() {
		return xmlURI;
	}

	@Override
	public boolean isExpired() {
		return false;
	}
	/**
	 * XML资源管理器
	 * 
	 * @author duanyy
	 *
	 */
	public static class Manager extends CacheManager<XmlResource> {
		public Manager(){
			super(new XmlResourceProvider());
		}

	}

	/**
	 * XML资源提供者
	 * 
	 * @author duanyy
	 *
	 */
	public static class XmlResourceProvider implements Provider<XmlResource> {		
		@Override
		public XmlResource load(String id) {
			Settings profile = Settings.get();
			ResourceFactory rm = (ResourceFactory) profile.get("ResourceFactory");
			if (null == rm){
				rm = new ResourceFactory();
			}
			
			Document doc = null;
			InputStream in = null;
			try {
				in = rm.load(id, null);
				doc = XmlTools.loadFromInputStream(in);
				if (doc != null){
					return new XmlResource(id,doc);
				}
			} catch (Exception ex){
				logger.error("Error occurs when load xml file,source=" + id, ex);
			}finally {
				IOTools.closeStream(in);
			}
			return null;
		}

		@Override
		public void addChangeListener(ChangeAware<XmlResource> listener) {
			// do nothing
		}

	}
	
	
	public static void main(String [] args){
		Manager xrcm = new Manager();
		
		XmlResource xrc = xrcm.get("java:///com/logicbus/together/Demo.xml#com.logicbus.together.XmlResourceManager");
		
		if (xrc == null){
			logger.error("Can not load xrc..");
			return ;
		}
		
		Document doc = xrc.getDocument();
		
		try {
			Logiclet logiclet = Compiler.compile(doc.getDocumentElement(), Settings.get(),null);
			
			if (logiclet == null){
				logger.error("Can not compile the document.");
			}
			
			//新的文档
			Document result = XmlTools.newDocument("root");
			
			Element target = result.getDocumentElement();
			
			logiclet.execute(target, null, null,null);
			
			XmlTools.saveToOutputStream(result, System.out);
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
