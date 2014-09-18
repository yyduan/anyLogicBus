package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.anysoft.cache.Cacheable;
import com.anysoft.util.Confirmer;
import com.anysoft.util.JsonTools;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlElementProperties;

/**
 * 连接模型
 * @author duanyy
 * @since 1.2.5
 * 
 * @version 1.2.5.3 [20140731 duanyy]
 * -  基础包的Cacheable接口修改
 * 
 * @version 1.2.8 [20140912 duanyy]
 * - JsonSerializer中Map参数化
 */
public class ConnectionModel implements Cacheable{
	/**
	 * a logger of log4j
	 */
	protected static final Logger logger 
		= LogManager.getLogger(ConnectionModel.class);	
	/**
	 * 名称
	 */
	protected String name;
	
	/**
	 * 获取名称
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * jdbc驱动
	 */
	protected String driver;
	
	/**
	 * 获取JDBC驱动类
	 * @return
	 */
	public String getDriver(){
		return driver;
	}
	
	/**
	 * URL
	 */
	protected String url;
	
	/**
	 * 获取数据库连接所用的URI
	 * @return
	 */
	public String getURI(){
		return url;
	}
	
	/**
	 * username
	 */
	protected String username;
	
	/**
	 * 获取用户名
	 * @return
	 */
	public String getUserName(){
		return username;
	}
	
	/**
	 * password
	 */
	protected String password;
	
	/**
	 * 获取密码
	 * @return
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * 最大连接数
	 */
	protected int maxActive = 3;
	
	/**
	 * 获取maxActive
	 * @return
	 */
	public int getMaxActive(){return maxActive;}
	
	/**
	 * 空闲连接数
	 */
	protected int maxIdle = 1;
	
	/**
	 * 获取空闲连接数
	 * @return
	 */
	public int getMaxIdle(){return maxIdle;}
	
	/**
	 * 最大等待时间
	 */
	protected int maxWait = 5000;
	
	/**
	 * 获取最大等待时间
	 * @return
	 */
	public int getMaxWait(){return maxWait;}
	
	/**
	 * 监控指标
	 */
	protected String monitor;
	
	/**
	 * 获得监控指标
	 * @return
	 */
	public String getMonitor(){
		return monitor;
	}
	
	/**
	 * 数据确认ID
	 * @since 1.1.1
	 */
	protected String callbackId = "";
	
	/**
	 * 数据确认类的类名
	 * @since 1.1.1
	 */
	protected String callback = "";
		
	protected Confirmer confirmer = null;

	/**
	 * 获取当前的ClassLoader
	 * @return
	 */
	protected ClassLoader getClassLoader(){
		Settings settings = Settings.get();
		
		ClassLoader cl = (ClassLoader) settings.get("classLoader");
		if (cl == null){
			cl = Thread.currentThread().getContextClassLoader();
		}
		
		return cl;
	}
	
	public void report(Element e){
		e.setAttribute("name", name);
		e.setAttribute("driver", driver);
		e.setAttribute("url", url);
		e.setAttribute("username", username);
		e.setAttribute("password", "********");
		e.setAttribute("maxActive", String.valueOf(maxActive));
		e.setAttribute("maxIdle", String.valueOf(maxIdle));
		e.setAttribute("maxWait", String.valueOf(maxWait));
		e.setAttribute("monitor", monitor);	
		e.setAttribute("callbackId", callbackId);
		e.setAttribute("callback", callback);
	}
	
	public void report(Map<String,Object> json){
		JsonTools.setString(json, "name",name);
		JsonTools.setString(json, "driver",driver);
		JsonTools.setString(json, "url",url);
		JsonTools.setString(json, "username", username);
		JsonTools.setString(json, "password","********");
		JsonTools.setInt(json, "maxActive", maxActive);
		JsonTools.setInt(json, "maxIdle", maxIdle);
		JsonTools.setInt(json, "maxWait", maxWait);
		JsonTools.setString(json, "monitor", monitor);
		JsonTools.setString(json, "callbackId", callbackId);
		JsonTools.setString(json, "callback", callback);
	}	
	
