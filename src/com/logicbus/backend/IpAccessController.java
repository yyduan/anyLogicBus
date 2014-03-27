package com.logicbus.backend;

import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 基于IP的访问控制器
 * 
 * <p>
 * 本AccessController实现表达了这样的场景：<br>
 * - 限定每个客户端总的访问并发数，通过client.maxThead环境变量控制，缺省为10;<br>
 * - 限定每个客户端在一分钟内的访问次数，通过client.maxTimesPerMin环境变量控制，缺省值为1000.<br>
 * 
 * @author duanyy
 *
 */
public class IpAccessController extends IpAndServiceAccessController {
	
	@Override
	protected String getSessionId(Path serviceId, ServiceDescription servant,
			Context ctx){
		return ctx.getClientIp();
	}
}
