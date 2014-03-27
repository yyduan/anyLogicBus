package com.logicbus.models.servant;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlSerializer;
import com.anysoft.util.XmlTools;
import java.util.List;

/**
 * 服务描述
 * 
 * @author duanyy
 *
 */
public class ServiceDescription implements XmlSerializer{
	/**
	 * 服务ID
	 */
	private String m_service_id;
	/**
	 * 服务名称
	 */
	private String m_name = "";
	/**
	 * 说明
	 */
	private String m_note = "";
	/**
	 * module
	 */
	private String m_module = "";
	/**
	 * 服务路径
	 */
	private String m_path = "";
	/**
	 * 服务参数
	 */
	private Properties m_properties;
	/**
	 * 服务的可见性(public,login,limited)
	 */
	private String visible = "public";
	
	/**
	 * constructor
	 * @param id 服务ID
	 */
	public ServiceDescription(String id){
		m_service_id = id;
		m_properties = new DefaultProperties("Default",Settings.get());
	}
	
	/**
	 * 获得服务ID
	 * @return 服务ID
	 */
	public String getServiceID(){return m_service_id;}
	
	/**
	 * 设置服务ID
	 * @param id 服务ID
	 */
	public void setServiceID(String id){m_service_id = id;}
	
	/**
	 * 获得服务的可见性
	 * @return 可见性
	 */
	public String getVisible(){return visible;}
	
	/**
	 * 设置服务的可见性
	 * @param _visible
	 */
	public void setVisible(String _visible){visible = _visible;}
	
	/**
	 * 获得服务名称
	 * @return name
	 */
	public String getName(){return m_name;}
	
	/**
	 * 设置服务名称
	 * @param name name
	 */
	public void setName(String name){m_name = name;}
	
	/**
	 * 获取服务说明
	 * @return 服务说明
	 */
	public String getNote(){return m_note;}
	
	/**
	 * 设置服务说明
	 * @param note 服务说明
	 */
	public void setNote(String note){m_note = note;}
	
	/**
	 * 获得服务路径
	 * @return
	 */
	public String getPath(){return m_path;}
	
	/**
	 * 设置服务路径
	 * @param path 
	 */
	public void setPath(String path){m_path = path;}
	
	/**
	 * 获得服务实现代码
	 * @return
	 */
	public String getModule(){return m_module;}
	
	/**
	 * 设置服务实现代码
	 * @param module 
	 */
	public void setModule(String module){m_module = module;}
	
	/**
	 * 获取参数变量集
	 * @return
	 */
	public Properties getProperties(){return m_properties;}
	
	/**
	 * 服务所依赖的库文件
	 */
	protected Vector<String> modulesMaster = null;
	
	/**
	 * 获取服务以来库文件列表
	 * @return 
	 */
	public String [] getModules(){return modulesMaster == null ? null : modulesMaster.toArray(new String[0]);}
	
	/**
	 * 输出到打印流
	 * @param out
	 */
	public void List(PrintStream out)
	{
		out.println("Service ID:" + m_service_id);
		out.println("Name:" + m_name);
		out.println("Module:" + m_module);
		out.println("Note:" + m_note);
		
		DefaultProperties props = (DefaultProperties)m_properties;
		out.println("Parameters:");
		props.list(out);
	}
	
	@Override
	public void toXML(Element root){
		Document doc = root.getOwnerDocument();
			
		//id
		root.setAttribute("id",getServiceID());
		//name
		root.setAttribute("name", getName());
		//note
		root.setAttribute("note", getNote());
		//module
		root.setAttribute("module",getModule());
		//visible
		root.setAttribute("visible",getVisible());
		//path
		root.setAttribute("path",getPath());
		//Properties
		
		{
			DefaultProperties properties = (DefaultProperties) getProperties();
			Enumeration<?> __keys = properties.keys();
			if (__keys.hasMoreElements()){
				Element propertiesElem = doc.createElement("properties");
				while (__keys.hasMoreElements()){
					String __name = (String)__keys.nextElement();
					String __value = properties.GetValue(__name,"",false,true);
					
					Element e = doc.createElement("parameter");
					e.setAttribute("id",__name);
					e.setAttribute("value",__value);
					propertiesElem.appendChild(e);
				}
				root.appendChild(propertiesElem);
			}
		}
		if (modulesMaster != null && modulesMaster.size() > 0)
		{
			Element eModules = doc.createElement("modules");
			
			for (String module:modulesMaster){
				Element eModule = doc.createElement("module");
				eModule.setAttribute("url", module);
				eModules.appendChild(eModule);
			}
			
			root.appendChild(eModules);
		}

	}
	
