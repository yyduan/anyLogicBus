package com.logicbus.backend;

import com.anysoft.util.*;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 服务统计信息
 * 
 * @author duanyy
 *
 */
public class ServantStat {
	/**
	 * 启动时间
	 */
	public long startTime = System.currentTimeMillis();
	/**
	 * 服务次数
	 */
	public long serviceTimes = 0;
	/**
	 * 最大服务时长
	 */
	public long serviceMaxTime = 0;
	/**
	 * 最短服务时长
	 */
	public long serviceMinTime = 999999999;
	/**
	 * 平均服务时长
	 */
	public double serviceAvgTime = 0;
	/**
	 * 当期状态
	 */
	public String status = "running";
	/**
	 * 上次访问时间
	 */
	public long lastVisitedTime = System.currentTimeMillis();
	/**
	 * 采样步长,缺省1分钟
	 */
	private int step = 60 * 1000;
	/**
	 * 当前周期的调用次数
	 */
	private int times = 0;
	/**
	 * 当前周期的错误次数
	 */
	private int errorTimes = 0;
	/**
	 * 当前周期的调用总时长
	 */
	private long duration = 0;
	/**
	 * 上一周期的调用次数
	 */
	private int lastTimes = 0;
	/**
	 * 上一周期的错误次数
	 */
	private int lastErrorTimes = 0;
	/**
	 * 上一周期的调用总时长
	 */
	private long lastDuration = 0;
	/**
	 * 周期开始时间
	 */
	private long cycleStartTime = 0;
	
	/**
	 * 等待线程数
	 */
	private long waitCnt = 0;
	public void setWaitCnt(long _cnt){waitCnt = _cnt;}
	/**
	 * 访问记录
	 * 
	 * <br>在服务访问结束之后调用
	 * @param _duration 时长
	 * @param code 返回码
	 */
	public void visited(long _duration,String code){
		serviceTimes ++;
		
		if (serviceTimes <= 1)
			serviceAvgTime = _duration;
		else
			serviceAvgTime = (serviceAvgTime * (serviceTimes - 1) + _duration)/ serviceTimes;
		
		if (serviceMinTime > _duration){
			serviceMinTime = _duration;
		}
		if (serviceMaxTime < _duration){
			serviceMaxTime = _duration;
		}
		
		long current = System.currentTimeMillis();

		if (current / step - lastVisitedTime / step == 0){
			//和上次记录处于同一个周期
			times ++;
			duration += _duration;
			errorTimes += (code.equals("core.ok")?0:1);
		}else{
			//新的周期
			lastTimes = times;
			lastDuration = duration;
			lastErrorTimes = errorTimes;
			times = 1;
			duration = _duration;
			cycleStartTime = (current / step) * step;
			
			if (timesMetric != null){
				timesMetric.update(cycleStartTime, lastTimes);
			}
			if (durationMetric != null){
				durationMetric.update(cycleStartTime, lastDuration);
			}
			if (errorMetric != null){
				errorMetric.update(cycleStartTime, lastErrorTimes);
			}
		}
		lastVisitedTime = current;
	}	
	
	/**
	 * 输出到XML
	 * 
	 * @param root XML根节点
	 */
	public void toXML(Element root){
		Document doc = root.getOwnerDocument();
		
		
		root.setAttribute("status", status);
		root.setAttribute("start", DateUtil.formatDate(new Date(startTime), "yyyyMMddHHmmss"));
		root.setAttribute("times", String.valueOf(serviceTimes));
		root.setAttribute("maxDuration", String.valueOf(serviceMaxTime));
		root.setAttribute("minDuration", String.valueOf(serviceMinTime));
		root.setAttribute("avgDuration", String.valueOf(serviceAvgTime));
		root.setAttribute("lastVistiedTime", DateUtil.formatDate(new Date(lastVisitedTime), "yyyyMMddHHmmss"));
		
		Element stat = doc.createElement("runtime");
		stat.setAttribute("curTimes", String.valueOf(times));
		stat.setAttribute("curDuration", df.format(duration));
		stat.setAttribute("times", String.valueOf(lastTimes));
		stat.setAttribute("errors",String.valueOf(lastErrorTimes));
		stat.setAttribute("duration", df.format(lastDuration));
		stat.setAttribute("timestamp", String.valueOf(cycleStartTime));
		stat.setAttribute("step", String.valueOf(step));
		stat.setAttribute("waitCnt", String.valueOf(waitCnt));
		root.appendChild(stat);		
		
		if (timesMetric != null || durationMetric != null){
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
			root.appendChild(eMetrics);
		}
		
	}
	
	/**
	 * 设置监视器
	 * 
	 * <br>可以在monitor参数中定义监控指标，monitor参数的语法为：<br>
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * var1=value1;var2=value2
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * <br>monitor参数支持4种参数：<br>
	 * - step:采样步长，单位为秒，缺省为60<br>
	 * - times_rras:调用次数指标，可定义多个RRA，以,号分隔<br>
	 * - duration_rras:调用时长指标，可定义多个RRA,以,号分隔<br>
	 * - error_rras:调用错误次数指标，可定义多个RRA，以,号分隔<br>
	 * 
	 * 本函数在{@link com.logicbus.backend.ServantPool ServantPool}的构造函数中调用。
	 * 可在服务描述参数或全局参数中定义service.monitor变量来配置本函数的monitor参数。
	 * 
	 * @param monitor monitor参数
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
	}
	
	/**
	 * 调用次数指标
	 */
	protected Metric timesMetric = null;
	/**
	 * 调用时长指标
	 */
	protected Metric durationMetric = null;
	/**
	 * 调用失败次数指标
	 */
	protected Metric errorMetric = null;
	
	/**
	 * double数值格式化器
	 */
	private static DecimalFormat df = new DecimalFormat("#.00"); 
	
	/**
	 * 打印统计信息
	 * @param out 输出流
	 */
	public void list(PrintStream out){
		out.println("Start Time:" + DateUtil.formatDate(new Date(startTime),"yyyyMMddHHmmss"));
		out.println("Service Times:" + serviceTimes);
		out.println("Max Duration:" + serviceMaxTime);
		out.println("Min Duration:" + serviceMinTime);
		out.println("AVG Duration:" + serviceAvgTime);
	}
}
