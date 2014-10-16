package com.logicbus.remote.impl.http;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.JsonSerializer;
import com.logicbus.remote.core.Builder;
import com.logicbus.remote.core.Parameters;


/**
 * Http服务调用参数
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class HttpParameters extends DefaultProperties implements Parameters {
	protected static Object context = new Object();
	
	@Override
	public String getValue(String varName, Object context, String defaultValue) {
		return GetValue(varName, defaultValue);
	}

	@Override
	public Object getContext(String varName) {
		return context;
	}

	@Override
	public Parameters param(String id, String value) {
		SetValue(id, value);
		return this;
	}

	@Override
	public Parameters params(String... _params) {
		for (int i = 0 ; i < _params.length ; i = i + 2){
			if (i + 1 < _params.length){
				param(_params[i],_params[i + 1]);
			}else{
				param(_params[i],"");
			}
		}
		return this;
	}

	@Override
	public <data extends JsonSerializer> Parameters param(String id, data value) {
		if (value != null){
			Map<String,Object> json = new HashMap<String,Object>();
			value.toJson(json);
			jsonObjects.put(id, json);
		}
		return this;
	}

	@Override
	public <data> Parameters param(String id,data value,Builder<data> builder) {
		Object json = value;
		
		if (builder != null){
			json = builder.serialize(id, value);
		}
		
		if (json != null){
			jsonObjects.put(id, json);
		}
		return this;
	}

	@Override
	public Parameters clean(){
		jsonObjects.clear();
		return this;
	}
	
	protected Hashtable<String,Object> jsonObjects = new  Hashtable<String,Object>();

	@Override
	public Object getData(String id){
		return jsonObjects.get(id);
	}

}
