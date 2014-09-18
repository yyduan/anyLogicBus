package com.logicbus.backend.stats.context;

import com.anysoft.util.Watcher;
import com.anysoft.util.XMLConfigurable;
import com.logicbus.backend.stats.core.MetricsHandler;

/**
 * MetricsHandler上下文
 * @author duanyy
 *
 */
public interface MetricsHandlerContext extends XMLConfigurable,AutoCloseable{
	/**
	 * 获取指定ID的MetricsHandler
	 * @param id
	 * @return
	 */
	public MetricsHandler getHandler(String id);
	
	/**
	 * 注册监控器
	 * @param watcher
	 */
	public void addWatcher(Watcher<MetricsHandler> watcher);
	
	/**
	 * 注销监控器
	 * @param watcher
	 */
	public void removeWatcher(Watcher<MetricsHandler> watcher);
}
