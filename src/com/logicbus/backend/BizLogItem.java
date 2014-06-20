package com.logicbus.backend;

/**
 * 日志项
 * 
 * @author duanyy
 * 
 * @since 1.2.3
 * 
 */
public class BizLogItem implements Comparable<BizLogItem> {
	
	/**
	 * 全局序列号
	 */
	public String sn;
	
	/**
	 * 服务ID
	 */
	public String id;
	
	/**
	 * 调用者
	 */
	public String client;
	
	/**
	 * 调用者IP
	 */
	public String clientIP;
	
	/**
	 * 服务主机
	 */
	public String host;
	
	/**
	 * 结果代码
	 */
	public String result;
	
	/**
	 * 结果原因
	 */
	public String reason;
	
	/**
	 * 开始时间
	 */
	public long startTime;
	
	/**
	 * 服务时长
	 */
	public long duration;
	
	/**
	 * 请求URL
	 */
	public String url;
	
	/**
	 * 服务文档内容
	 */
	public String content;
	
	@Override
	public int compareTo(BizLogItem other) {		
		return sn.compareTo(other.sn);
	}	
}
