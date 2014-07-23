package com.logicbus.backend.acm;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.logicbus.backend.AccessController;
import com.logicbus.backend.Context;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 基于ACM的访问控制器
 * 
 * @author duanyy
 * @since 1.2.3
 * 
 * @version 1.2.4.3 [20140709 duanyy]
 * - 在找不到ACM的情况下,使用缺省的ACM
 */
abstract public class ACMAccessController implements AccessController {
	/**
	 * 访问列表
	 */
	protected Hashtable<String,AccessStat> acl = new Hashtable<String,AccessStat>();

	/**
	 * 锁
	 */
	protected ReentrantLock lock = new ReentrantLock();
	
	/**
	 * ACM缓存管理器
	 */
	protected ACMCacheManager acmCache = null;
	
	/**
	 * 是否启用TokenCenter模式
	 */
	protected boolean tcMode = false;
		
	/**
	 * Token Holder
	 */
	protected TokenHolder tokenHolder = null;
	
	protected TokenCenterConnector tcc = null;
	
	protected String appField = "a";
	
	protected String defaultAcm = "default";
	
	public ACMAccessController(Properties props){
		acmCache = getCacheManager();
		tcMode = PropertiesConstants.getBoolean(props, "acm.tcMode", false);
		defaultAcm = PropertiesConstants.getString(props, "acm.default", defaultAcm);

		if (tcMode){
			tokenHolder = new TokenHolder(props);
		}
		appField = props.GetValue("acm.appArguName", appField);		
	}
	
	public TokenHolder getTokenHolder(){
		return tokenHolder;
	}
	
	/**
	 * 创建CacheManager
	 * @return
	 */
	protected ACMCacheManager getCacheManager(){
		return ACMCacheManager.get();
	}
	
	protected String getACMObject(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx){
		return sessionId + ":" + serviceId.getPath();
	}
	
	@Override
	public int accessStart(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		AccessControlModel acm = acmCache.get(sessionId);
		if (acm == null){
			acm = acmCache.get(defaultAcm);
			if (acm == null)
				return -2;
		}
		
		if (!servant.getVisible().equals("public")){
			//仅对非public服务进行控制
			if (tcMode){
				//从参数中获取Token
				String t = ctx.GetValue("token", "");
				if (t == null || t.length() <= 0){
					//没有按照协议要求传递token参数
					return -1;
				}
				//看看TokenHolder中有没有缓存该Token
				boolean found = tokenHolder.exist(t);
				if (!found){
					//调用TokenCenter查询Token是否有效
					String app = ctx.GetValue(appField, "Default");
					if (tcc == null){
						tcc = new TokenCenterConnector(Settings.get());
					}
					boolean valid = tcc.tokenIsValid(app, t);
					if (!valid){
						//连TokenCenter都说是非法
						return -3;
					}
					tokenHolder.add(t);
				}
			}
		}
		
		lock.lock();
		try{
			String acmObject = getACMObject(sessionId,serviceId,servant,ctx);
			AccessStat current = acl.get(acmObject);	
			if (current == null){
				current = new AccessStat();
				acl.put(acmObject, current);
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
			
			return acm.getPriority(ctx.getClientIp(), serviceId.getPath(), current);
		}finally{
			lock.unlock();
		}
	}

	@Override
	public int accessEnd(String sessionId,Path serviceId, ServiceDescription servant, Context ctx) {
		lock.lock();
		try{
			String acmObject = getACMObject(sessionId,serviceId,servant,ctx);
			AccessStat current = acl.get(acmObject);
			if (current != null){
				current.thread --;
			}
		}finally{
			lock.unlock();
		}
		return 0;
	}

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

}
