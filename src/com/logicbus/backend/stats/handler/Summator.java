package com.logicbus.backend.stats.handler;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import com.anysoft.stream.Handler;
import com.logicbus.backend.stats.core.Fragment;
import com.logicbus.backend.stats.core.Slide;

/**
 * 累加器
 * @author duanyy
 *
 */
public class Summator extends Slide{
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
	protected void onHandle(Fragment _data,long t) {
		incr(_data);
	}

	@Override
	protected void onFlush(long t) {
		Handler<Fragment> handler = getSlidingHandler();
		Iterator<Entry<String,Fragment>> iterator = metrics.entrySet().iterator();
		
		while (iterator.hasNext()){
			Entry<String,Fragment> entry = iterator.next();
			if (handler != null){
				handler.handle(entry.getValue(),t);
			}
		}
		
		if (handler != null){
			handler.flush(t);
		}
		
		metrics.clear();
	}		
}
