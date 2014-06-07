package com.logicbus.backend;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.anysoft.util.IOTools;
import com.anysoft.util.Settings;
import com.logicbus.models.servant.ServiceDescription;


/**
 * 服务资源池
 * 
 * @author duanyy
 *
 */
public class ServantPool {
	
	/**
	 * 服务描述
	 */
	private ServiceDescription m_desc;
	
	/**
	 * 资源池中服务员个数
	 */
	private int m_servant_count;
	
	/**
	 * 服务统计
	 */
	private ServantStat m_stat;
	
	/**
	 * 获取服务描述
	 * @return ServiceDescription
	 */
	public ServiceDescription getDescription(){return m_desc;}
	
	/**
	 * 获取服务统计
	 * @return
	 */
	public ServantStat getStat(){return m_stat;}
	
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LogManager.getLogger(ServantPool.class);
	
	/**
	 * 设置资源池为暂停
	 */
	public void pause(){
		m_stat.status = "pause";
	}
	/**
	 * 恢复资源池为运行
	 */
	public void resume(){
		m_stat.status = "running";
	}
	/**
	 * 判断资源池是否运行状态
	 * @return 
	 */
	public boolean isRunning(){
		return m_stat.status.equals("running");
	}
	
	/**
	 * 通过服务描述构造资源池
	 * @param sd 服务描述
	 */
	public ServantPool(ServiceDescription sd){
		m_desc = sd;
		m_stat = new ServantStat();
		{
			String value;
			value = m_desc.getProperties().GetValue("service.maxcount","5");
			m_servant_count = Integer.parseInt(value);
			
			value = m_desc.getProperties().GetValue("service.monitor", 
					"step=60;times_rras=SUM:60:720,SUM:3600:720;duration_rras=SUM:60:720,SUM:3600:720;error_rras=SUM:60:720,SUM:3600:720");
			if (value != null && value.length() > 0){
				m_stat.setMonitor(value);
			}
		}

		logger.info("Initialize the servant pool..");
		logger.info("Id:" + m_desc.getServiceID());
		logger.info("Name:" + m_desc.getName());
		logger.info("Module:" + m_desc.getModule());
		logger.info("Pool Size:" + m_servant_count);
		
		m_servants = new Servant[m_servant_count];
	}
	
	/**
	 * 重新装入服务资源池
	 * 
	 * <br>目的是按照新的服务描述装入资源池
	 * 
	 * @param sd 服务描述
	 */
	public void reload(ServiceDescription sd){
		m_desc = sd;
		close();
	}
	
	/**
	 * 关闭资源池
	 * 
	 */
	public void close(){
		if (m_servants != null){
			synchronized (m_servants){
				for (int i = 0 ; i < m_servants.length ; i ++){
					Servant s = m_servants[i];
					if (s != null){
						s.destroy();
					}
					m_servants[i] = null;
				}
			}
		}
		
		if (m_highpriority_servants != null){
			synchronized(m_highpriority_servants){
				for (int i = 0 ; i < m_highpriority_servants.length ; i ++){
					Servant s = m_highpriority_servants[i];
					if (s != null){
						s.destroy();
					}
				}
			}
		}
	}
	
	/**
	 * 访问一次
	 * @param duration 本次访问的时长
	 * @param code 本次访问的错误代码
	 */
	synchronized public void visited(long duration,String code){
		m_stat.visited(duration,code);
	}
	
	/**
	 * 低优先级服务资源池
	 */
	private Servant[] m_servants = null;
	
	/**
	 * 高优先级服务资源池 
	 */
	private Servant[] m_highpriority_servants = null;
	
	/**
	 * 从指定资源池中获取空闲的服务员
	 * @param queue 资源池队列
	 * @return 满足条件的服务员
	 * @throws ServantException
	 */
	private Servant getServant(Servant[] queue)throws ServantException{
		for (int i = 0 ; i < m_servant_count ; i ++)
		{
			if (queue[i] == null)
			{
				Servant servant = CreateServant(m_desc);
				if (servant != null){
					m_servants[i] = servant;
					servant.setState(Servant.STATE_BUSY);
					return servant;
				}
				throw new ServantException("core.error_module","Can not create instance:" + m_desc.getModule());
			}
			else
			{
				Servant servant = (Servant)queue[i];
				if (servant.getState() == Servant.STATE_IDLE){
					servant.setState(Servant.STATE_BUSY);
					return servant;
				}
				continue;
			}
		}
		return null;
	}
	
	/**
	 * 按照优先级获取空闲的服务员
	 * @param priority 优先级
	 * @return 满足条件的服务员
	 * @throws ServantException
	 */
	public synchronized Servant getServant(int priority) throws ServantException{
		
		Servant found = null; 
		synchronized (m_servants){
			found = getServant(m_servants);
		}
		if (found == null){
			if (priority > 1){
				if (m_highpriority_servants == null){
					m_highpriority_servants = new Servant[m_servant_count];
				}
				synchronized (m_highpriority_servants){
					found = getServant(m_highpriority_servants);
				}
			}
		}
		if (found != null) return found;
		throw new ServantException("core.service_busy","The service is so busy,another time please!");
	}
	
	/**
	 * 根据服务描述创建服务员
	 * @param desc 服务描述
	 * @return 服务员
	 * @throws ServantException
	 */
	protected Servant CreateServant(ServiceDescription desc) throws ServantException{
		String class_name = desc.getModule();
		Servant temp = null;
		try {
			
			//ClassLoader采用当前ClassLoader
			//1.2.0
			ClassLoader cl = null;
			{
				Settings settings = Settings.get();
				cl = (ClassLoader)settings.get("classLoader");
			}
			cl = cl == null ? Thread.currentThread().getContextClassLoader() : cl;
			
			String [] modules = desc.getModules();
			if (modules != null && modules.length > 0){
				logger.info("Load class from remote..");
				URL[] urls = new URL[modules.length];
				int i = 0;
				for (String module:modules){
					String url = desc.getProperties().transform(module);
					urls[i] = new URL(url);
					logger.info("url=" + url);
					i++;
				}
				URLClassLoader classLoader = new URLClassLoader(urls,cl);
				try {
					temp = (Servant)classLoader.loadClass(class_name).newInstance();
				}finally{
					if (classLoader != null){
						IOTools.closeStream(classLoader);
					}
				}
			}else{
				temp = (Servant)(cl.loadClass(class_name).newInstance());
			}
			temp.create(desc);			
			return temp;
		}catch (ServantException e){
			throw e;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new ServantException("core.error_module",e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ServantException("core.error_module",e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ServantException("core.error_module",e.getMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServantException("core.error_remote_module",e.getMessage());
		}
	}
}
