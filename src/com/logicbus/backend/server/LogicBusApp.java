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
import com.logicbus.backend.DefaultNormalizer;
import com.logicbus.backend.IpAndServiceAccessController;
import com.logicbus.backend.Normalizer;
import com.logicbus.backend.QueuedServantFactory;
import com.logicbus.backend.ServantFactory;
import com.logicbus.backend.bizlog.DefaultBizLogger;
import com.logicbus.backend.timer.TimerManager;


/**
 * anyLogicBus基于anyWebLoader的应用
 * 
 * @author duanyy
 * @version 1.2.4.5 [20140709 duanyy]
 * - 增加扩展的配置文件
 * 
 * @version 1.2.5 [20140723 duanyy]
 * - 修正ResourceFactory的bug
 * 
 * @version 1.2.6 [20140807 duanyy] <br>
 * - ServantPool和ServantFactory插件化
 */
public class LogicBusApp implements WebApp {
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LogManager.getLogger(LogicBusApp.class);
		
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
		ResourceFactory resourceFactory = null;
		try {
			logger.info("Use resource factory:" + rf);
			resourceFactory = (ResourceFactory) classLoader.loadClass(rf).newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			logger.error("Can not create instance of :" + rf);
		}
		if (resourceFactory == null){
			resourceFactory = new ResourceFactory();
			logger.info("Use default:" + ResourceFactory.class.getName());
		}
		settings.registerObject("ResourceFactory", resourceFactory);
		
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
		
		//初始化AccessController
		{
			String acClass = settings.GetValue("acm.module", 
					"com.logicbus.backend.IpAndServiceAccessController");
			
			logger.info("AccessController is initializing,module:" + acClass);
			AccessController ac = null;
			try {
				AccessController.TheFactory acf = new AccessController.TheFactory(classLoader);
				ac = acf.newInstance(acClass,settings);
			}catch (Throwable t){
				ac = new IpAndServiceAccessController(settings);
				logger.error("Failed to initialize AccessController.Using default:" + IpAndServiceAccessController.class.getName());
			}
			settings.registerObject("accessController", ac);
		}
		//初始化Normalizer
		{
			String normalizerClass = settings.GetValue("normalizer.module", 
				"com.logicbus.backend.DefaultNormalizer");
			
			logger.info("Normalizer is initializing,module:" + normalizerClass);
			Normalizer normalizer = null;
			try {
				Normalizer.TheFactory ncf = new Normalizer.TheFactory(classLoader);
				normalizer = ncf.newInstance(normalizerClass);
			}catch (Throwable t){
				normalizer = new DefaultNormalizer();
				logger.error("Failed to initialize Normalizer.Using default:" + DefaultNormalizer.class.getName());
			}
			settings.registerObject("normalizer", normalizer);
		}
		//初始化BizLogger
		{
			String bizLoggerClass = PropertiesConstants.getString(settings, "bizlog.logger", "com.logicbus.backend.bizlog.DefaultBizLogger");
			logger.info("BizLogger is initializing,module:" + bizLoggerClass);								
			BizLogger bizLogger = null;
			try {
				BizLogger.TheFactory factory = new BizLogger.TheFactory();
				bizLogger = factory.newInstance(bizLoggerClass, settings);
			}catch (Throwable t){
				bizLogger = new DefaultBizLogger(settings);
				logger.error("Failed to initialize bizLogger.Using default:" + DefaultBizLogger.class.getName());				
			}
			settings.registerObject("bizLogger", bizLogger);
						
			String bizLogHome = PropertiesConstants.getString(settings, "bizlog.home", "");
			if (bizLogHome == null || bizLogHome.length() <= 0){
				logger.info("bizlog.home is not set.Set it to /var/log/bizlog");
				settings.SetValue("bizlog.home","var/log/bizlog");
			}
		}
		//初始化servantFactory
		{
			String sfClass = PropertiesConstants.getString(settings, "servant.factory", "com.logicbus.backend.QueuedServantFactory");
			logger.info("Servant Factory is initializing,module:" + sfClass);
			ServantFactory sf = null;
			try {
				ServantFactory.TheFactory sfFactory = new ServantFactory.TheFactory();
				sf = sfFactory.newInstance(sfClass, settings);
			}catch (Throwable t){
				sf = new QueuedServantFactory(settings);
				logger.error("Failed to initialize servantFactory.Using default:" + QueuedServantFactory.class.getName());
			}
			settings.registerObject("servantFactory", sf);
		}
		// 启动定时器
		startTimer(settings, resourceFactory,classLoader);
	}

	@Override
	public void destroy(ServletContext sc) {
		TimerManager __tm = TimerManager.get();
		logger.info("Stop timer..");
		__tm.stop();
		
		Settings settings = Settings.get();
		
		ServantFactory sf = (ServantFactory)settings.get("servantFactory");
		if (sf != null){
			logger.info("The servantFactory is closing..");
			try {
				sf.close();
			}catch (Throwable t){
				
			}
		}

		BizLogger bizLogger = (BizLogger)settings.get("bizLogger");
		if (bizLogger != null){
			logger.info("The bizLogger is closing..");
			try {
				bizLogger.close();
			}catch (Throwable t){
				
			}
		}
	}
}
