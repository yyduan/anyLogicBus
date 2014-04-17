package com.logicbus.backend.server.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.logicbus.backend.Context;

/**
 * Http请求的上下文
 * 
 * @author duanyy
 * @version 1.0.5 [20140412 duanyy]  <br>
 * - 改进消息传递模型 <br>
 */

public class HttpContext extends Context {
	
	/**
	 * request
	 */
	protected HttpServletRequest request = null;
	
	/**
	 * to get request
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest(){ return request;}
	
	/**
	 * constructor
	 * @param _request HttpServletRequest
	 */
	public HttpContext(HttpServletRequest _request){
		request = _request;
	}
	
	@Override 
	public String _GetValue(String _name) {
		String found = super._GetValue(_name);
		if (found == null || found.length() <= 0){
			if (request != null)
			{
				HttpSession session = request.getSession(false);
				if (session != null){
					Object obj = session.getAttribute(_name);
					if (obj != null) return obj.toString();
				}
				String value = request.getParameter(_name);
				if (value != null){
					return value;
				}
			}
		}
		return found;
	}

	@Override
	public String getClientIp() {
		return request.getRemoteHost();
	}

	@Override
	public String getHost() {
		return request.getLocalAddr() + ":" + request.getLocalPort();
	}

	@Override
	public String getRequestURI() {
		return request.getRequestURI();
	}
}