package com.logicbus.manager.monitor;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.XmlTools;
import com.logicbus.backend.Metric;

public class MetricHolder {
	protected Hashtable<String,Metric> metrics = new Hashtable<String,Metric>();
	protected long lastUpdated = 0;
	public long getLastUpdated(){return lastUpdated;}
	public void setMetrics(String strMetrics){
		DefaultProperties props = new DefaultProperties();
		props.loadFromString(strMetrics);
		
		Enumeration<String> keys = props.keys();
		
		while (keys.hasMoreElements()){
			String key = keys.nextElement();
			String value = props.GetValue(key, "");
			if (value != null && value.length() > 0){
				addMetric(key,value);
			}
		}
	}	
	protected void addMetric(String id,String rras){
		Metric m = new Metric(id,rras);
		metrics.put(m.getID(), m);
	}
	
	public Metric getMetric(String id){
		return metrics.get(id);
	}
	
	public String [] keys(){
		return metrics.keySet().toArray(new String[0]);
	}
	
	public void updateMetric(String id,long timestamp,double value){
		Metric m = getMetric(id);
		if (m != null){
			m.update(timestamp, value);
		}		
	}
	public void update(Element e){
		lastUpdated = System.currentTimeMillis();
		Element eRuntime = XmlTools.getFirstElementByPath(e, "runtime");
		updateMetrics(eRuntime);
	}	
	public void updateMetrics(Element e){
		if (e != null){
			long timestamp = 0;
			{
				String value = e.getAttribute("timestamp");
				if (value == null || value.length() <= 0){
					timestamp = System.currentTimeMillis();
				}else{
					timestamp = Long.valueOf(value);
					if (timestamp <= 0){
						timestamp = System.currentTimeMillis();
					}
				}
			}
			String [] keys = keys();
			for (String key:keys){
				updateMetric(key,timestamp,getAttr(e,key,0));
			}
		}
	}
	private double getAttr(Element e,String attr,double defaultValue){
		double retValue = 0;
		try {
			String value = e.getAttribute(attr);
			if (value == null || value.length() <= 0){
				retValue = defaultValue;
			}else{
				retValue = Double.valueOf(value);
			}
		}catch (Exception ex){
			retValue = defaultValue;
		}
		return retValue;
	}
	public void toXML(Element root){
		Document doc = root.getOwnerDocument();
		String [] keys = keys();
		if (keys.length > 0){
			Element eMetrics = doc.createElement("metrics");
			
			for (String key:keys){
				Metric metric = getMetric(key);
				if (metric != null){
					Element eMetric = doc.createElement("metric");
					metric.report(eMetric);
					eMetrics.appendChild(eMetric);
				}
			}
			root.appendChild(eMetrics);
		}
	}
}
