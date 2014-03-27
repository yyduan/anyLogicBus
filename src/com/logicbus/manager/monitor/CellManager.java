package com.logicbus.manager.monitor;

import com.anysoft.util.Manager;

public class CellManager extends Manager<Cell>{
	protected static CellManager instance = null;
	synchronized public static CellManager get(){
		if (instance == null){
			instance = new CellManager();
		}
		return instance;
	}
}
