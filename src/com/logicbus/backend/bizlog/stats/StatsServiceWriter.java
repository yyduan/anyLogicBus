package com.logicbus.backend.bizlog.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.logicbus.client.ClientException;
import com.logicbus.client.HttpClient;
import com.logicbus.client.JsonBuffer;
import com.logicbus.client.Parameter;

/**
 * 统计数据服务输出
 * 
 * @author duanyy
 * 
 * @since 1.2.7.1
 * 
 */
public class StatsServiceWriter extends StatsWriter {

	@Override
	protected void write(Map<String, BizLogStatsItem> _data) {
		//免失败模式开启
		//如果连续错误次数超过3次，则连续10分钟不再尝试，直接返回为true
		if (errorTimes > 3 && System.currentTimeMillis() - lastTryTime < 10 * 60 * 1000)
			return ;
		
		if (url == null || url.length() <= 0){
			//没有配置服务器URL
			return;
		}
		
		Parameter para = client.createParameter()
		.param("t",String.valueOf(System.currentTimeMillis()));
		
		if (app != null && app.length() >= 0){
			para.param("a", app);
		}
		
		JsonBuffer result = new JsonBuffer();
		
		{
			//组织input
			Map<String,Object> root = result.getRoot();
			
			List<Object> data = new ArrayList<Object>();
			Collection<BizLogStatsItem> values = _data.values();
			
			for (BizLogStatsItem item:values){
				Map<String,Object> map = new HashMap<String,Object>(5);
				item.toJson(map);
				data.add(map);
			}
			
			root.put("data", data);
		
			root.put("app", app);
			
			if (host == null){
				Settings settings = Settings.get();
				host = settings.GetValue("host", "${server.host}:${server.port}");
			}
			root.put("host", host);
			
			para.param("count", String.valueOf(data.size()));
		}
		try {	
			client.invoke(url, para, result,result);
			errorTimes = 0;
		} catch (ClientException e) {
			errorTimes ++;			
		}finally{		
			lastTryTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void onConfigure(Element e, Properties p) {
		url = PropertiesConstants.getString(p,"url","",true);
		app = PropertiesConstants.getString(p,"app","${server.app}",true);
		client = new HttpClient(p);
	}

	protected String host = null;
	protected String app;
	protected String url;
	protected HttpClient client;
	
	//连续错误次数
	private int errorTimes = 0;
	//上次尝试时间
	private long lastTryTime = 0;	
}
