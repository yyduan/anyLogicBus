package com.logicbus.backend.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.anysoft.util.IOTools;
import com.anysoft.util.Settings;
import com.anysoft.webloader.ServletHandler;
import com.logicbus.backend.AccessController;
import com.logicbus.backend.Context;
import com.logicbus.backend.DefaultNormalizer;
import com.logicbus.backend.Normalizer;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.server.MessageRouter;
import com.logicbus.models.catalog.Path;

/**
 * 基于anyWebLoader的ServletHandler
 * 
 * @author duanyy
 *
 */
public class MessageRouterServletHandler implements ServletHandler {
	/**
	 * 访问控制器
	 */
	protected AccessController ac = null;
	
	/**
	 * 路径标准化
	 */
	protected Normalizer normalizer = null;
	
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LogManager.getLogger(MessageRouterServletHandler.class);
	
	/**
	 * 是否已经获取服务器信息
	 */
	protected static boolean getServerInfo = false;
	
	/**
	 * 编码
	 */
	protected static String encoding = "utf-8";
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		// TODO Auto-generated method stub
		Settings settings = Settings.get();
		encoding = settings.GetValue("http.encoding", encoding);
		ac = (AccessController) settings.get("accessController");
		
		normalizer = (Normalizer) settings.get("normalizer");
		if (normalizer == null){
			normalizer = new DefaultNormalizer();
		}
	}

	@Override
	public void doService(HttpServletRequest request,
			HttpServletResponse response, String method)
			throws ServletException, IOException {
		if (!getServerInfo){
			Settings settings = Settings.get();
			settings.SetValue("server.host", request.getLocalAddr());
			settings.SetValue("server.port", String.valueOf(request.getLocalPort()));
			logger.info("Get server info:" + settings.GetValue("server.host", "") + ":" + settings.GetValue("server.port",""));
			getServerInfo = true;
		}
		
		response.setHeader("Expires", "Mon, 26 Jul 1970 05:00:00 GMT");
		response.setHeader("Last-Modified", "Mon, 26 Jul 1970 05:00:00 GMT");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		
		StringBuffer doc = new StringBuffer();
		MessageDoc msgDoc = null;
		Context ctx = null;
		try{
			//从request输入流中读入XML文档
			
			if (method.equals("post"))
			{
				try{
					String _contentType = request.getContentType();
					if (_contentType != null && _contentType.length() > 0){
						doc = loadFromInputStream(doc,request.getInputStream());
					}
				}catch (Exception ex){
					// 没有输入XML文档
				}
			}

			msgDoc = new MessageDoc(doc,encoding);
			ctx = new HttpContext(request);	
			ctx.setClientIp(request.getRemoteHost());
			
			//规范化ID
			Path id = normalizer.normalize(ctx, request);
			
			{
				if (logger.getEffectiveLevel().equals(Level.DEBUG)){
					logger.debug("Invoking service:" + id);
					logger.debug("Input:");					
					logger.debug(msgDoc.toString());
				}
			}
			
			MessageRouter.action(id,msgDoc,ctx,ac);
			
			{
				if (logger.getEffectiveLevel().equals(Level.DEBUG)){
					logger.debug("Output:");
					logger.debug(msgDoc.toString());
				}
			}
			
			msgDoc.setReturn(ctx.getReturnCode(), ctx.getReason(), ctx.getEndTime() - ctx.getStartTime());
			response.setContentType(msgDoc.getContentType());
			msgDoc.output(response.getOutputStream(),response);
		}catch (Exception ex){
			if (msgDoc != null){
				response.setContentType(msgDoc.getContentType());
				msgDoc.setReturn(ctx.getReturnCode(), ctx.getReason(), ctx.getEndTime() - ctx.getStartTime());
				msgDoc.output(response.getOutputStream(),response);
			}
		}	
		finally {

		}
	}
	
	/**
	 * 从InputStream中装入文本
	 * @param buf 文本对象
	 * @param in InputStream
	 * @return
	 */
	private StringBuffer loadFromInputStream(StringBuffer buf,InputStream in){
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
            while ((line = reader.readLine()) != null) {
            	buf.append(line);
            	buf.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	IOTools.closeStream(in,reader);
        }
		return buf;
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
