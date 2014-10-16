package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.logicbus.selector.Selector;

/**
 * 当前时间
 * 
 * @author duanyy
 * 
 * @since 1.2.8.2
 *
 */
public class Now extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		return String.valueOf(System.currentTimeMillis());
	}

}
