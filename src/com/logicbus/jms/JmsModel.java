package com.logicbus.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.cache.SimpleModel;
import com.anysoft.util.XmlTools;

/**
 * Jms配置模型
 * @author duanyy
 *
 */
public class JmsModel extends SimpleModel{

	public JmsModel(String _id) {
		super(_id);
	}
	
	protected HashMap<String,SimpleModel> destinations = new HashMap<String,SimpleModel>();
	
	public SimpleModel getDestination(String id){
		return destinations.get(id);
	}
	
	
	@Override
	public void fromXML(Element root) {
		super.fromXML(root);
		
		NodeList nodeList = XmlTools.getNodeListByPath(root, "destination");
		if (nodeList != null && nodeList.getLength() > 0){
			
			for (int i = 0,length = nodeList.getLength() ; i < length ; i ++){
				Node n = nodeList.item(i);
				
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				
				Element e = (Element)n;
				
				String id = e.getAttribute("id");
				if (id == null || id.length() <= 0){
					continue;
				}
				
				SimpleModel model = new SimpleModel(id);
				
				model.fromXML(e);
				
				destinations.put(id, model);
			}
		}
	}

	@Override
	public void toXML(Element root) {
		super.toXML(root);
		
		Collection<SimpleModel> models = destinations.values();
		
		Document doc = root.getOwnerDocument();
		
		for (SimpleModel model:models){
			Element eDest = doc.createElement("destination");
			
			model.toXML(eDest);
			
			root.appendChild(eDest);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map root) {
		super.fromJson(root);
		
		Object _fieldsObject = root.get("destinations");
		if (_fieldsObject != null && _fieldsObject instanceof List){
			for (Object _fieldObject:(List)_fieldsObject){
				if (!(_fieldObject instanceof Map)){
					continue;
				}
				
				Map _data = (Map)_fieldObject;
				String id = (String)_data.get("id");
				if (id == null || id.length() <= 0){
					continue;
				}
				
				SimpleModel model = new SimpleModel(id);
				
				model.fromJson(root);
				
				destinations.put(id, model);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void toJson(Map root) {
		super.toJson(root);
		
		Collection<SimpleModel> models = destinations.values();
		
		if (models.size() > 0){
			List list = new ArrayList();
			for (SimpleModel model:models){
				Map mapModel = new HashMap();
				
				model.toJson(mapModel);
				
				list.add(mapModel);
			}
			root.put("destinations", list);
		}
	}	
}