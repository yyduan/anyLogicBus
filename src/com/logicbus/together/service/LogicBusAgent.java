package com.logicbus.together.service;

import java.io.InputStream;
import java.sql.Connection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.datasource.ConnectionPool;
import com.logicbus.datasource.ConnectionPoolFactory;
import com.logicbus.datasource.SQLTools;
import com.logicbus.models.servant.ServiceDescription;
import com.logicbus.together.Compiler;
import com.logicbus.together.Logiclet;


/**
 * together在logicbus中的代理
 * 
 * @author duanyy
 * @since 1.1.0
 * 
 */
public class LogicBusAgent extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage) msgDoc.asMessage(XMLMessage.class);
		
		String reload = getArgument("reload","false",msgDoc,ctx);
		
		if (reload.equals("true")){
			reloadProtocol();
		}
		
		if (dbSupport){
			ConnectionPool pool = ConnectionPoolFactory.getPool();
			Connection conn = pool.getConnection(dsName, 3000);
			
			if (conn == null) 
					throw new ServantException("core.sqlerror","Can not get a db connection : " + dsName);
			
			if (transactionSupport){
				conn.setAutoCommit(false);
			}else{
				conn.setAutoCommit(true);
			}
			
			ctx.setConnection(conn);
			
			try {
				Element root = msg.getRoot();
				if (logiclet != null){
					logiclet.execute(root, msg, ctx,null);
					if (logiclet.hasError()){
						throw new ServantException(logiclet.getCode(),logiclet.getReason());
					}
				}
				if (transactionSupport){
					conn.commit();
				}
			}catch (Exception ex){
				if (transactionSupport){
					conn.rollback();
				}
				throw ex;
			}finally{
				ctx.setConnection(null);
				SQLTools.close(conn);
			}
		}else{
			Element root = msg.getRoot();
			if (logiclet != null){
				logiclet.execute(root, msg, ctx,null);
				if (logiclet.hasError()){
					throw new ServantException(logiclet.getCode(),logiclet.getReason());
				}
			}
		}
		return 0;
	}

	@Override
	public void create(ServiceDescription sd) throws ServantException{
		super.create(sd);
		
		Properties props = sd.getProperties();		
		dbSupport = PropertiesConstants.getBoolean(props, "dbSupport", dbSupport);		
		dsName = PropertiesConstants.getString(props, "datasource", dsName);
		transactionSupport = PropertiesConstants.getBoolean(props, "transactionSupport", transactionSupport);	
		
		xrcMaster = PropertiesConstants.getString(props, "xrc.master", "${master.home}/servants/" + sd.getPath() + ".xrc");
		xrcSecondary = PropertiesConstants.getString(props, "xrc.secondary", "${secondary.home}/servants/" + sd.getPath() + ".xrc");
		
		reloadProtocol();
	}	
	
	protected void reloadProtocol(){
		Settings settings = Settings.get();
		ResourceFactory rm = (ResourceFactory) settings.get("ResourceFactory");
		if (null == rm){
			rm = new ResourceFactory();
		}
		
		Document doc = null;
		InputStream in = null;
		try {
			in = rm.load(xrcMaster, xrcSecondary);
			doc = XmlTools.loadFromInputStream(in);
			
			logiclet = Compiler.compile(doc.getDocumentElement(), Settings.get(),null);			
			if (logiclet == null){
				logger.error("Can not compile the document,xrc =" + xrcMaster);
			}
		} catch (Exception ex){
			logger.error("Error occurs when load xml file,source=" + xrcMaster , ex);
		}finally {
			IOTools.closeStream(in);
		}
	}
	
	/**
	 * 协议文档主URI
	 */
	protected String xrcMaster = "";
	
	/**
	 * 协议文档备URI
	 */
	protected String xrcSecondary = "";
	
	/**
	 * Logiclet根节点
	 */
	protected Logiclet logiclet = null;
	
	/**
	 * 是否需要数据库连接,缺省是false
	 */
	protected boolean dbSupport = false;
	
	/**
	 * 是否需要事务支持,缺省是false
	 */
	protected boolean transactionSupport = false;
	
	/**
	 * 数据库数据源名称,缺省为Default
	 */
	protected String dsName = "Default";
}
