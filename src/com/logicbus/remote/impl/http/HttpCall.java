package com.logicbus.remote.impl.http;

import java.util.Map;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
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
import com.logicbus.selector.FieldList;
import com.logicbus.selector.Selector;

/**
 * 基于Http请求的实现
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class HttpCall implements Call {

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
		
		try {	
			client.invoke(uri, p, buffer,buffer);
		} catch (ClientException e) {
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
	
	public static void main(String [] args){
		
	}
}
