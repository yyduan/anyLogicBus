package com.logicbus.backend.bizlog.stats;

import java.util.Hashtable;
import java.util.Map;
import com.anysoft.stream.AbstractHandler;

/**
 * 统计数据输出
 * @author duanyy
 *
 * @since 1.2.7.1
 * 
 * @version 1.2.7.2 [20140903 duanyy]
 * - 优化BizLogStatsItem数据结构
 * 
 */
abstract public class StatsWriter extends AbstractHandler<BizLogStatsItem> {
	protected Hashtable<String,BizLogStatsItem> stats = new Hashtable<String,BizLogStatsItem>();
	@Override
	protected void onHandle(BizLogStatsItem _data) {
		String key = _data.getStatsDimesion();
		BizLogStatsItem found = stats.get(key);
		
		if (found == null){
			stats.put(key, _data);
		}else{
			found.incr(_data.times,_data.errorTimes,_data.duration);
		}
	}

	@Override
	protected void onFlush() {
		write(stats);
		stats.clear();
	}
	
	public String getHandlerType(){
		return "stats";
	}
	
	abstract protected void write(Map<String,BizLogStatsItem> _data);
}
