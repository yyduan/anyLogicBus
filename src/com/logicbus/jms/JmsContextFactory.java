package com.logicbus.jms;

import javax.jms.JMSException;

import com.anysoft.util.Factory;
import com.anysoft.util.Settings;


/**
 * JmsContext 工厂类
 * 
 * @author duanyy
 *
 */
public interface JmsContextFactory {

	/**
	 * 获取JmsContext
	 * @param id 
	 * @return
	 */
	public JmsContext getContext(String id) throws JMSException;

	/**
	 * 工厂类
	 * 
	 * <br>
	 * 用来创建JmsContextFactory.
	 * 
	 * @author duanyy
	 *
	 */
	public static class TheFactory extends Factory<JmsContextFactory>{
		public TheFactory(ClassLoader cl){
			super(cl);
		}
		
		protected static JmsContextFactory factory = null;
		
		public synchronized static JmsContextFactory getContextFactory(){
			if (factory == null){
				Settings settings = Settings.get();
				
				TheFactory f = new TheFactory((ClassLoader) settings.get("classLoader"));
				
				String module = settings.GetValue("jms.module", "com.logicbus.jms.DefaultContextFactory");
				
				factory = f.newInstance(module);
			}
			return factory;
		}
		
		public synchronized static JmsContext getContext(String id) throws JMSException{
			JmsContextFactory f = getContextFactory();
			return f.getContext(id);
		}
	}
}
