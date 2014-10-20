package com.logicbus.backend.metrics.handler;

import com.anysoft.stream.DispatchHandler;
import com.logicbus.backend.metrics.core.Fragment;
import com.logicbus.backend.metrics.core.MetricsHandler;

/**
 * 分发处理器
 * @author duanyy
 *
 */
public class Dispatch extends DispatchHandler<Fragment> implements MetricsHandler{
	public String getHandlerType(){
		return "handler";
	}

	@Override
	public void metricsIncr(Fragment fragment) {
		handle(fragment,System.currentTimeMillis());
	}
}