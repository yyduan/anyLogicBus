package com.logicbus.manager.monitor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.BaseException;
import com.anysoft.util.Factory;
import com.anysoft.util.Manager;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.XMLConfigurable;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;


public class Cell extends MetricHolder implements XMLConfigurable,CopyHolder{
	protected static Logger logger = LogManager.getLogger(Cell.class);
	protected String id;
	public String getID(){return id;}
	
	protected String checkUrl;
	public String getCheckUrl(){return checkUrl;}
	
	protected long ttl = 3600;
	public long getTTL(){return ttl;}

	public enum STATE {VALID,ERROR,DEAD}; 
	public STATE state = STATE.VALID;
	public void setState(STATE _state){
		state = _state;
	}
	protected String copyMetrics;
	public boolean isDead() {
		return System.currentTimeMillis() - getLastUpdated() > ttl;
	}	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		// TODO Auto-generated method stub
		XmlElementProperties props = new XmlElementProperties(_e,_properties);
		id = PropertiesConstants.getString(props, "id", "unknown");
		ttl = PropertiesConstants.getLong(props, "ttl", ttl);
		checkUrl = PropertiesConstants.getString(props, "checkUrl", "http://${ip}:${port}//logicbus/services/core/manager/NodeQuery");
		
		String metrics = props
				.GetValue(
						"monitor.cell.metrics",
						"freemem=AVG:60:720,AVG:3600:720,AVG:43200:720,AVG:86400:720,AVG:604800:720;"
								+ "maxmem=AVG:60:720,AVG:3600:720,AVG:43200:720,AVG:86400:720,AVG:604800:720;"
								+ "totalmem=AVG:60:720,AVG:3600:720,AVG:43200:720,AVG:86400:720,AVG:604800:720;"
								+ "threadcnt=AVG:60:720,AVG:3600:720,AVG:43200:720,AVG:86400:720,AVG:604800:720;"
								+ "processorcnt=AVG:60:720,AVG:3600:720,AVG:43200:720,AVG:86400:720,AVG:604800:720");
		
		setMetrics(metrics);
		
		copyMetrics = props
				.GetValue(
						"monitor.cell.metrics",
						"times=SUM:60:720,SUM:3600:720,SUM:43200:720,SUM:86400:720,SUM:604800:720;"
								+ "duration=SUM:60:720,SUM:3600:720,SUM:43200:720,SUM:86400:720,SUM:604800:720;");
	}

	public void update(Document doc,AssetManager assetHolder){
		if (assetHolder == null){
			return ;
		}
		
		Element e = XmlTools.getFirstElementByPath(doc.getDocumentElement(), "cell");
		update(e,assetHolder);
	}

	public void update(Element root,AssetManager assetHolder){	
		update(root);
		// 处理服务
		NodeList eAssets = XmlTools.getNodeListByPath(root, "assets/asset");
		for (int i = 0; i < eAssets.getLength(); i++) {
			Node n = eAssets.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element e = (Element) n;
			if (!e.getNodeName().equals("asset")) {
				continue;
			}
			assetFound(e, assetHolder);
		}
	}

	synchronized private void assetFound(Element e, AssetManager assetHolder) {
		// TODO Auto-generated method stub
		String id = e.getAttribute("id");
		String path = e.getAttribute("path");
		if (id != null && path != null && id.length() > 0 && path.length() > 0) {
			String key = path + "/" + id;
			Asset asset = assetHolder.get(key);
			if (asset == null) {
				logger.info("Found new asset,id=" + key);
				asset = assetHolder.create(e);
				assetHolder.add(key,asset);
			}
			asset.update(e);

			Copy copy = getCopy(key);
			if (copy == null){
				logger.info("Found new copy,id=" + key + ",at " + getID());
				copy = new Copy(this,asset,copyMetrics);
				
				addCopy(key, copy);
				asset.addCopy(getID(), copy);
			}else{
				Copy found = asset.getCopy(getID());
				if (found == null){
					asset.addCopy(getID(), copy);
				}
			}
			copy.update(e);
		}
	}

	private Manager<Copy> copys = new Manager<Copy>();
	@Override
	public Copy getCopy(String id) {
		return copys.get(id);
	}
	@Override
	public void addCopy(String id, Copy copy) {
		copys.add(id, copy);
	}	
	
	public static class TheFactory extends Factory<Cell>{
		public TheFactory() {
			super(Cell.class.getClassLoader());
		}
		public String getClassName(String _module) throws BaseException{
			return "com.logicbus.manager.monitor.Cell";
		}				
	}

	public void toXML(Element root){
		Document doc = root.getOwnerDocument();
		root.setAttribute("id", getID());
		root.setAttribute("ttl", String.valueOf(getTTL()));
		root.setAttribute("checkUrl", getCheckUrl());
		
		super.toXML(root);
		
		String [] keys = copys.keys();
		if (keys.length > 0){
			Element eCopys = doc.createElement("copys");
			for (String key:keys){
				Copy copy = getCopy(key);
				if (copy != null){
					Element eCopy = doc.createElement("copy");
					copy.toXML(eCopy,"cell");
					eCopys.appendChild(eCopy);
				}
			}
			root.appendChild(eCopys);
		}
	}
}
