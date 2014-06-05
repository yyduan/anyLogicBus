package com.logicbus.backend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.logicbus.backend.server.http.HttpContext;

/**
 * 缺省的SessionManager
 * 
 * <p>
 * 直接采用应用服务器的实现
 * 
 * @author duanyy
 * @since 1.1.3
 */
public class DefaultSessionManager extends SessionManager {

	@Override
	public HttpSession getSession(Context ctx, boolean create) throws ServantException{
		if (!(ctx instanceof HttpContext)){
			throw new ServantException("core.nothttpcontext","The Context is not a HttpContext instance.");
		}
		
		HttpContext httpContext = (HttpContext)ctx;
		
		HttpServletRequest request = httpContext.getRequest();
		return request.getSession(create);
	}

}
