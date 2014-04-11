package com.logicbus.client;


/**
 * 客户端代理类
 * 
 * @author duanyy
 * @since 1.0.4
 * 
 */
abstract public class Client {
	/**
	 * 服务调用
	 * @param id 服务ID
	 * @return response
	 * @throws ClientException
	 */
	public Response invoke(String id) throws ClientException{
		Response result = new Response();
		return invoke(id,null,result);		
	}
	/**
	 * 服务调用
	 * @param id 服务ID
	 * @param result response
	 * @return response
	 * @throws ClientException
	 */
	public Response invoke(String id,Response result) throws ClientException{
		return invoke(id,null,result);
	}
	/**
	 * 服务调用
	 * @param id 服务ID
	 * @param para request
	 * @return response
	 * @throws ClientException
	 */
	public Response invoke(String id,Request para) throws ClientException{
		Response result = new Response();
		return invoke(id,para,result);
	}
	/**
	 * 服务调用
	 * @param id 服务ID
	 * @param para request
	 * @param result response
	 * @return response
	 * @throws ClientException
	 */
	abstract public Response invoke(String id,Request para,Response result) throws ClientException;	
}
