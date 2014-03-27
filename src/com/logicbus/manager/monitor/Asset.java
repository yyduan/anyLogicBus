package com.logicbus.manager.monitor;


import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.BaseException;
import com.anysoft.util.Manager;
import com.anysoft.util.Properties;
import com.anysoft.util.XMLConfigurable;
import com.anysoft.util.XmlTools;


public class Asset extends MetricHolder implements XMLConfigurable,CopyHolder{
	protected String id;
	protected String path;
	
	public String getUniqueID(){return path + "/" + id;}
	
	private Manager<Copy> copys = new Manager<Copy>();
	@Override
	public Copy getCopy(String id) {
		return copys.get(id);
	}

	@Override
	public void addCopy(String id, Copy copy) {
		copys.add(id, copy);
	}

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		// TODO Auto-generated method stub
		id = _e.getAttribute("id");
		path = _e.getAttribute("path");

		String metrics = _properties
				.GetValue(
						"monitor.asset.metrics",
						"times=SUM:60:720,SUM:3600:720,SUM:43200:720,SUM:86400:720,SUM:604800:720;"
								+ "duration=SUM:60:720,SUM:3600:720,SUM:43200:720,SUM:86400:720,SUM:604800:720;");

		setMetrics(metrics);

		extractProperties(_e);
		
		Element props = XmlTools.getFirstElementByPath(_e, "properties");
		if (props != null){
			extractParameters(props);
		}
	}

	private void extractProperties(Element _e){
		NamedNodeMap attr = _e.getAttributes();
		for (int i = 0; i < attr.getLength(); i++) {
			Node n = attr.item(i);
			if (n.getNodeType() != Node.ATTRIBUTE_NODE)
				continue;
			Pair pair = new Pair();
			pair.id = n.getNodeName();
			pair.value = n.getNodeValue();
			properties.add(pair);
		}		
	}
	
	private void extractParameters(Element e){
		NodeList ps = e.getChildNodes();
		for (int j = 0 ; j < ps.getLength() ; j ++){
			Node node = ps.item(j);
			if (node.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			Element _e = (Element)node;
			if (!_e.getNodeName().equals("parameter")){
				continue;
			}
			Pair pair = new Pair();
			pair.id = _e.getAttribute("id");
			pair.value = _e.getAttribute("value");
			parameters.add(pair);
		}		
	}
	
	public void toXML(Element root){
		Document doc = root.getOwnerDocument();
		for (Pair pair:properties){
			root.setAttribute(pair.id, pair.value);
		}
		
		super.toXML(root);
		
		if (parameters.size() > 0){
			Element eProperties = doc.createElement("properties");
			
			for (Pair pair:parameters){
				Element eParameter = doc.createElement("parameter");
				
				eParameter.setAttribute("id", pair.id);
				eParameter.setAttribute("value", pair.value);
				
				eProperties.appendChild(eParameter);
			}
			
			root.appendChild(eProperties);
		}

		String [] keys = copys.keys();
		if (keys.length > 0){
			Element eCopys = doc.createElement("copys");
			for (String key:keys){
				Copy copy = getCopy(key);
				if (copy != null){
					Element eCopy = doc.createElement("copy");
					copy.toXML(eCopy,"asset");
					eCopys.appendChild(eCopy);
				}
			}
			root.appendChild(eCopys);
		}
	}
	
	protected Vector<Pair> properties = new Vector<Pair>();
	protected Vector<Pair> parameters = new Vector<Pair>();
	public static class Pair {
		public String id;
		public String value;
	}
}
