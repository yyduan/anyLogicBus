package com.logicbus.dbcp;

import com.anysoft.util.JsonSerializer;
import com.anysoft.util.XmlSerializer;

/**
 * 数据连接池的统计信息
 * 
 * @author duanyy
 * @since 1.2.5
 */
public interface ConnectionPoolStat extends XmlSerializer,JsonSerializer{
	public void visited(int creating,int working,int idle,int waitQueueLength,long duration,boolean isNull);
}
