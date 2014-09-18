package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.selector.Selector;

/**
 * 常量选择器
 * 
 * @author duanyy
 * @since 1.2.8
 * 
 */
public class Constants extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
		value = PropertiesConstants.getString(_p, "selector-value", value,true);
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		return value;
	}

	protected String value = "0";
}
