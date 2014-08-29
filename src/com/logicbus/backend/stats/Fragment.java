package com.logicbus.backend.stats;

/**
 * 统计片段
 * 
 * @author duanyy
 * 
 * @param <dimension>
 *            维度
 * @param <measure>
 *            量度
 */
public interface Fragment<dimension extends Comparable<dimension>, measure extends Measure<measure>> {
	/**
	 * 获取维度值
	 * 
	 * @return
	 */
	public dimension getDimension();

	/**
	 * 获取量度值
	 * 
	 * @return
	 */
	public measure getMeasure();
}
