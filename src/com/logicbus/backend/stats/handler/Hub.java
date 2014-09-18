package com.logicbus.backend.stats.handler;

import com.anysoft.stream.HubHandler;
import com.logicbus.backend.stats.core.Fragment;
import com.logicbus.backend.stats.core.MetricsHandler;

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