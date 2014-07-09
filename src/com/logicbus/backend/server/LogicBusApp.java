package com.logicbus.backend.server;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;
import com.anysoft.webloader.WebApp;
import com.logicbus.backend.AccessController;
import com.logicbus.backend.BizLogger;
import com.logicbus.backend.Normalizer;
import com.logicbus.backend.QueuedServantFactory;
import com.logicbus.backend.timer.TimerManager;


/**
 * anyLogicBus基于anyWebLoader的应用
 * 
 * @author duanyy
 * @version 1.2.4.5 [20140709 duanyy]
 * - 增加扩展的配置文件
 * 
 */
public class LogicBusApp implements WebApp {
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LogManager.getLogger(LogicBusApp.class);
	
	/**
	 * 业务日志记录
	 */
	protected static BizLogger bizLogger = null;
	
	/**
	 * 启动定时器
	 * @param settings 参数
	 * @param resourceFactory 资源工厂
	 * @param classLoader
	 */
	private void startTimer(Properties settings,
			ResourceFactory resourceFactory,ClassLoader classLoader) {
		String __tmClass = settings.GetValue("timer.manager",
				"com.logicbus.backend.timer.TimerManager");
		
		String __timerConfig = settings.GetValue("timer.config.master", 
				"java:///com/logicbus/backend/timer/timer.xml#com.logicbus.backend.server.LogicBusApp");
		if (__timerConfig == null || __timerConfig.length() <= 0)
			return;

		String __timerSecondaryConfig = settings.GetValue("timer.config.secondary", 
				"java:///com/logicbus/backend/timer/timer.xml#com.logicbus.backend.server.LogicBusApp");
		
		logger.info("Start timer..");

		ResourceFactory rm = new ResourceFactory();
		InputStream in = null;
		try {
			in = rm.load(__timerConfig,__timerSecondaryConfig, null);
			Document doc = XmlTools.loadFromInputStream(in);
			// 启动定时器
			TimerManager __tm = TimerManager.get(__tmClass,classLoader);
			__tm.schedule(doc.getDocumentElement());

			logger.info("Start timer..OK!");
		} catch (Exception ex) {
			logger.error("Error loading xml file,source=" + __timerConfig, ex);
			logger.info("Start timer..Failed!");
		} finally {
			IOTools.closeStream(in);
		}
	}

	@Override
	public void init(DefaultProperties props,ServletContext sc) {
		Settings settings = Settings.get();
		settings.addSettings(props);
		
		// 初始化一些object		
		ClassLoader classLoader = (ClassLoader) sc.getAttribute("classLoader");
		if (classLoader == null){
			classLoader = LogicBusApp.class.getClassLoader();
		}
		settings.registerObject("classLoader", classLoader);
		
		//resourceFactory
		String rf = settings.GetValue("resource.factory","com.anysoft.util.resource.ResourceFactory");
		settings.registerObject("ResourceFactory", rf);
		ResourceFactory resourceFactory = (ResourceFactory) settings
				.get("ResourceFactory");

		//先装入扩展的配置文件
		String extProfile = settings.GetValue("settings.ext.master", "");
		String extSecondaryProfile = settings.GetValue("settings.ext.secondary", "");
		if (extProfile != null && extProfile.length() > 0 
				&& extSecondaryProfile != null && extSecondaryProfile.length() > 0){
			logger.info("Load ext xml settings");
			logger.info("Url = " + extProfile);
			settings.addSettings(extProfile,extSecondaryProfile,resourceFactory);
			logger.info("Load xml settings..OK!");
		}
		
		// 装入配置文件
		String profile = settings.GetValue("settings.master",
				"java:///com/logicbus/backend/server/http/profile.xml#com.logicbus.backend.server.LogicBusApp");	
		String secondary_profile = settings.GetValue("settings.secondary",
				"java:///com/logicbus/backend/server/http/profile.xml#com.logicbus.backend.server.LogicBusApp");
		
		logger.info("Load xml settings..");
		logger.info("Url = " + profile);
		settings.addSettings(profile,secondary_profile,resourceFactory);
		logger.info("Load xml settings..OK!");

		String encoding = settings.GetValue("http.encoding","utf-8");
		XmlTools.setDefaultEncoding(encoding);
		
		String acClass = settings.GetValue("acm.module", 
				"com.logicbus.backend.IpAndServiceAccessController");
		AccessController.TheFactory acf = new AccessController.TheFactory(classLoader);
		AccessController ac = acf.newInstance(acClass,settings);
		settings.registerObject("accessController", ac);
	
		String normalizerClass = settings.GetValue("normalizer.module", 
				"com.logicbus.backend.DefaultNormalizer");
		Normalizer.TheFactory ncf = new Normalizer.TheFactory(classLoader);
		Normalizer normalizer = ncf.newInstance(normalizerClass);
		settings.registerObject("normalizer", normalizer);

		//初始化BizLogger
		String bizLoggerClass = PropertiesConstants.getString(settings, "bizlog.logger", "com.logicbus.backend.bizlog.DefaultBizLogger");		
		BizLogger.TheFactory factory = new BizLogger.TheFactory();		
		bizLogger = factory.newInstance(bizLoggerClass, settings);
		settings.registerObject("bizLogger", bizLogger);
		String bizLogHome = PropertiesConstants.getString(settings, "bizlog.home", "");
		if (bizLogHome == null || bizLogHome.length() <= 0){
			logger.info("bizlog.home is not set.Set it to /var/log/bizlog");
			settings.SetValue("bizlog.home","var/log/bizlog");
		}
		
		// 启动定时器
		startTimer(settings, resourceFactory,classLoader);
	}

	@Override
	public void destroy(ServletContext sc) {
		TimerManager __tm = TimerManager.get();
		logger.info("Stop timer..");
		__tm.stop();
		
		QueuedServantFactory sf = QueuedServantFactory.get();
		logger.info("Close servant factory...");
		sf.close();
		
		if (bizLogger != null){
			bizLogger.close();
		}
	}
}
