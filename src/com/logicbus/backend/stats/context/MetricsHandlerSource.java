package com.logicbus.backend.stats.context;

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

import com.anysoft.util.BaseException;
import com.anysoft.util.Factory;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.Watcher;
import com.anysoft.util.WatcherHub;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;
import com.logicbus.backend.stats.core.MetricsHandler;

/**
 * MetricsHandler的Source
 * @author duanyy
 *
 */
public class MetricsHandlerSource implements MetricsHandlerContext,Watcher<MetricsHandler>{
	/**
	 * logger of log4j
	 */
	protected static final Logger logger = LogManager.getLogger(MetricsHandlerSource.class);
	
	protected WatcherHub<MetricsHandler> watcherHub = new WatcherHub<MetricsHandler>();
	
	/**
	 * 缓存的对象
	 */
	protected Hashtable<String,MetricsHandler> caches = new Hashtable<String,MetricsHandler>();

	/**
	 * 配置来源
	 */
	protected List<MetricsHandlerContext> sources = new ArrayList<MetricsHandlerContext>();
	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		Properties p = new XmlElementProperties(_e,_properties);
		
		NodeList children = XmlTools.getNodeListByPath(_e, "context");
				
		for (int i = 0 ; i < children.getLength() ; i ++){
			Node n = children.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element e = (Element)n;
			
			try {
				MetricsHandlerContext source = factory.newInstance(e, p,"module",Inner.class.getName());
				if (source != null){
					source.addWatcher(this);
					sources.add(source);
				}
			}catch (Exception ex){
				logger.error("Can not create RedisPoolSource instance,check your configuration.");
			}
		}
	}

	@Override
	public void close() throws Exception {
		caches.clear();
		
		for (MetricsHandlerContext s:sources){
			s.removeWatcher(this);
			IOTools.close(s);
		}
		
		sources.clear();
	}

	@Override
	public MetricsHandler getHandler(String id) {
		MetricsHandler found = caches.get(id);
		if (found == null){
			synchronized (caches){
				found = caches.get(id);
				if (found == null){
					found = loadHandler(id);
					if (found != null){
						caches.put(id, found);
					}
				}
			}
		}
		return found;
	}

	protected MetricsHandler loadHandler(String id) {
		for (MetricsHandlerContext s:sources){
			MetricsHandler found = s.getHandler(id);
			if (found != null){
				return found;
			}
		}
		return null;
	}

	@Override
	public void addWatcher(Watcher<MetricsHandler> watcher) {
		watcherHub.addWatcher(watcher);
	}

	@Override
	public void removeWatcher(Watcher<MetricsHandler> watcher) {
		watcherHub.removeWatcher(watcher);
	}

	@Override
	public void added(String id, MetricsHandler _data) {
		if (watcherHub != null){
			watcherHub.added(id, _data);
		}
	}

	@Override
	public void removed(String id, MetricsHandler _data) {
		caches.remove(id);
		if (watcherHub != null){
			watcherHub.removed(id, _data);
		}
	}

	@Override
	public void changed(String id, MetricsHandler _data) {
		caches.remove(id);
		if (watcherHub != null){
			watcherHub.changed(id, _data);
		}
	}
	
	public static class TheFactory extends Factory<MetricsHandlerContext>{
		
	}
	
	public static final TheFactory factory = new TheFactory();
	
	public static MetricsHandlerContext newInstance(Element doc,Properties p){
		if (doc == null) return null;
		return factory.newInstance(doc, p);
	}
	
	public static MetricsHandlerSource theInstance = null;
	public static MetricsHandlerSource get(){
		if (theInstance != null){
			return theInstance;
		}
		
		synchronized (factory){
			if (theInstance == null){
				theInstance = (MetricsHandlerSource)newInstance(Settings.get(), new MetricsHandlerSource());
			}
		}
		
		return theInstance;
	}
	
	protected static MetricsHandlerContext newInstance(Properties p,MetricsHandlerContext instance){
		String configFile = p.GetValue("metrics.server.master", 
				"java:///com/logicbus/backend/stats/metrics.server.xml#com.logicbus.backend.stats.MetricsHandlerSource");

		String secondaryFile = p.GetValue("metrics.server.secondary", 
				"java:///com/logicbus/backend/stats/metrics.server.xml#com.logicbus.backend.stats.MetricsHandlerSource");
		
		ResourceFactory rm = Settings.getResourceFactory();
		InputStream in = null;
		try {
			in = rm.load(configFile,secondaryFile, null);
			Document doc = XmlTools.loadFromInputStream(in);
			if (doc != null){
				if (instance == null){
					return newInstance(doc.getDocumentElement(),p);
				}else{
					instance.configure(doc.getDocumentElement(), p);
					return instance;
				}
			}
		} catch (Exception ex){
			logger.error("Error occurs when load xml file,source=" + configFile, ex);
		}finally {
			IOTools.closeStream(in);
		}
		return null;
	}
}
