package com.logicbus.backend.stats.handler;

import com.anysoft.stream.AbstractHandler;
import com.logicbus.backend.stats.core.Fragment;
import com.logicbus.backend.stats.core.MetricsHandler;

abstract public class Writer extends AbstractHandler<Fragment> implements MetricsHandler{

	@Override
	public void metricsIncr(Fragment fragment) {
		handle(fragment,System.currentTimeMillis());
	}

	@Override
	protected void onHandle(Fragment _data, long timestamp) {
		write(_data,timestamp);
	}

	@Override
	protected void onFlush(long timestamp) {

	}

	abstract protected void write(Fragment data,long t);
}
