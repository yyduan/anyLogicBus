package com.logicbus.backend.server;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.anysoft.util.DefaultProperties;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;
import com.anysoft.webloader.WebApp;
import com.logicbus.backend.AccessController;
import com.logicbus.backend.Normalizer;
import com.logicbus.backend.ServantFactory;
import com.logicbus.backend.timer.TimerManager;


/**
 * anyLogicBus基于anyWebLoader的应用
 * 
 * @author duanyy
 *
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
		String __tmClass = settings.GetValue("module.timermanager",
				"com.logicbus.backend.timer.TimerManager");
		
		String __timerConfig = settings.GetValue("master.timer.config", 
				"java:///com/logicbus/backend/timer/timer.xml#com.logicbus.backend.server.LogicBusApp");
		if (__timerConfig == null || __timerConfig.length() <= 0)
			return;

		String __timerSecondaryConfig = settings.GetValue("secondary.timer.config", 
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
		String rf = settings.GetValue("module.resourcefactory","com.anysoft.util.resource.ResourceFactory");
		settings.registerObject("ResourceFactory", rf);
		ResourceFactory resourceFactory = (ResourceFactory) settings
				.get("ResourceFactory");
				
		// 装入配置文件
		String profile = settings.GetValue("master.config",
				"java:///com/logicbus/backend/server/http/profile.xml#com.logicbus.backend.server.LogicBusApp");	
		String secondary_profile = settings.GetValue("secondary.config",
				"java:///com/logicbus/backend/server/http/profile.xml#com.logicbus.backend.server.LogicBusApp");
		
		logger.info("Load xml settings..");
		logger.info("Url = " + profile);
		settings.addSettings(profile,secondary_profile,resourceFactory);
		logger.info("Load xml settings..OK!");

		String encoding = settings.GetValue("http.encoding","utf-8");
		XmlTools.setDefaultEncoding(encoding);
		
		String acClass = settings.GetValue("module.accesscontroller", 
				"com.logicbus.backend.IpAndServiceAccessController");
		AccessController.TheFactory acf = new AccessController.TheFactory(classLoader);
		AccessController ac = acf.newInstance(acClass);
		settings.registerObject("accessController", ac);
	
		String normalizerClass = settings.GetValue("module.normalizer", 
				"com.logicbus.backend.DefaultNormalizer");
		Normalizer.TheFactory ncf = new Normalizer.TheFactory(classLoader);
		Normalizer normalizer = ncf.newInstance(normalizerClass);
		settings.registerObject("normalizer", normalizer);
		
		// 启动定时器
		startTimer(settings, resourceFactory,classLoader);
	}

	@Override
	public void destroy(ServletContext sc) {
		TimerManager __tm = TimerManager.get();
		logger.info("Stop timer..");
		__tm.stop();
		
		ServantFactory sf = ServantFactory.get();
		logger.info("Close servant factory...");
		sf.close();
	}
}