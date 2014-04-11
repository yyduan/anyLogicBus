package com.logicbus.client;

/**
 * 客户端请求
 * @author duanyy
 *
 */
public class Request extends Buffer {

	public Request(int bufSize) {
		super(bufSize);
	}

	public Request(){
		this(2048);
	}
	
}
