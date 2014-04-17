package com.logicbus.datasource;

import java.sql.Connection;

import java.util.LinkedList;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



/**
 * 数据源
 * 
 * <br>
 * 针对数据库的数据源
 * 
 * @author duanyy
 * @since 1.0.6
 */
public class NamedDataSource implements CloseAware {
	
	/**
	 * a logger of log4j
	 */
	protected static final Logger logger = LogManager.getLogger(NamedDataSource.class);
	
	/**
	 * 连接工厂
	 */
	protected ConnectionFactory factory = null;

	protected DataSourceStat stat = null;

	public DataSourceStat getStat(){return stat;}
	
	public ConnectionFactory getFactory(){return factory;}
	
	public NamedDataSource(ConnectionFactory _factory){
		factory = _factory;
		
		stat = new DataSourceStat();
		String monitor = factory.getMonitor();
		if (monitor != null && monitor.length() > 0){
			stat.setMonitor(monitor);
		}
		init();
	}
	
	public String getName(){return factory.getName();}

	protected void init() {
		logger.info("Init datasource:" + factory.getName());
		
		int maxIdle = factory.getMaxIdle();		
		idleList.clear();
		for (int i = 0 ; i < maxIdle ; i ++){
			Connection conn = newConnection();
			if (conn != null){
				idleList.add(conn);				
			}
		}
		
		active = idleList.size();
		
		logger.info("Count of active connections is " + active);
	}


	/**
	 * 当前空闲的连接列表
	 */
	protected LinkedList<Connection> idleList = new LinkedList<Connection>();
		
	/**
	 * 针对idleList的锁
	 */
	protected ReentrantLock lock = new ReentrantLock();

	/**
	 * 条件，空闲队列非空
	 */
	protected Condition notEmpty = lock.newCondition();
	
	/**
	 * 当前活动的连接
	 */
	protected int active = 0;
	
	
	/**
	 * 创建新的连接
	 * @return
	 */
	protected Connection newConnection(){
		Connection conn = factory.newConnection();		
		return conn == null ? null : new ManagedConnection(this,conn);
	}
	
	protected void visited(int waitQueueLength,long duration,boolean isNull){
		if (stat != null){
			stat.visited(waitQueueLength, duration, isNull);
		}
	}
	
	public Connection getConnection(long timeout){
		long _start = System.currentTimeMillis();			
		lock.lock();
		Connection conn = null;
		try {			
			int maxActive = factory.getMaxActive();
			
			long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);
			while (!(idleList.size() > 0 || active < maxActive)){
				if (nanos <= 0L)
					return null;
				nanos = notEmpty.awaitNanos(nanos);
			}

			if (idleList.size() <= 0){
				conn = newConnection();
				active ++;
			}else{
				conn = idleList.removeFirst();
				if (conn.isValid(0)){
					//测试Connection是否可用
					if (conn instanceof ManagedConnection){
						((ManagedConnection)conn).setIdle(false);
					}
				}else{
					conn = newConnection();
				}
			}
			
			return conn;
		}catch (Exception ex){
			logger.error("Can find a idle connection or there are too much connections.",ex);
			return null;
		}finally{
			visited(lock.getQueueLength(),System.currentTimeMillis() - _start,conn == null);
			lock.unlock();
		}
	}
	

	@Override
	public void connectionClosed(Connection conn) {
		lock.lock();
		try {
			if (conn.isValid(0)){
				idleList.add(conn);
			}else{
				active --;
			}
			notEmpty.signal();
		}catch (Exception ex){
			ex.printStackTrace();		
		}finally{
			lock.unlock();
		}
	}
}
