package com.logicbus.backend.metrics.handler;

import com.anysoft.stream.HubHandler;
import com.logicbus.backend.metrics.core.Fragment;
import com.logicbus.backend.metrics.core.MetricsHandler;

/**
 * Hub处理器
 * @author duanyy
 *
 */
public class Hub extends HubHandler<Fragment> implements MetricsHandler{
	public String getHandlerType(){
		return "handler";
	}

	@Override
	public void metricsIncr(Fragment fragment) {
		handle(fragment,System.currentTimeMillis());
	}
}	