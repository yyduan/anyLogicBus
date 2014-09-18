package com.logicbus.backend.stats.core;

import java.io.InputStream;




import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.anysoft.stream.Handler;
import com.anysoft.util.Factory;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 指标接口
 * 
 * @author duanyy
 * @since 1.2.8
 */
public interface MetricsHandler extends Handler<Fragment>,MetricsCollector{
	public static class TheFactory extends Factory<MetricsHandler>{
		/**
		 * a logger of log4j
		 */
		protected static final Logger logger = LogManager.getLogger(TheFactory.class);
		
		/**
		 * 根据环境变量中的配置来创建MetricsHandler
		 * @param props
		 * @return
		 */
		public static MetricsHandler getInstance(Properties props){
			String master = props.GetValue("metrics.client.master", 
					"java:///com/logicbus/backend/stats/metrics.client.xml#com.logicbus.backend.stats.MetricsHandler");
			String secondary = props.GetValue("metrics.client.master", 
					"java:///com/logicbus/backend/stats/metrics.client.xml#com.logicbus.backend.stats.MetricsHandler");
			
			ResourceFactory rf = Settings.getResourceFactory();
			
			InputStream in = null;
			try {
				in = rf.load(master,secondary, null);
				Document doc = XmlTools.loadFromInputStream(in);		
				if (doc != null){
					return getInstance(doc.getDocumentElement(),props);
				}
			}catch (Throwable ex){
				logger.error("Error occurs when load xml file,source=" + master, ex);
			}finally {
				IOTools.closeStream(in);
			}
			return null;
		}		
		protected static TheFactory instance = new TheFactory();
		
		public static MetricsHandler getInstance(Element e,Properties p){
			return instance.newInstance(e, p);
		}
	}	
}
