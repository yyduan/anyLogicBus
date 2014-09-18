package com.logicbus.backend.stats.context;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.Watcher;
import com.logicbus.backend.stats.core.MetricsHandler;

public class Inner implements MetricsHandlerContext {
	
	protected MetricsHandlerHolder holder = new MetricsHandlerHolder();
	
	protected final static Logger logger = LogManager.getLogger(Inner.class);
	
	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		if (holder != null){
			holder.configure(_e, _properties);
		}
	}

	@Override
	public void close() throws Exception {
		if (holder != null){
			holder.close();
		}
	}

	@Override
	public MetricsHandler getHandler(String id) {
		return holder != null ? holder.getPool(id) : null;
	}

	@Override
	public void addWatcher(Watcher<MetricsHandler> watcher) {
		// do nothing
	}

	@Override
	public void removeWatcher(Watcher<MetricsHandler> watcher) {
		// do nothing
	}
}
