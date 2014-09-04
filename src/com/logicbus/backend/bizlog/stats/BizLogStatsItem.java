package com.logicbus.backend.bizlog.stats;

import java.lang.reflect.Field;
import java.util.Map;

import com.anysoft.stream.Flowable;
import com.anysoft.util.JsonSerializer;
import com.anysoft.util.JsonTools;

/**
 * BizLog的统计项
 * 
 * @author duanyy
 *
 * @since 1.2.7.1
 * 
 * @version 1.2.7.2 [20140903 duanyy]
 * - 将hasError属性修改为errorTimes;
 */
public class BizLogStatsItem implements Comparable<BizLogStatsItem>,Flowable,JsonSerializer{
	/**
	 * 服务Id
	 */
	protected String serviceId;

	/**
	 * 调用次数
	 */
	protected long times = 0;

	/**
	 * 错误次数
	 * 
	 * @since 1.2.7.2
	 */
	protected long errorTimes = 0;
	
	/**
	 * 调用总时长
	 */
	protected double duration = 0;
	
	/**
	 * 应用ID
	 */
	protected String app;
	
	public BizLogStatsItem(String _serviceId){
		serviceId = _serviceId;
	}
	
	public BizLogStatsItem app(final String _app){
		app = _app;
		return this;
	}
	
	public BizLogStatsItem serviceId(final String _serviceId){
		serviceId = _serviceId;
		return this;
	}
	
	public BizLogStatsItem incr(final long _times,final long _errorTimes,final double _duration){
		times += _times;
		errorTimes += _errorTimes;
		duration += _duration;
		return this;
	}
	
	@Override
	public String getValue(String varName, Object context, String defaultValue) {
		try {
			Class<?> clazz = this.getClass();
			Field field = clazz.getField(varName);
			if (field == null){
				return defaultValue;
			}
			
			Object found = field.get(this);
			return found.toString();
		}catch (Exception ex){
			return defaultValue;
		}
	}

	@Override
	public Object getContext(String varName) {
		return null;
	}

	@Override
	public String getStatsDimesion() {
		return serviceId;
	}

	@Override
	public int compareTo(BizLogStatsItem o) {
		return serviceId.compareTo(o.serviceId);
	}
	
	public int hashCode(){
		return serviceId.hashCode();
	}

	@SuppressWarnings({ "rawtypes"})
	@Override
	public void toJson(Map json) {
		JsonTools.setString(json,"service",serviceId);
		JsonTools.setLong(json, "times", times);
		JsonTools.setLong(json, "error", errorTimes);
		JsonTools.setDouble(json, "duration", duration);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map json) {
		serviceId = JsonTools.getString(json, "service", "");
		times = JsonTools.getLong(json, "times", 0);
		errorTimes = JsonTools.getLong(json, "error", 0);
		duration = JsonTools.getDouble(json, "duration", 0);
	}
}
