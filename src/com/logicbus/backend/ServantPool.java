package com.logicbus.backend;

import com.anysoft.util.BaseException;
import com.logicbus.models.servant.ServiceDescription;



/**
 * 服务资源池
 * 
 * @author duanyy
 * @version 1.2.2 [20140617 duanyy]
 * - 改进同步模型
 * 
 * @version 1.2.5 [20140722 duanyy]
 * - Servant的destroy方法变更为close
 * 
 * @version 1.2.6 [20140807 duanyy]
 * - 变更为interface
 */
public interface ServantPool extends AutoCloseable{
	public ServiceDescription getDescription();
	public ServantStat getStat();
	public void reload(ServiceDescription sd);
		
	public void pause();
	public void resume();
	public boolean isRunning();
			
	public void visited(long duration,String code);
	
	public Servant borrowObject(int priority) throws BaseException;
	public void returnObject(Servant obj);
}
