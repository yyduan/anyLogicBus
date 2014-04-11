package com.logicbus.client;

/**
 * 服务器响应
 * @author duanyy
 * @since 1.0.4
 */
public class Response extends Buffer {
	public Response(int bufSize) {
		super(bufSize);
	}

	public Response(){
		this(2048);
	}
}
