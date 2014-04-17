package com.logicbus.datasource;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.Metric;


/**
 * 数据源的统计信息
 * 
 * @author duanyy
 * @since 1.0.6
 */
public class DataSourceStat {

	/**
	 * 累计统计指标
	 */
	protected StatUnit totalStat = new StatUnit();
	
	/**
	 * 上一周期的指标
	 */
	protected StatUnit lastStat = new StatUnit();
	
	/**
	 * 当前周期统计
	 */
	protected StatUnit nowStat = new StatUnit();
	
	/**
	 * 采样时长，缺省60s
	 */
	protected int step = 60 * 1000;
	
	/**
	 * 次数指标
	 */
	protected Metric timesMetric = null;

	/**
	 * 总时长指标
	 */
	protected Metric durationMetric = null;

	/**
	 *　返回为null的次数 
	 */
	protected Metric errorMetric = null;
	
	/**
	 *　等待队列长度
	 */
	protected Metric queueMetric = null;
	
	/**
	 *　等待队列长度
	 */
	protected Metric maxMetric = null;
	
	/**
	 * 上次访问时间
	 */
	protected long lastVisitedTime = System.currentTimeMillis();
	
	/**
	 * 周期开始时间
	 */
	protected long cycleStartTime = System.currentTimeMillis();
	
	/**
	 * 设置监控指标
	 * @param monitor 监控指标
	 */
	public void setMonitor(String monitor){
		Properties props = new DefaultProperties();
		
		props.loadFromString(monitor);
		
		step = PropertiesConstants.getInt(props, "step", 60) * 1000;
		
		String value = PropertiesConstants.getString(props, "times_rras", "");
		if (value.length() > 0){
			timesMetric = new Metric("times",value);
		}
		value = PropertiesConstants.getString(props, "duration_rras", "");
		if (value.length() > 0){
			durationMetric = new Metric("duration",value);
		}
		value = PropertiesConstants.getString(props, "error_rras", "");
		if (value.length() > 0){
			errorMetric = new Metric("errors",value);
		}
		value = PropertiesConstants.getString(props, "queue_rras", "");
		if (value.length() > 0){
			queueMetric = new Metric("queue",value);
		}
		value = PropertiesConstants.getString(props, "maxduration_rras", "");
		if (value.length() > 0){
			maxMetric = new Metric("maxDuration",value);
		}		
	}
	
	public void visited(int waitQueueLength,long duration,boolean isNull){
		totalStat.times ++;
		totalStat.nullTimes += ((isNull)? 1 : 0);
		if (duration > totalStat.maxDuration)
			totalStat.maxDuration = duration;
		totalStat.totalDuration += duration;
		totalStat.waitQueueLength = waitQueueLength;
		
		
		long current = System.currentTimeMillis();
		if (current / step - lastVisitedTime / step == 0){
			//和上次访问在同一个周期
			nowStat.times ++;
			nowStat.nullTimes += ((isNull)? 1 : 0);
			if (duration > nowStat.maxDuration){
				nowStat.maxDuration = duration;
			}
			nowStat.totalDuration += duration;
			nowStat.waitQueueLength = waitQueueLength;
		}else{
			//新的周期
			cycleStartTime = (current / step) * step;
			
			lastStat.clone(nowStat);
			
			nowStat.times = 1;
			nowStat.maxDuration = duration;
			nowStat.nullTimes =  ((isNull)? 1 : 0);
			nowStat.totalDuration = duration;
			nowStat.waitQueueLength = waitQueueLength;
			
			if (timesMetric != null){
				timesMetric.update(cycleStartTime, lastStat.times);
			}
			
			if (durationMetric != null){
				durationMetric.update(cycleStartTime, lastStat.totalDuration);
			}
			
			if (errorMetric != null){
				errorMetric.update(cycleStartTime, lastStat.nullTimes);
			}
			
			if (queueMetric != null){
				queueMetric.update(cycleStartTime, lastStat.waitQueueLength);
			}
		}
		lastVisitedTime = current;
	}
	
	public void toXML(Element e){
		e.setAttribute("step",String.valueOf(step));
		e.setAttribute("timestamp", String.valueOf(cycleStartTime));
		
		Document doc = e.getOwnerDocument();
		
		Element totalElement = doc.createElement("total");
		totalStat.toXML(totalElement);
		e.appendChild(totalElement);
		
		Element lastElement = doc.createElement("last");
		lastStat.toXML(lastElement);
		e.appendChild(lastElement);
		
		Element nowElement = doc.createElement("now");
		nowStat.toXML(nowElement);
		e.appendChild(nowElement);		
		
		Element eMetrics = doc.createElement("metrics");
		
		if (timesMetric != null){
			Element metric = doc.createElement("metric");
			timesMetric.toXML(metric);
			eMetrics.appendChild(metric);
		}

		if (durationMetric != null){
			Element metric = doc.createElement("metric");
			durationMetric.toXML(metric);
			eMetrics.appendChild(metric);
		}
		
		if (errorMetric != null){
			Element metric = doc.createElement("metric");
			errorMetric.toXML(metric);
			eMetrics.appendChild(metric);
		}
		
		if (queueMetric != null){
			Element metric = doc.createElement("metric");
			queueMetric.toXML(metric);
			eMetrics.appendChild(metric);
		}
		
		e.appendChild(eMetrics);
	}
	
	/**
	 * 统计单位
	 * @author duanyy 
	 *
	 */
	public static final class StatUnit{
		/**
		 * 请求次数
		 */
		public long times = 0;
		
		/**
		 * 最大耗时
		 */
		public long maxDuration = 0;
		
		/**
		 * 平均耗时
		 */
		public long totalDuration = 0;
		
		/**
		 * 返回为空的次数
		 * 
		 * <br>
		 * 原因包括: <br>
		 * - 由于队列满,暂时无法得到连接,超时 <br>
		 * - 数据库无法连接上 <br>
		 */
		public long nullTimes = 0;

		/**
		 * 等待队列长度
		 */
		public int waitQueueLength = 0;
		
		/**
		 * 输出到XML
		 * @param e XML节点
		 */
		public void toXML(Element e){
			e.setAttribute("times", String.valueOf(times));
			e.setAttribute("maxDuration", String.valueOf(maxDuration));
			e.setAttribute("totalDuration", String.valueOf(totalDuration));
			e.setAttribute("nullTimes", String.valueOf(nullTimes));
			e.setAttribute("waitQueueLength",String.valueOf(waitQueueLength));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void toJson(Map json){
			json.put("times", times);
			json.put("maxDuration", maxDuration);
			json.put("totalDuration", totalDuration);
			json.put("nullTimes", nullTimes);
			json.put("waitQueueLength", waitQueueLength);
		}
		
		public void clone(StatUnit other){
			times = other.times;
			maxDuration = other.maxDuration;
			totalDuration = other.totalDuration;
			nullTimes = other.nullTimes;
			waitQueueLength = other.waitQueueLength;
		}
	}
}
