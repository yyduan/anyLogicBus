package com.logicbus.backend.bizlog;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.BizLogItem;
import com.logicbus.backend.BizLogger;


/**
 * 分发BizLogger
 * 
 * <br>
 * 负责接收日志，并分发到多个BizLogger上并行处理．<br>
 * 
 * @author duanyy
 * @since 1.2.3
 * 
 */
public class DispatchBizLogger implements BizLogger {
	/**
	 * 工作线程数
	 */
	protected int threadCnt = 10;
	
	/**
	 * 工作线程组
	 */
	protected WorkerThread [] workers = null;
	
	public DispatchBizLogger(Properties props){
		threadCnt = PropertiesConstants.getInt(props, "bizlog.dispatch.threadCnt", threadCnt);
		threadCnt = threadCnt <= 0 ? 10 : threadCnt;
		
		workers = new WorkerThread[10];
		
		String loggerClass = PropertiesConstants.getString(props, 
				"bizlog.dispatch.logger", "com.logicbus.backend.bizlog.DefaultBizLogger");
		
		BizLogger.TheFactory factory = new BizLogger.TheFactory();
				
		for (int i = 0 ; i < workers.length ; i ++){
			BizLogger logger = null;
			try {
				logger = factory.newInstance(loggerClass,props);				
			}catch (Exception ex){
				ex.printStackTrace();
			}	
			workers[i] = new WorkerThread(logger);
		}
	}
	
	protected int hash(String sn){
		int hashcode = sn.hashCode();
		hashcode = hashcode <= 0 ? - hashcode : hashcode;
		
		return hashcode % threadCnt;
	}

	@Override
	public void close() {
		for (int i = 0 ; i < workers.length ; i ++){
			workers[i].close();
			workers[i] = null;
		}
	}
	
	@Override
	public void log(BizLogItem item) {		
		int index = hash(item.sn);		
		workers[index].newLog(item);
	}
	
	/**
	 * 工作线程
	 * 
	 * @author duanyy
	 * @since 1.2.3
	 * 
	 */
	public static class WorkerThread implements Runnable{
		protected ConcurrentLinkedQueue<BizLogItem> queue = new ConcurrentLinkedQueue<BizLogItem>();
		protected Thread thread = null;
		protected BizLogger logger = null;
		protected boolean stopped = false;
		
		public WorkerThread(BizLogger _logger){
			logger = _logger;
			thread = new Thread(this);
			thread.start();
		}
		
		public void newLog(BizLogItem item){
			queue.offer(item);
		}
		
		public void close(){
			stopped = true;
			if (logger != null){
				logger.close();
			}
			thread.interrupt();
		}
		
		@Override
		public void run() {
			while (!stopped){
				try {
					if (!queue.isEmpty()){
						BizLogItem item = queue.poll();						
						while (item != null){
							if (logger != null){
								logger.log(item);
							}
							item = queue.poll();
						}
					}
					Thread.sleep(2000);
				}catch (Exception ex){
					
				}
			}
		}
	}

}
