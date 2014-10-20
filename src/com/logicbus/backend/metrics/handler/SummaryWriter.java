package com.logicbus.backend.metrics.handler;

import java.util.Hashtable;
import java.util.Map;

import com.anysoft.stream.AbstractHandler;
import com.logicbus.backend.metrics.core.Fragment;
import com.logicbus.backend.metrics.core.MetricsHandler;

/**
 * 对外输出
 * @author duanyy
 *
 */
abstract public class SummaryWriter extends AbstractHandler<Fragment> implements MetricsHandler{
	protected Hashtable<String,Fragment> metrics = new Hashtable<String,Fragment>();
	
	public void incr(Fragment f){
		String id = f.getStatsDimesion();
		
		Fragment found = metrics.get(id);
		
		if (found == null){
			synchronized(metrics){
				found = metrics.get(id);
				if (found == null){
					metrics.put(id, f);
				}else{
					found.incr(f);
				}
			}
		}else{
			found.incr(f);
		}
	}

	@Override
	public void metricsIncr(Fragment fragment) {
		handle(fragment,System.currentTimeMillis());
	}	
	
	@Override
	protected void onHandle(Fragment _data,long t) {
		incr(_data);
	}

	@Override
	protected void onFlush(long t) {
		write(metrics,t);
		metrics.clear();
	}			
	
	abstract protected void write(Map<String,Fragment> data,long t);
}