	@Override
	public void toXML(Element e) {
		e.setAttribute("name", name);
		e.setAttribute("driver", driver);
		e.setAttribute("url", url);
		e.setAttribute("username", username);
		e.setAttribute("password", password);
		e.setAttribute("maxActive", String.valueOf(maxActive));
		e.setAttribute("maxIdle", String.valueOf(maxIdle));
		e.setAttribute("maxWait", String.valueOf(maxWait));
		e.setAttribute("monitor", monitor);
		e.setAttribute("callbackId", callbackId);
		e.setAttribute("callback", callback);
	}

	@Override
	public void fromXML(Element e) {
		XmlElementProperties props = new XmlElementProperties(e,null);
		
		name = PropertiesConstants.getString(props,"name", "");
		driver = PropertiesConstants.getString(props, "driver", "");
		url = PropertiesConstants.getString(props, "url", "");
		username = PropertiesConstants.getString(props, "username","");
		password = PropertiesConstants.getString(props, "password","");
		maxActive = PropertiesConstants.getInt(props, "maxActive",3);
		maxIdle = PropertiesConstants.getInt(props, "maxIdle",1);
		maxWait = PropertiesConstants.getInt(props, "maxWait",5000);
		monitor = PropertiesConstants.getString(props, "monitor", "");
		callbackId = PropertiesConstants.getString(props, "callbackId", "");
		callback = PropertiesConstants.getString(props, "callback", "");
	}

	@Override
	public void toJson(Map<String,Object> json) {
		JsonTools.setString(json, "name",name);
		JsonTools.setString(json, "driver",driver);
		JsonTools.setString(json, "url",url);
		JsonTools.setString(json, "username", username);
		JsonTools.setString(json, "password",password);
		JsonTools.setInt(json, "maxActive", maxActive);
		JsonTools.setInt(json, "maxIdle", maxIdle);
		JsonTools.setInt(json, "maxWait", maxWait);
		JsonTools.setString(json, "monitor", monitor);
		JsonTools.setString(json, "callbackId", callbackId);
		JsonTools.setString(json, "callback", callback);
	}

	@Override
	public void fromJson(Map<String,Object> json) {
		name = JsonTools.getString(json, "name", "");
		driver = JsonTools.getString(json, "driver", "");
		url = JsonTools.getString(json, "url", "");
		username = JsonTools.getString(json, "username", "");
		password = JsonTools.getString(json, "password", "");
		maxActive = JsonTools.getInt(json, "maxActive",3);
		maxIdle = JsonTools.getInt(json, "maxIdle",1);
		maxWait = JsonTools.getInt(json, "maxWait",5000);
		monitor = JsonTools.getString(json, "monitor", "");
		callbackId = JsonTools.getString(json, "callbackId", callbackId);
		callback = JsonTools.getString(json, "callback", callback);
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public boolean isExpired() {
		return false;
	}

	/**
	 * 按照当前的连接属性创建数据库连接
	 * @return
	 */
	public Connection newConnection(){
		Connection conn = null;
		try {
			ClassLoader cl = getClassLoader();
			
			if (confirmer == null){
				if (callbackId != null && callbackId.length() > 0 
						&& callback != null && callback.length() > 0){
					try {
						confirmer = (Confirmer)cl.loadClass(callback).newInstance();
						confirmer.prepare(callbackId);
					}catch (Exception ex){
						
					}
				}
			}			
			if (confirmer == null){
				Class.forName(driver, true, cl);
				conn = DriverManager.getConnection(url, username, password);
			}else{
				String _driver = confirmer.confirm("driver", driver);
				String _url = confirmer.confirm("url", url);
				String _username = confirmer.confirm("username", username);
				String _password = confirmer.confirm("password", password);
				
				Class.forName(_driver,true,cl);
				conn = DriverManager.getConnection(_url, _username,_password);
			}
		}catch (Exception ex){
			logger.error("Can not create a connection to " + url,ex);
		}		
		return conn;
	}	
}
