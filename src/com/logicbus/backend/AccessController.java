package com.logicbus.backend;

import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Factory;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 访问控制器
 * 
 * <p>
 * 访问控制器用于控制客户端对服务器访问的权限，定义了anyLogicBus调度框架访问控制的基本行为。调度框架对AccessController的调用如下：<br>
 * 1. 在调用服务之前，调度框架会调用访问控制器的{@link #accessStart(String, ServiceDescription, Context)};<br>
 * 2. 在完成服务之后，会调用访问控制器的{@link #accessEnd(String, ServiceDescription, Context)}.<br>
 * 
 * <p>
 * 访问控制器在{@link #accessStart(String, ServiceDescription, Context)}中通过返回值和框架约定权限控制方式，如果返回值小于0，则表明
 * 本次无权访问；如果返回值大于1，则表明本次访问为高优先级访问；其他则表明本次访问为低优先级访问。
 * 
 * @author duanyy
 */
public interface AccessController {
	
	/**
	 * 开始访问
	 * 
	 * <p>
	 * 在调用服务之前调用
	 * 
	 * @param serviceId 本次访问的服务ID
	 * @param servant 本次访问服务的描述
	 * @param ctx 本次访问的上下文
	 * @return 访问优先级 <0表明无权访问;>1表明为高优先级访问;其他为低优先级访问
	 */
	public int accessStart(Path serviceId,ServiceDescription servant,Context ctx);
	
	/**
	 * 结束访问
	 * @param serviceId 本次访问的服务ID
	 * @param servant 本次访问服务的描述
	 * @param ctx 本次访问的上下文
	 * @return 无用处，仅仅追求对称美
	 */
	public int accessEnd(Path serviceId,ServiceDescription servant,Context ctx);
	
	/**
	 * 输出信息到XML
	 * @param root Element节点
	 */
	public void toXML(Element root);
	
	/**
	 * AccessController的工厂类
	 * @author duanyy
	 *
	 */
	public static class TheFactory extends Factory<AccessController>{
		public TheFactory(ClassLoader cl){
			super(cl);
		}
		/**
		 * 根据module映射类名
		 */
		public String getClassName(String _module) throws BaseException{
			if (_module.indexOf(".") < 0){
				return "com.logicbus.backend." + _module;
			}
			return _module;
		}		
	}
}
