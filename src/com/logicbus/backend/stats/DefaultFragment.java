package com.logicbus.backend.stats;

/**
 * Fragment的缺省实现
 * 
 * @author duanyy
 *
 * @param <dimension>
 * @param <measure>
 */
public class DefaultFragment<dimension extends Comparable<dimension>,measure extends Measure<measure>> 
implements Fragment<dimension, measure> {

	private dimension dim;
	private measure meas;

	public void setDimension(dimension _dim){
		dim = _dim;
	}
	
	public void setMeasure(measure _meas){
		meas = _meas;
	}
	
	@Override
	public dimension getDimension() {
		return dim;
	}

	@Override
	public measure getMeasure() {
		return meas;
	}

}
