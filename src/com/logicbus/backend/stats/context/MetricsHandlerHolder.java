package com.logicbus.backend.stats.context;

import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.stream.Handler;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.XMLConfigurable;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;
import com.logicbus.backend.stats.core.Fragment;
import com.logicbus.backend.stats.core.MetricsHandler;

public class MetricsHandlerHolder implements XMLConfigurable,AutoCloseable {

	protected Hashtable<String,MetricsHandler> pools = new Hashtable<String,MetricsHandler>();
	
	protected final static Logger logger = LogManager.getLogger(MetricsHandlerHolder.class);
	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		XmlElementProperties p = new XmlElementProperties(_e,_properties);
		
		NodeList rcps = XmlTools.getNodeListByPath(_e, "handler");
		
		for (int i = 0 ; i < rcps.getLength() ; i ++){
			Node n = rcps.item(i);
			
			if (n.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element e = (Element)n;
			
			try {
				MetricsHandler handler = MetricsHandler.TheFactory.getInstance(e, p);
				if (handler != null){
					pools.put(handler.getId(), handler);
				}
			}catch (Exception ex){
				logger.warn("Can not create MetricsHandler instance,check your xml configurations.");
			}
			
		}
	}

	@Override
	public void close() throws Exception {
		Collection<MetricsHandler> values = pools.values();
		
		for (Handler<Fragment> p:values){
			p.close();
		}
		pools.clear();
	}
	
	public MetricsHandler getPool(String id) {
		return pools.get(id);
	}	
	
}
