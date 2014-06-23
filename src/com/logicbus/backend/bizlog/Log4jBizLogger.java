package com.logicbus.backend.bizlog;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.logicbus.backend.BizLogItem;


/**
 * 输出到Log4j的BizLogger
 * 
 * @author duanyy
 *
 * @since 1.2.3
 */
public class Log4jBizLogger extends AbstractBizLogger {
	protected Logger logger = null;
	protected int thread = 0;
	public Log4jBizLogger(Properties props) {
		super(props);

		thread = PropertiesConstants.getInt(props, "thread", 0);
				
		delimeter = PropertiesConstants.getString(props,
				"bizlog.log4j.delimeter", delimeter);
		
		isBilling = PropertiesConstants.getBoolean(props, 
				"bizlog.isBilling", isBilling);
	}

	protected String delimeter = "%%";
	protected boolean isBilling = true;
	protected StringBuffer buf = new StringBuffer();
	
	@Override
	protected void onLog(BizLogItem item) {
		buf.setLength(0);
		
		buf.append(isBilling?1:0).append(delimeter)
		.append(item.sn).append(delimeter)
		.append(item.startTime).append(delimeter)
		.append(item.host).append(delimeter)
		.append(item.clientIP).append(delimeter)
		.append(item.client).append(delimeter)
		.append(item.duration).append(delimeter)
		.append(item.id).append(delimeter)
		.append(item.result).append(delimeter)
		.append(item.result.equals("core.ok")?"":item.reason).append(delimeter)
		.append(item.url).append(delimeter);
		
		if (item.content != null && item.content.length() > 0){
			buf.append(item.content.replaceAll("\n", "").replaceAll("\r",""));
		}

		if (logger == null){
			Properties props = new DefaultProperties("Default",Settings.get());
			props.SetValue("thread", String.valueOf(thread));
			logger = initLogger(props);
		}
		
		logger.info(buf.toString());
	}
	
	private Logger initLogger(Properties props) {
		Logger _logger = LogManager.getLogger(Log4jBizLogger.class.getName() + "." + thread);
		_logger.setAdditivity(false);
		
		DailyRollingFileAppender myAppender = new DailyRollingFileAppender();
		myAppender.setFile(PropertiesConstants.getString(props,
				"bizlog.log4j.file",
				"${bizlog.home}/bizlog${server.port}_${thread}.log"));
		myAppender.setDatePattern(PropertiesConstants.getString(props,
				"bizlog.log4j.datePattern", "'.'yyyy-MM-dd-HH-mm"));
		myAppender.setEncoding(PropertiesConstants.getString(props,
				"bizlog.log4j.encoding", "${http.encoding}"));
		myAppender.setBufferSize(PropertiesConstants.getInt(props,
				"biz.log4j.bufferSize", 10240));
		myAppender.setBufferedIO(PropertiesConstants.getBoolean(props,
				"bizlog.log4j.bufferedIO", true));
		myAppender.setImmediateFlush(PropertiesConstants.getBoolean(props,
				"bizlog.log4j.immediateFlush", false));
		myAppender.setLayout(new MyLayout());
		myAppender.setName(Log4jBizLogger.class.getName() + "." + thread);
		
		myAppender.activateOptions();
		_logger.addAppender(myAppender);
		
		return _logger;
	}

	/**
	 * 自定义的Layout
	 * 
	 * <br>
	 * BizLog输出格式固定，因此自定义一个Layout提高效率。
	 * 
	 * @author duanyy
	 *
	 * @since 1.2.3
	 */
	public static class MyLayout extends Layout{
		protected static String lineSeperator = System.getProperty("line.separator");
		@Override
		public void activateOptions() {
		}

		@Override
		public String format(LoggingEvent e) {
			return e.getRenderedMessage() + lineSeperator;
		}

		@Override
		public boolean ignoresThrowable() {
			return true;
		}
		
	}
}
