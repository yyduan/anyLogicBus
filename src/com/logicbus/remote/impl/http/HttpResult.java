package com.logicbus.remote.impl.http;

import java.util.Map;

import com.anysoft.util.JsonSerializer;
import com.anysoft.util.JsonTools;
import com.logicbus.remote.client.JsonBuffer;
import com.logicbus.remote.core.Builder;
import com.logicbus.remote.core.Result;

/**
 * 服务请求结果
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class HttpResult implements Result {
	protected JsonBuffer buffer = null;
	
	protected HttpResult(JsonBuffer _buf){
		buffer = _buf;
	}
	
	@Override
	public String getHost() {
		Map<String,Object> root = buffer.getRoot();
		return JsonTools.getString(root, "host", "");
	}

	@Override
	public String getCode() {
		Map<String,Object> root = buffer.getRoot();
		return JsonTools.getString(root, "code", "");
	}

	@Override
	public String getReason() {
		Map<String,Object> root = buffer.getRoot();
		return JsonTools.getString(root, "reason", "");
	}

	@Override
	public String getGlobalSerial() {
		Map<String,Object> root = buffer.getRoot();
		return JsonTools.getString(root, "serial", "");
	}

	@Override
	public long getDuration() {
		Map<String,Object> root = buffer.getRoot();
		return JsonTools.getLong(root, "duration", 0);
	}

	@Override
	public <data extends JsonSerializer> data getData(String id,
			Class<data> clazz) {
		data result = null;
		
		Map<String,Object> root = buffer.getRoot();
		Object found = root.get(id);
		if (found != null){
			if (found instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) found;
				try {
					result = clazz.newInstance();
					result.fromJson(map);
				} catch (Exception e) {
					// error occurs
				}
			}
		}
		return result;
	}

	@Override
	public <data extends JsonSerializer> data getData(String id, data result) {
		Map<String,Object> root = buffer.getRoot();
		Object found = root.get(id);
		if (found != null){
			if (found instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) found;
				result.fromJson(map);
			}
		}
		return result;
	}

	@Override
	public <data> data getData(String id, Builder<data> builder) {
		data result = null;
		
		Map<String,Object> root = buffer.getRoot();
		Object found = root.get(id);
		if (found != null){
			if (builder != null){
				result = builder.deserialize(id, found);
			}
		}
		return result;
	}

}
