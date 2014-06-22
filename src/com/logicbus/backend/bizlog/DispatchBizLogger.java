package com.logicbus.backend.bizlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.DefaultProperties;
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
public class DispatchBizLogger extends  AbstractBizLogger {
	/**
	 * 工作线程数
	 */
	protected int threadCnt = 10;
	
	/**
	 * 工作线程组
	 */
	protected WorkerThread [] workers = null;
	
	protected static Logger logger = LogManager.getLogger(DispatchBizLogger.class);
	
	public DispatchBizLogger(Properties props){
		super(props);
		threadCnt = PropertiesConstants.getInt(props, "bizlog.dispatch.threadCnt", threadCnt);
		threadCnt = threadCnt <= 0 ? 10 : threadCnt;
		
		workers = new WorkerThread[threadCnt];
		
		String loggerClass = PropertiesConstants.getString(props, 
				"bizlog.dispatch.logger", "com.logicbus.backend.bizlog.DefaultBizLogger");
		loggerClass = loggerClass.equals("com.logicbus.backend.bizlog.DispatchBizLogger") 
				? "com.logicbus.backend.bizlog.DefaultBizLogger" : loggerClass;
		
		BizLogger.TheFactory factory = new BizLogger.TheFactory();
		
		Properties child = new DefaultProperties("Default",props);
				
		for (int i = 0 ; i < workers.length ; i ++){
			BizLogger logger = null;
			try {
				child.SetValue("thead", String.valueOf(i));
				logger = factory.newInstance(loggerClass,child);				
			}catch (Exception ex){
				ex.printStackTrace();
			}	
			workers[i] = new WorkerThread(i,logger);
		}
		
		logger.info("DispatchBizLogger is Loaded..");
		logger.info("Count of Thread is " + threadCnt);
		logger.info("Child biz logger is " + loggerClass);
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
	public void onLog(BizLogItem item) {		
		int index = hash(item.sn);		
		workers[index].newLog(item);
	}

	@Override
	public void report(Element root) {
		if (reportSupport){
			super.report(root);
			
			Document doc = root.getOwnerDocument();
			
			for (int i = 0; i < workers.length ; i ++){
				Element thread = doc.createElement("logger");
				thread.setAttribute("id", "Thread" + String.valueOf(i));
				workers[i].report(thread);
				root.appendChild(thread);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (reportSupport){
			super.report(json);
			
			List<Object> threads = new ArrayList<Object>(threadCnt);
			
			for (int i = 0; i < workers.length ; i ++){
				Map<String,Object> thread = new HashMap<String,Object>(); 
				thread.put("id", "Thread" + i);
				workers[i].report(thread);
				threads.add(thread);
			}
			
			json.put("logger", threads);
		}
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
		protected int id = 0;
		
		public WorkerThread(int _id,BizLogger _logger){
			id = _id;
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
		
		public void report(Element root){
			root.setAttribute("queueLength",String.valueOf(queue.size()));
			if (logger != null){
				logger.report(root);
			}
		}
		
		public void report(Map<String,Object> json){
			json.put("queueLength", queue.size());
			if (logger != null){
				logger.report(json);
			}
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
