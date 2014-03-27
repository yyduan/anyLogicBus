package com.logicbus.manager.monitor;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Factory;
import com.anysoft.util.Manager;
import com.anysoft.util.Settings;

public class AssetManager extends Manager<Asset>{
	protected static AssetManager instance = null;
	synchronized public static AssetManager get(){
		if (instance == null){
			instance = new AssetManager();
		}
		return instance;
	}
	
	public Asset create(Element e){
		TheFactory factory = new TheFactory();
		return factory.newInstance(e, Settings.get());
	}
	
	public static class TheFactory extends Factory<Asset>{
		public TheFactory() {
			super(AssetManager.class.getClassLoader());
		}

		public String getClassName(String _module) throws BaseException{
			return "com.logicbus.manager.monitor.Asset";
		}				
	}
}
