package com.logicbus.backend.acm;

import com.anysoft.cache.Cachable;
import com.anysoft.cache.ChangeAware;
import com.anysoft.cache.Provider;
import com.anysoft.util.Properties;

public class XMLResourceSimpleModelProvider<model extends Cachable> implements Provider<model> {

	public XMLResourceSimpleModelProvider(Properties props){
		
	}
	
	@Override
	public model load(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChangeListener(ChangeAware<model> listener) {
		// to do noting
	}

}
