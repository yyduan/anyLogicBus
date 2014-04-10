package com.logicbus.models.servant.getter;

import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.servant.Argument;
import com.logicbus.models.servant.Getter;


/**
 * 缺省的参数Getter
 * 
 * @author duanyy
 *
 * @since 1.0.3
 */
public class Default implements Getter {

	@Override
	public String getValue(Argument argu, MessageDoc msg, Context ctx) throws ServantException {
		String id = argu.getId();
		String value;
		if (argu.isOption()){
			value = ctx.GetValue(id, argu.getDefaultValue());
		}else{
			value = ctx.GetValue(id, "");
			if (value == null || value.length() <= 0){
				throw new ServantException("client.args_not_found",
						"Can not find parameter:" + id);
			}
		}
		return value;
	}
}
