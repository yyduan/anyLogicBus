package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.XmlTools;
import com.logicbus.selector.Selector;

public class DateFormatter extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
		pattern = PropertiesConstants.getString(_p,"pattern",pattern,true);
		
		Element _selector = XmlTools.getFirstElementByPath(_e, "selector");
		if (_selector == null){
			selector = Selector.newInstance(_e, _p, SingleField.class.getName());
		}else{
			selector = Selector.newInstance(_selector, _p);
		}
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		String value = selector.select(_dataProvider);
		long t = Long.parseLong(value);	
		return String.format(pattern, t);
	}

	protected Selector selector = null;
	
	protected String pattern = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS";
}
