package com.logicbus.models.servant;

import java.util.Map;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.DefaultProperties;
import com.anysoft.util.Factory;
import com.anysoft.util.JsonSerializer;
import com.anysoft.util.JsonTools;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlSerializer;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;

/**
 * 服务调用参数
 * @author duanyy
 *
 * @since 1.0.3
 */
public class Argument implements XmlSerializer,JsonSerializer{
	/**
	 * 参数ID
	 */
	protected String id;
	
	/**
	 * 获取Id
	 * @return
	 */
	public String getId(){return id;}
	
	/**
	 * 缺省值
	 */
	protected String defaultValue;
	
	/**
	 * 获取缺省值
	 * @return
	 */
	public String getDefaultValue(){return defaultValue;}
	
	/**
	 * 是否可选
	 */
	protected boolean isOption;
	
	/**
	 * 是否可选
	 * @return
	 */
	public boolean isOption(){return isOption;}
	
	/**
	 * 参数getter
	 */
	protected String getter ="Default";
	
	/**
	 * 获取getter
	 * @return
	 */
	public String getGetter(){return getter;}
	
	/**
	 * getter的参数
	 */
	protected String getterParameters;
	
	/**
	 * 获取gettter的参数
	 * @return
	 */
	public String getGetterParameters(){return getterParameters;}
	
	/**
	 * getter的参数列表
	 */
	protected Properties parameters = null;
	
	/**
	 * 获取getter的参数列表
	 * @return
	 */
	public Properties getParameter(){
		if (parameters != null){
			return parameters;
		}
		if (getterParameters == null || getterParameters.length() <= 0){
			return null;
		}
		parameters = new DefaultProperties();
		parameters.loadFromString(getterParameters);
		return parameters;
	}
	
	/**
	 * getter的实例
	 */
	protected Getter theGetter = null;
	
	/**
	 * 获取参数值
	 * @param msg 服务接口文档
	 * @param ctx 上下文
	 * @return 参数值
	 */
	public String getValue(MessageDoc msg,Context ctx)throws ServantException{
		if (theGetter == null){
			Settings settings = Settings.get();
			ClassLoader cl = (ClassLoader)settings.get("classLoader");
			TheFactory factory = new TheFactory(cl);
			theGetter = factory.newInstance(getter);
		}
		return theGetter.getValue(this, msg, ctx);
	}
	
	@Override
	public void toXML(Element e) {
		e.setAttribute("id", id);
		e.setAttribute("defaultValue", defaultValue);
		e.setAttribute("isOption", isOption?"true":"false");
		e.setAttribute("getter", getter);
		e.setAttribute("parameters", getterParameters);
	}

	@Override
	public void fromXML(Element e) {
		XmlElementProperties props = new XmlElementProperties(e,null);
		
		id = PropertiesConstants.getString(props, "id", "");
		defaultValue = PropertiesConstants.getString(props, "defaultValue", "");
		isOption = PropertiesConstants.getBoolean(props, "isOption", true);
		getter = PropertiesConstants.getString(props,"getter","Default");
		getterParameters = PropertiesConstants.getString(props,"paramters","");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void toJson(Map json) {
		JsonTools.setString(json, "id", id);
		JsonTools.setString(json, "defaultValue",defaultValue);
		JsonTools.setBoolean(json, "isOption",isOption);
		JsonTools.setString(json, "getter",getter);
		JsonTools.setString(json, "parameters",getterParameters);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map json) {
		id = JsonTools.getString(json, "id","");
		defaultValue = JsonTools.getString(json, "defaultValue","");
		isOption = JsonTools.getBoolean(json, "isOption",true);
		getter = JsonTools.getString(json, "getter","Default");
		getterParameters = JsonTools.getString(json, "parameters","");
	}

	/**
	 * Factory of getter
	 * @author duanyy
	 *
	 */
	public static class TheFactory extends Factory<Getter>{
		public TheFactory(ClassLoader cl){
			super(cl);
		}
		
		@Override
		public String getClassName(String _module) throws BaseException{
			if (_module.indexOf(".") < 0){
				return "com.logicbus.models.servant.getter." + _module;
			}
			return _module;
		}		
	}
}
