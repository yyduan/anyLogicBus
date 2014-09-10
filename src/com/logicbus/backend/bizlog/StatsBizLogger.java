package com.logicbus.backend.bizlog;

import org.w3c.dom.Element;

import com.anysoft.stream.AbstractHandler;
import com.anysoft.stream.DispatchHandler;
import com.anysoft.stream.HubHandler;
import com.anysoft.util.Factory;
import com.anysoft.util.Properties;
import com.anysoft.util.XmlTools;
import com.anysoft.stream.Handler;
import com.logicbus.backend.bizlog.stats.BizLogStatsItem;

/**
 * 用于统计的BizLogger
 * @author duanyy
 * 
 * @since 1.2.7.1
 * 
 * @version 1.2.7.2 [20140903 duanyy]
 * - 优化BizLogStatsItem数据结构
 * 
 */
public class StatsBizLogger extends AbstractHandler<BizLogItem> implements
		BizLogger {
	protected Handler<BizLogStatsItem> handler = null;

	@Override
	protected void onHandle(BizLogItem _data) {
		if (handler != null){
			BizLogStatsItem statsItem = 
					(new BizLogStatsItem(_data.id)).incr(1,_data.result.equals("core.ok")?0:1, _data.duration);
	
			handler.handle(statsItem);
		}
	}

	@Override
	protected void onFlush() {
		if (handler != null){
			handler.flush();
		}
	}

	@Override
	protected void onConfigure(Element e, Properties p) {
		Element stats = XmlTools.getFirstElementByPath(e, getHandlerType());
		if (stats != null){
			handler = TheFactory.getInstance(stats, p);
		}
	}
	
	public void close() throws Exception{
		super.close();
		if (handler != null){
			handler.close();
		}
	}
	
	public String getHandlerType(){
		return "stats";
	}
	
	public static class Dispatch extends DispatchHandler<BizLogStatsItem>{
		public String getHandlerType(){
			return "stats";
		}
	}
	
	public static class Hub extends HubHandler<BizLogStatsItem>{
		public String getHandlerType(){
			return "stats";
		}
	}
	
	public static class TheFactory extends Factory<Handler<BizLogStatsItem>>{
		protected static TheFactory instance = new TheFactory();
		
		public static Handler<BizLogStatsItem> getInstance(Element e,Properties p){
			return instance.newInstance(e, p);
		}
	}
}
