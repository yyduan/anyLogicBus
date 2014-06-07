package com.logicbus.backend;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServantManager;
import com.logicbus.models.servant.ServiceDescription;
import com.logicbus.models.servant.ServiceDescriptionWatcher;


/**
 * 服务员工厂
 * @author duanyy
 * @version 1.2.0 [20140607 duanyy]修正无法reload的bug
 */
public class ServantFactory implements ServiceDescriptionWatcher{
	/**
	 * a logger of log4j
	 */
	protected Logger logger = LogManager.getLogger(ServantFactory.class);
	/**
	 * 服务资源池列表
	 */
	private Hashtable<String, ServantPool> m_pools = null;
	
	/**
	 * constructor
	 */
	protected ServantFactory(){
		ServantManager sm = ServantManager.get();
		sm.addWatcher(this);
		m_pools = new Hashtable<String, ServantPool>();
	}
	
	/**
	 * 获得服务资源池列表
	 * @return 服务资源池列表
	 */
	public ServantPool [] getPools(){
		return m_pools.values().toArray(new ServantPool[0]);
	}
	
	/**
	 * 获取指定服务的服务资源池
	 * @param id 服务ID
	 * @return 服务资源池
	 * @throws ServantException 当没有找到服务定义时抛出
	 */
	protected ServantPool getServantPool(Path id)throws ServantException
	{
		ServantManager sm = ServantManager.get();
		ServiceDescription sd = sm.get(id);
		if (sd == null){
			throw new ServantException("core.service_not_found","No service desc is found:" + id);
		}
		
		return new ServantPool(sd);		
	}
	
	
	/**
	 * 重新装入指定服务的资源池
	 * @param _id 服务id
	 * @return 服务资源池
	 * @throws ServantException
	 */
	public synchronized ServantPool reloadPool(Path _id) throws ServantException{
		synchronized (m_pools){
			ServantPool temp = m_pools.get(_id.getPath());
			if (temp != null){
				//重新装入的目的是因为更新了服务描述信息			
				ServantManager sm = ServantManager.get();
				ServiceDescription sd = sm.get(_id);
				temp.reload(sd);
			}
			return temp;
		}
	}
	
	/**
	 * 获取指定服务的的服务资源池
	 * @param _id 服务Id
	 * @return 服务资源池
	 * @throws ServantException
	 * @see {@link #getServantPool(String)}
	 */
	public ServantPool getPool(Path _id) throws ServantException{
		synchronized (m_pools){
			Object temp = m_pools.get(_id.getPath());
			if (temp != null){		
				ServantPool pool = (ServantPool)temp;
				return pool;
			}

			ServantPool newPool = getServantPool(_id);
			if (newPool != null)
			{
				m_pools.put(_id.getPath(), newPool);
				return newPool;
			}
			return null;
		}
	}
	
	/**
	 * 关闭
	 */
	public void close(){
		synchronized (m_pools){
			Enumeration<ServantPool> pools = m_pools.elements();
			
			while (pools.hasMoreElements()){
				ServantPool sp = pools.nextElement();
				if (sp != null){
					sp.close();
				}
			}
		}
	}
	
	/**
	 * 唯一实例
	 */
	protected static ServantFactory instance = null;
	
	/**
	 * 获取唯一实例
	 * @return 唯一实例
	 */
	synchronized static public ServantFactory get(){
		if (instance != null){
			return instance;
		}
		instance = new ServantFactory();
		return instance;
	}

	@Override
	public void changed(Path id, ServiceDescription desc) {
		synchronized (m_pools){
			logger.info("changed" + id);
			ServantPool temp = m_pools.get(id);
			if (temp != null){
				//重新装入的目的是因为更新了服务描述信息			
				logger.info("Service has been changed,reload it:" + id);
				temp.reload(desc);
			}
		}
	}
	
	@Override
	public void removed(Path id){
		synchronized (m_pools){
			logger.info("removed:" + id);
			ServantPool temp = m_pools.get(id);
			if (temp != null){
				//服务被删除了
				logger.info("Service has been removed,close it:" + id);
				temp.close();
				m_pools.remove(id);
			}
		}		
	}
}