	@Override
	public void fromXML(Element root){
		setServiceID(root.getAttribute("id"));
		setName(root.getAttribute("name"));
		setModule(root.getAttribute("module"));
		setNote(root.getAttribute("note"));
		setVisible(root.getAttribute("visible"));
		setPath(root.getAttribute("path"));
		
		NodeList eProperties = XmlTools.getNodeListByPath(root, "properties/parameter");
		if (eProperties != null){
			for (int i = 0 ; i < eProperties.getLength() ; i ++){
				Node n = eProperties.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				Element e = (Element)n;
				String _id = e.getAttribute("id");
				String _value = e.getAttribute("value");
				getProperties().SetValue(_id,_value);
			}
		}
		
		NodeList eModules = XmlTools.getNodeListByPath(root, "modules/module");
		if (eModules != null){
			if (modulesMaster == null){
				modulesMaster = new Vector<String>();
			}else{
				modulesMaster.clear();
			}
			
			for (int i = 0 ; i < eModules.getLength() ; i ++){
				Node n = eModules.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				Element e = (Element)n;
				String url = e.getAttribute("url");
				if (url != null && url.length() > 0){
					modulesMaster.add(url);
				}
			}
		}
	}
	
	/**
	 * 从JSON对象中读入
	 * @param json 
	 */
	@SuppressWarnings("rawtypes")
	public void fromJson(Map json){
		setServiceID((String)json.get("id"));
		setName((String)json.get("name"));
		setModule((String)json.get("module"));
		setNote((String)json.get("note"));
		setVisible((String)json.get("visible"));
		setPath((String)json.get("path"));
		
		Object propertiesObj = json.get("properties");
		if (propertiesObj != null && propertiesObj instanceof List){
			List propsList = (List)propertiesObj;
			for (Object para:propsList){
				if (!( para instanceof Map)){
					continue;
				}
				try {
					Map paraMap = (Map)para;
					String id = (String)paraMap.get("id");
					String value = (String)paraMap.get("value");
					
					if (id != null && value != null){
						getProperties().SetValue(id,value);
					}
				}catch (Exception ex){
					//如果类型转换错误,不管了
				}
			}
		}
		
		Object modulesObj = json.get("modules");
		if (modulesObj != null && modulesObj instanceof List){
			List modulesList = (List) modulesObj;
			for (Object module:modulesList){
				if (! (module instanceof String)){
					continue;
				}
				modulesMaster.add((String)module);
			}
		}
	}
	
	/**
	 * 写出到JSON对象
	 * @param json
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void toJson(Map json){
		json.put("type", "service");
		json.put("id", getServiceID());
		json.put("name",getName());
		json.put("module",getModule());
		json.put("note", getNote());
		json.put("visible", getVisible());
		json.put("path",getPath());
		
		{
			DefaultProperties properties = (DefaultProperties) getProperties();
			Enumeration<?> __keys = properties.keys();
			if (__keys.hasMoreElements()){
				
				List propertiesList = new Vector();				
				while (__keys.hasMoreElements()){
					String __name = (String)__keys.nextElement();
					String __value = properties.GetValue(__name,"",false,true);					
					Map pair = new HashMap();
					pair.put("id", __name);
					pair.put("value", __value);					
					propertiesList.add(pair);
				}
				
				json.put("properties", propertiesList);
			}
		}
		if (modulesMaster != null && modulesMaster.size() > 0)
		{
			List modulesList = new Vector();
			for (String module:modulesMaster){
				modulesList.add(module);
			}
			
			json.put("modules", modulesList);
		}		
	}
}
