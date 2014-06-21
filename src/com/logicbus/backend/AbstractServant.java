package com.logicbus.backend;

import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.servant.ServiceDescription;

/**
 * Servant虚基类
 * 
 * @author duanyy
 * @since 1.2.3
 */
abstract public class AbstractServant extends Servant {
	
	@Override
	public int actionProcess(MessageDoc msg, Context ctx) throws Exception {
		String json = getArgument("json",jsonDefault,msg,ctx);
		if (json != null && json.equals("true")){
			return onJson(msg,ctx);
		}else{
			return onXml(msg,ctx);
		}
	}

	protected String jsonDefault = "true";
	
	public void create(ServiceDescription sd) throws ServantException{
		super.create(sd);
		jsonDefault = sd.getProperties().GetValue("jsonDefault",jsonDefault);
		onCreate(sd);
	}
	
	public void destroy(){
		super.destroy();
		onDestroy();
	}
	
	abstract protected void onDestroy();

	abstract protected void onCreate(ServiceDescription sd) throws ServantException;

	abstract protected int onXml(MessageDoc msgDoc, Context ctx) throws Exception;

	abstract protected int onJson(MessageDoc msgDoc, Context ctx) throws Exception;
}
