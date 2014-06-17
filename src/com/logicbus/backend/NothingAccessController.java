package com.logicbus.backend;

import org.w3c.dom.Element;

import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

public class NothingAccessController implements AccessController {

	@Override
	public String createSessionId(Path serviceId, ServiceDescription servant,
			Context ctx) {
		return ctx.getClientIp() + ":" + serviceId.getPath();
	}

	@Override
	public int accessStart(String sessionId, Path serviceId,
			ServiceDescription servant, Context ctx) {
		return 1;
	}

	@Override
	public int accessEnd(String sessionId, Path serviceId,
			ServiceDescription servant, Context ctx) {
		return 0;
	}

	@Override
	public void toXML(Element root) {

	}

}
