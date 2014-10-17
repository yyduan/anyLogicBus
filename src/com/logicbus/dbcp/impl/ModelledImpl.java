package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;

/**
 * 基于ConnectionModel的实现
 * 
 * @author duanyy
 * 
 * @since 1.2.9.1
 *
 */
public class ModelledImpl extends AbstractConnectionPool{

	public ModelledImpl(ConnectionModel _model){
		model = _model;
		
		Properties props = new DefaultProperties();		
		props.SetValue(getIdOfMaxQueueLength(),	String.valueOf(model.getMaxActive()));
		props.SetValue(getIdOfIdleQueueLength(),String.valueOf(model.getMaxIdle()));

		create(props);
	}
	
	@Override
	public String getName() {
		return model.getName();
	}

	@Override
	public void report(Element xml) {
		if (xml != null){
			model.report(xml);
			
			Document doc = xml.getOwnerDocument();
			
			// runtime
			{
				Element _runtime = doc.createElement("runtime");
				super.report(_runtime);
				xml.appendChild(_runtime);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			model.report(json);
			
			// runtime
			{
				Map<String,Object> _runtime = new HashMap<String,Object>();
				super.report(_runtime);
				json.put("runtime", _runtime);
			}
		}
	}
	
	@Override
	protected int getMaxWait() {
		return model.getMaxWait();
	}

	@Override
	protected Connection createObject() throws BaseException {
		return model.newConnection();
	}
	
	protected ConnectionModel model;
}