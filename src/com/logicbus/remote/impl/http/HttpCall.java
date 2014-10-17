package com.logicbus.remote.impl.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.selector.FieldList;
import com.anysoft.selector.Selector;
import com.anysoft.util.BaseException;
import com.anysoft.util.Counter;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;
import com.logicbus.client.ClientException;
import com.logicbus.client.HttpClient;
import com.logicbus.client.JsonBuffer;
import com.logicbus.client.Parameter;
import com.logicbus.remote.core.Call;
import com.logicbus.remote.core.CallException;
import com.logicbus.remote.core.Parameters;
import com.logicbus.remote.core.Result;
import com.logicbus.remote.util.CallStat;


/**
 * 基于Http请求的实现
 * 
 * @author duanyy
 *
 * @since 1.2.9
 * 
 * @version 1.2.9.1 [20141017 duanyy]
 * - 实现Reportable接口
 * - 增加Counter模型
 */
public class HttpCall implements Call {
	protected static Logger logger = LogManager.getLogger(HttpCall.class);
	
	@Override
	public void close() throws Exception {
		// nothing to do
	}

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		Properties p = new XmlElementProperties(_e,_properties);
		
		uri = PropertiesConstants.getString(p, "uri", "");
		
		//queryParameters
		Element qp = XmlTools.getFirstElementByPath(_e, "request/query");
		if (qp != null){
			queryParameters = new FieldList();
			queryParameters.configure(qp, p);
		}
		
		//argument data
		Element ad = XmlTools.getFirstElementByPath(_e, "request/data");
		if (ad != null){
			arguments = new FieldList();
			arguments.configure(ad, p);
		}
		
		client = new HttpClient(p);
		
		stat = createCounter(p);
	}

	@Override
	public Parameters createParameter() {
		return new HttpParameters();
	}

	@Override
	public Result execute(Parameters paras) throws CallException {
		Parameter p = null;
		
		if (paras != null && queryParameters != null){
			Selector[] fields = queryParameters.getFields();
			if (fields != null && fields.length > 0){
				p = client.createParameter();
				
				for (Selector s:fields){
					if (!s.isOk()){
						continue;
					}
					String id = s.getId();
					String value = s.select(paras);

					p.param(id, value);
				}
			}
		}
		
		JsonBuffer buffer = new JsonBuffer();
		
		if (paras != null && arguments != null){
			Selector[] fields = arguments.getFields();
			if (fields != null && fields.length > 0){
				Map<String,Object> root = buffer.getRoot();
				
				for (Selector s:fields){
					if (!s.isOk()){
						continue;
					}
					String id = s.getId();
					
					Object data = paras.getData(id);
					if (data != null){
						root.put(id, data);
					}
				}
			}
		}
		
		long start = System.currentTimeMillis();
		try {	
			client.invoke(uri, p, buffer,buffer);
			if (stat != null){
				stat.count(System.currentTimeMillis() - start, true);
			}
		} catch (ClientException e) {
			if (stat != null){
				stat.count(System.currentTimeMillis() - start, false);
			}
			throw new CallException(e.getCode(),e.getMessage(),e);
		}
		return new HttpResult(buffer);
	}

	private String uri;	

	/**
	 * URL中的参数
	 */
	private FieldList queryParameters = null;

	/**
	 * Http entity中的数据
	 */
	private FieldList arguments = null;
	
	/**
	 * Http Client
	 */
	protected HttpClient client = null;
	
	/**
	 * 统计模型
	 */
	protected Counter stat = null;

	protected Counter createCounter(Properties p){
		String module = PropertiesConstants.getString(p,"call.stat.module", CallStat.class.getName());
		try {
			return Counter.TheFactory.getCounter(module, p);
		}catch (Exception ex){
			logger.warn("Can not create call counter:" + module + ",default counter is instead.");
			return new CallStat(p);
		}
	}
	
	@Override
	public void report(Element xml) {
		if (xml != null){
			xml.setAttribute("module", getClass().getName());
			
			Document doc = xml.getOwnerDocument();
			
			{
				Element _runtime = doc.createElement("runtime");
				
				if (stat != null)
				{
					Element _stat = doc.createElement("stat");
					stat.report(_stat);
					_runtime.appendChild(_stat);
				}
				
				xml.appendChild(_runtime);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			json.put("module", getClass().getName());
			
			{
				Map<String,Object> _runtime = new HashMap<String,Object>();
				
				if (stat != null){
					Map<String,Object> _stat = new HashMap<String,Object>();
					stat.report(_stat);
					_runtime.put("stat", _stat);
				}
				
				json.put("runtime", _runtime);
			}
		}
	}
}
