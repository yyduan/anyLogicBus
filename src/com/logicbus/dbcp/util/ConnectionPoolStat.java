package com.logicbus.dbcp.util;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.DateUtil;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Reportable;

/**
 * 连接池统计工具
 * 
 * @author duanyy
 * 
 * @since 1.2.9
 *
 */
public class ConnectionPoolStat implements Reportable {
	/**
	 * 启动时间
	 */
	private long startTime = System.currentTimeMillis();
		
	/**
	 * 当前状态
	 */
	public String status = "running";
	
	/**
	 * 全部统计数据（自服务器启动开始）
	 */
	private StatUnit total = new StatUnit();
	
	/**
	 * 当前统计数据（当前周期）
	 */
	private StatUnit current = new StatUnit();
	
	/**
	 * 上次访问时间
	 */
	public long lastVisitedTime = System.currentTimeMillis();
		
	/**
	 * 周期开始时间
	 */
	private long currentCycleStart = System.currentTimeMillis();
	
	/**
	 * 周期
	 */
	private long cycle = 60 * 1000;
	/**
	 * 访问记录
	 * 
	 * <br>在服务访问结束之后调用
	 * @param _duration 时长
	 * @param error 是否错误调用
	 */
	public void visited(long _duration,boolean error){
		total.visited(_duration, error);
		
		long now = System.currentTimeMillis();

		if (now / cycle - lastVisitedTime / cycle == 0){
			//和上次记录处于同一个周期
			current.visited(_duration, error);
		}else{
			current.first(_duration, error);
			currentCycleStart = (now / cycle) * cycle;
		}
		lastVisitedTime = now;
	}	
	
	public ConnectionPoolStat(Properties p){
		cycle = PropertiesConstants.getInt(p, "dbcp.stat.cycle", 60*1000);
	}
	
	@Override
	public void report(Element root) {
		if (root != null){
			Document doc = root.getOwnerDocument();
			
			root.setAttribute("status", status);
			root.setAttribute("start", DateUtil.formatDate(new Date(startTime), "yyyyMMddHHmmss"));
			root.setAttribute("lastVistiedTime", DateUtil.formatDate(new Date(lastVisitedTime), "yyyyMMddHHmmss"));
			root.setAttribute("cycleStart", DateUtil.formatDate(new Date(currentCycleStart), "yyyyMMddHHmmss"));
			
			if (total != null){
				Element stat = doc.createElement("total");
				
				total.report(stat);
				
				root.appendChild(stat);
			}
			
			if (current != null){
				Element stat = doc.createElement("current");
				
				current.report(stat);
				
				root.appendChild(stat);
			}			
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			json.put("status", status);
			json.put("start", DateUtil.formatDate(new Date(startTime), "yyyyMMddHHmmss"));
			json.put("lastVistiedTime", DateUtil.formatDate(new Date(lastVisitedTime), "yyyyMMddHHmmss"));
			json.put("cycleStart", DateUtil.formatDate(new Date(currentCycleStart), "yyyyMMddHHmmss"));
			
			if (total != null){
				Map<String,Object> stat = new HashMap<String,Object>();
				
				total.report(stat);
				
				json.put("total", stat);
			}
			
			if (current != null){
				Map<String,Object> stat = new HashMap<String,Object>();
				
				current.report(stat);
				
				json.put("current", stat);
			}			
		}
	}
	
	public static final class StatUnit implements Reportable{
		/**
		 * 服务次数（当前周期）
		 */
		public volatile long times = 0;
		
		/**
		 * 错误次数（当前周期）
		 */
		public volatile long errorTimes = 0;
		
		/**
		 * 最大时长
		 */
		public volatile long max = 0;
		
		/**
		 * 最小时长
		 */
		public volatile long min = 100000;
		
		/**
		 * 平均时长（当期周期）
		 */
		public volatile double avg = 0;
		
		/**
		 * 访问
		 * @param _duration 时长
		 * @param code 结果原因
		 */
		public void visited(long _duration,boolean error){
			//计算平均值
			if (times <= 0){
				avg =  _duration;
			}else{
				avg = (avg * times + _duration) / (times + 1);
			}
				
			//计算次数
			times += 1;
				
			//计算最小值
			if (min > _duration){
				min = _duration;
			}
			
			//计算最大值
			if (max < _duration){
				max = _duration;
			}
			
			errorTimes += (error?1:0);
		}
		
		/**
		 * 首次访问
		 * @param _duration 时长
		 * @param code 结果原因
		 */
		public void first(long _duration,boolean error){
			//计算平均值
			avg =  _duration;
				
			//计算次数
			times = 1;
				
			//计算最小值
			min = _duration;
			
			//计算最大值
			max = _duration;
			
			errorTimes = (error?1:0);
		}		
		
		@Override
		public void report(Element xml) {
			if (xml != null){
				xml.setAttribute("times", String.valueOf(times));
				xml.setAttribute("error", String.valueOf(errorTimes));
				xml.setAttribute("max", String.valueOf(max));
				xml.setAttribute("min", String.valueOf(min));
				xml.setAttribute("avg", df.format(avg));
			}
		}

		@Override
		public void report(Map<String, Object> json) {
			if (json != null){
				json.put("times", String.valueOf(times));
				json.put("error", String.valueOf(errorTimes));
				json.put("max", String.valueOf(max));
				json.put("min", String.valueOf(min));
				json.put("avg", df.format(avg));
			}
		}
		/**
		 * double数值格式化器
		 */
		private static DecimalFormat df = new DecimalFormat("#.00"); 
	}
}
