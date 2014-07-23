package com.logicbus.dbcp.impl;

import com.anysoft.cache.Provider;

import com.anysoft.util.XMLConfigurable;

/**
 * 基于DataBase Connection Model,DBCM的Provider
 * 
 * @author Administrator
 * 
 */
abstract public interface DBCMProvider extends Provider<ConnectionModel>,
		XMLConfigurable {
}
