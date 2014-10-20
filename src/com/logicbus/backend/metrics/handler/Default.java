package com.logicbus.backend.metrics.handler;

import org.w3c.dom.Element;

import com.anysoft.stream.AbstractHandler;
import com.anysoft.util.Properties;
import com.logicbus.backend.metrics.core.Fragment;
import com.logicbus.backend.metrics.core.MetricsHandler;

/**
 * 缺省的处理器
 * @author duanyy
 *
 */
public class Default extends AbstractHandler<Fragment> implements MetricsHandler{

	@Override
	protected void onHandle(Fragment _data,long t) {
	}

	@Override
	protected void onFlush(long t) {
	}

	@Override
	protected void onConfigure(Element e, Properties p) {
	}

	@Override
	public void metricsIncr(Fragment fragment) {
		handle(fragment,System.currentTimeMillis());
	}
	
}