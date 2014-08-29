package com.logicbus.backend.stats;

/**
 * 量度
 * 
 * @author duanyy
 *
 */
public interface Measure<measure> {
	public measure avg(measure other);
	public measure max(measure other);
	public measure min(measure other);
	public measure sum(measure other);
	public measure last(measure other);
}
