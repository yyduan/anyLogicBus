package com.logicbus.backend.bizlog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.BizLogItem;
import com.logicbus.backend.BizLogger;


/**
 * BizLoger的虚基类
 * 
 * @author duanyy
 * @since 1.2.3
 * 
 */
abstract public class AbstractBizLogger implements BizLogger {
	/**
	 * 当前的时间周期，缺省半个小时
	 */
	protected long currentCycle = 30 * 60 * 1000;
	
	/**
	 * 周期时间戳
	 */
	protected long lastVisitedTime;
	
	public AbstractBizLogger(Properties props){
		reportSupport = PropertiesConstants.getBoolean(props, "bizlog.report", reportSupport);
		currentCycle = PropertiesConstants.getLong(props, "bizlog.report.current", currentCycle);
		
		if (reportSupport){
			total_items = new Hashtable<String,ReportItem>();
			current_items = new Hashtable<String,ReportItem>();
		}
	}

	private void stat(String group,Hashtable<String,ReportItem> items,BizLogItem item){
		ReportItem found = null;
		synchronized (items){
			found = items.get(group);
			if (found == null){
				found = new ReportItem(group);
				items.put(group, found);
			}				
		}
		if (found != null){
			synchronized(found){
				found.times ++;
				found.avg = (found.avg * (found.times - 1) + item.duration) / found.times;					
			}
		}		
	}
	
	@Override
	public void log(BizLogItem item) {
		if (reportSupport){
			String group = getGroup(item);
			//当前时间
			long current = System.currentTimeMillis();
			if (current / currentCycle - lastVisitedTime / currentCycle != 0){
				//新的周期
				synchronized(current_items){
					current_items.clear();
				}
			}
			stat(group,current_items,item);
			stat(group,total_items,item);
			lastVisitedTime = current;
		}
		onLog(item);
	}
	
	protected abstract void onLog(BizLogItem item);

	@Override
	public void close() {
	}

	@Override
	public void report(Element root) {
		if (reportSupport){
			Document doc = root.getOwnerDocument();
			Element current = doc.createElement("current");
			report(current_items,current);
			root.appendChild(current);
			
			Element total = doc.createElement("total");
			report(total_items,total);
			root.appendChild(total);
			
			root.setAttribute("module", getClass().getName());
		}
	}
	
	private void report(Hashtable<String,ReportItem> _items,Element root){
		Document doc = root.getOwnerDocument();
		long times = 0;
		double duration = 0;
		for (ReportItem item:_items.values()){
			Element itemElem = doc.createElement("item");			
			itemElem.setAttribute("group", item.group);
			itemElem.setAttribute("times", String.valueOf(item.times));
			itemElem.setAttribute("avg_duration", df.format(item.avg));
			root.appendChild(itemElem);
			
			times += item.times;
			duration += item.times * item.avg;
		}			
		
		root.setAttribute("times", String.valueOf(times));
		if (times > 0){
			root.setAttribute("avg_duration", df.format(duration / times));
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (reportSupport){
			report(current_items,json,"current");
			report(total_items,json,"total");
			json.put("module", getClass().getName());
		}
	}
	
	private void report(Hashtable<String,ReportItem> _items,Map<String,Object> json,String name){
		long times = 0;
		double duration = 0;
		List<Object> items = new ArrayList<Object>();
		for (ReportItem item:_items.values()){
			Map<String,Object> itemObj = new HashMap<String,Object>(3);
			itemObj.put("group", item.group);
			itemObj.put("times", item.times);
			itemObj.put("avg_duration", df.format(item.avg));
			items.add(itemObj);
			
			times += item.times;
			duration += item.times * item.avg;		
		}
		json.put("item", items);
		json.put("times", times);
		if (times > 0){
			json.put("avg_duration", df.format(duration / times));
		}
	}

	public String getGroup(BizLogItem item){
		return item.client + "_" + item.id + "_" + item.result; 
	}
	
	protected Hashtable<String,ReportItem> total_items = null;
	protected Hashtable<String,ReportItem> current_items = null;
	/**
	 * 是否支持Report
	 */
	protected boolean reportSupport = true;
	
	/**
	 * double数值格式化器
	 */
	private static DecimalFormat df = new DecimalFormat("#.000");	
	
	public static class ReportItem implements Comparable<ReportItem>{
		public String group;
		
		public ReportItem(String _group){
			group = _group;
		}
		
		public long times = 0;
		public double avg = 0;
		@Override
		public int compareTo(ReportItem other) {
			return group.compareTo(other.group);
		}
	}
}
