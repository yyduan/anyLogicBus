package com.logicbus.backend;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Properties;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * AccessController的实现
 * 
 * <p>本实现提供了基于SessionID的访问控制方式，提供了并发数，一分钟之内的调用次数等变量.
 * 
 * <p>本类是一个虚类，需要子类做进一步细化，包括：<br>
 * - SessionID如何组成？<br>
 * - 如何根据等待队列长度，最近一分钟之内的调用次数等变量判断访问权限<br>
 * 
 * @author duanyy
 * 
 * @version 1.0.1 [20140402 duanyy] <br>
 * - {@link com.logicbus.backend.AccessController AccessController}有更新
 * 
 * @version 1.2.1 [20140613 duanyy] <br>
 * - 共享锁由synchronized改为ReentrantLock
 */
abstract public class AbstractAccessController implements AccessController {
	/**
	 * 访问列表
	 */
	protected Hashtable<String,AccessStat> acl = new Hashtable<String,AccessStat>();

	/**
	 * 锁
	 */
	protected ReentrantLock lock = new ReentrantLock();

	public AbstractAccessController(Properties props){
		
	}
	
	@Override
	public int accessEnd(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		lock.lock();
		try {
			AccessStat current = acl.get(sessionId);
			if (current != null){
				current.thread --;
			}
		}finally{
			lock.unlock();
		}
		return 0;
	}

	@Override
	public int accessStart(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		lock.lock();
		try {
			AccessStat current = acl.get(sessionId);	
			if (current == null){
				current = new AccessStat();
				acl.put(sessionId, current);
			}
			
			current.timesTotal ++;
			current.thread ++;
			current.waitCnt = lock.getQueueLength();
			
			long timestamp = System.currentTimeMillis();
			timestamp = (timestamp / 60000)*60000;
			if (timestamp != current.timestamp){
				//新的周期
				current.timesOneMin = 1;
				current.timestamp = timestamp;
			}else{
				current.timesOneMin ++;
			}
			
			return getClientPriority(serviceId,servant,ctx,current);
		}finally{
			lock.unlock();
		}
	}
		
	/**
	 * 获取控制优先级
	 * @param serviceId 服务ID
	 * @param servant 服务描述
	 * @param ctx 上下文
	 * @param stat 当前Session的访问统计
	 * @return 优先级
	 */
	abstract protected int getClientPriority(Path serviceId,ServiceDescription servant,
			Context ctx,AccessStat stat);
	
	@Override
	public void toXML(Element root) {
		Document doc = root.getOwnerDocument();
		
		Enumeration<String> keys = acl.keys();
		while (keys.hasMoreElements()){
			String key = keys.nextElement();
			AccessStat value = acl.get(key);
			Element eAcl = doc.createElement("acl");
			
			eAcl.setAttribute("session", key);
			eAcl.setAttribute("currentThread", String.valueOf(value.thread));
			eAcl.setAttribute("timesTotal", String.valueOf(value.timesTotal));
			eAcl.setAttribute("timesOneMin",String.valueOf(value.timesOneMin));
			eAcl.setAttribute("waitCnt", String.valueOf(value.waitCnt));
			
			root.appendChild(eAcl);
		}
	}

	/**
	 * 访问统计
	 * @author duanyy
	 *
	 */
	public static class AccessStat {
		/**
		 * 总调用次数
		 */
		protected long timesTotal = 0;
		/**
		 * 最近一分钟调用次数
		 */
		protected int timesOneMin = 0;
		/**
		 * 当前接入进程个数
		 */
		protected int thread = 0;
		/**
		 * 时间戳(用于定义最近一分钟)
		 */
		protected long timestamp = 0;
		
		/**
		 * 等待进程数
		 * @since 1.2.1
		 */
		protected int waitCnt = 0;
	}
}
