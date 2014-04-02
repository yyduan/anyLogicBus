package com.logicbus.backend.server;

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
 * - {@link com.logicbus.backend.AccessController AccessController}有更新
 * 
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
		ctx.setStartTime(System.currentTimeMillis());
		
		ServantPool pool = null;
		Servant servant = null;		
		String sessionId = "";
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
					ctx.setReturnCode("client.permission_denied");
					ctx.setReason("Permission denied！service id: "+ id);
					return 0;
				}
			}

			servant = pool.getServant(priority);

			ServantWorkerThread thread = new ServantWorkerThread(servant,mDoc,ctx);
			long start_time = System.currentTimeMillis();
			thread.start();
			Thread.sleep(10);
			while (!thread.isFinished()){
				Thread.sleep(150);
				if (servant.isTimeOut(start_time)){
					break;
				}
			}
			
			if (!thread.isFinished()){
				thread.interrupt();
				ctx.setReturnCode("core.time_out");
				ctx.setReason("Time out.");
			}
			
			thread = null;
		}catch (ServantException ex){
			ctx.setReturnCode(ex.getCode());
			ctx.setReason(ex.getMessage());
			logger.error(ex.getCode() + ":" + ex.getMessage());
		}catch (Exception ex){
			ctx.setReturnCode("core.fatalerror");
			ctx.setReason(ex.getMessage());
			logger.error("core.fatalerror:" + ex.getMessage());
			ex.printStackTrace();
		}catch (Throwable t){
			ctx.setReturnCode("core.fatalerror");
			ctx.setReason(t.getMessage());
			logger.error("core.fatalerror:" + t.getMessage());			
		}
		finally {
			if (servant != null)
				servant.setState(Servant.STATE_IDLE);		
			if (ac != null){
				ac.accessEnd(sessionId,id, servant.getDescription(), ctx);
			}
			ctx.setEndTime(System.currentTimeMillis());
			if (pool != null){
				pool.visited(ctx.getEndTime() - ctx.getStartTime(),ctx.getReturnCode());
			}
		}
		return 0;
	}
}
