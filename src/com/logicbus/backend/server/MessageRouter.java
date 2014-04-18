package com.logicbus.backend.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.logicbus.backend.AccessController;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.ServantFactory;
import com.logicbus.backend.ServantPool;
import com.logicbus.backend.ServantWorkerThread;

import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.catalog.Path;

/**
 * 消息路由器
 * 
 * @author duanyy
 * 
 * @version 1.0.1 [20140402 duanyy] <br>
 * - {@link com.logicbus.backend.AccessController AccessController}有更新<br>
 * 
 * @version 1.0.2 [20140407 duanyy] <br>
 * - 采用{@link java.util.concurrent.CountDownLatch CountDownLatch}来和工作进程通讯.<br>
 * 
 * @version 1.0.5 [20140412 duanyy] <br>
 * - 改进消息传递模型 <br>
 */
public class MessageRouter {
	
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LogManager.getLogger(MessageRouter.class);
	
	/**
	 * 服务调用
	 * @param id 服务id
	 * @param mDoc 消息文档
	 * @param ctx 上下文
	 * @param ac 访问控制器
	 * @return 
	 */
	static public int action(Path id,MessageDoc mDoc,Context ctx,AccessController ac){
		mDoc.setStartTime(System.currentTimeMillis());
		
		ServantPool pool = null;
		Servant servant = null;		
		String sessionId = "";
		CountDownLatch latch = new CountDownLatch(1);
		try{
			ServantFactory factory = ServantFactory.get();
			pool = factory.getPool(id);		
			if (!pool.isRunning()){
				throw new ServantException("core.service_paused",
						"The Service is paused:service id:" + id);
			}

			int priority = 0;
			
			if (null != ac){
				sessionId = ac.createSessionId(id, pool.getDescription(), ctx);
				priority = ac.accessStart(sessionId,id, pool.getDescription(), ctx);
				if (priority < 0){
					mDoc.setReturn("client.permission_denied","Permission denied！service id: "+ id);
					return 0;
				}
			}

			servant = pool.getServant(priority);

			ServantWorkerThread thread = new ServantWorkerThread(servant,mDoc,ctx,latch);
			thread.start();
			if (!latch.await(servant.getTimeOutValue(), TimeUnit.MILLISECONDS)){
				mDoc.setReturn("core.time_out","Time out or interrupted.");
			}
			thread = null;
			
		}catch (ServantException ex){
			mDoc.setReturn(ex.getCode(), ex.getMessage());
			logger.error(ex.getCode() + ":" + ex.getMessage());
		}catch (Exception ex){
			mDoc.setReturn("core.fatalerror",ex.getMessage());
			logger.error("core.fatalerror:" + ex.getMessage());
		}catch (Throwable t){
			mDoc.setReturn("core.fatalerror",t.getMessage());
			logger.error("core.fatalerror:" + t.getMessage());			
		}
		finally {
			if (servant != null){
				servant.setState(Servant.STATE_IDLE);		
			}
			mDoc.setEndTime(System.currentTimeMillis());
			if (pool != null){
				pool.visited(mDoc.getDuration(),mDoc.getReturnCode());
				if (ac != null){
					ac.accessEnd(sessionId,id, pool.getDescription(), ctx);
				}				
			}
		}
		return 0;
	}
}
