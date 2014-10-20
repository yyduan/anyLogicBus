package com.logicbus.backend.bizlog;

import org.w3c.dom.Element;

import com.anysoft.stream.AbstractHandler;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.logicbus.backend.metrics.core.Dimensions;
import com.logicbus.backend.metrics.core.Fragment;
import com.logicbus.backend.metrics.core.Measures;
import com.logicbus.backend.metrics.core.MetricsHandler;

public class MetricsBizLogger extends AbstractHandler<BizLogItem> implements
BizLogger {

	@Override
	protected void onHandle(BizLogItem _data,long t) {
		if (metricsHandler == null){
			synchronized (lock){
				if (metricsHandler == null){
					Settings settings = Settings.get();
					metricsHandler = (MetricsHandler) settings.get("metricsHandler");
				}
			}
		}
		if (metricsHandler != null){
			Fragment f = new Fragment(metricsId);
			
			Dimensions dims = f.getDimensions();
			dims.lpush(_data.id);
			
			Measures meas = f.getMeasures();
			meas.lpush(new Object[]{new Long(1L),new Long(_data.result.equals("core.ok")?0L:1L),new Double(_data.duration)});
	
			metricsHandler.handle(f,t);
		}
	}

	@Override
	protected void onFlush(long t) {
		if (metricsHandler != null){
			metricsHandler.flush(t);
		}
	}

	@Override
	protected void onConfigure(Element e, Properties p) {
		metricsId = PropertiesConstants.getString(p, "metrics", metricsId,true);
	}
	
	protected String metricsId = "svc.thpt";
	
	protected static Object lock = new Object();
	protected static MetricsHandler metricsHandler = null;
}
