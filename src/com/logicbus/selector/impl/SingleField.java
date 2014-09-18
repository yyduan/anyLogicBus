package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.selector.Selector;


/**
 * 单字段选择器
 * 
 * @author duanyy
 * @since 1.2.8
 * 
 */
public class SingleField extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
		fieldName = PropertiesConstants.getString(_p, "selector-field", "",true);
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		if (context == null){
			context = _dataProvider.getContext(fieldName);
		}
		
		if (context != null){
			return _dataProvider.getValue(fieldName, context, getDefaultValue()).trim();
		}
		
		return getDefaultValue();
	}

	protected Object context = null;
	
	protected String fieldName;	
}
