package com.logicbus.manager.monitor;

public interface CopyHolder {
	public Copy getCopy(String id);
	public void addCopy(String id,Copy copy);
}
