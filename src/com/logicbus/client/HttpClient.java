package com.logicbus.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.anysoft.util.CommandLine;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;

/**
 * 基于Http请求的Client
 * 
 * @author duanyy
 * @since 1.0.4
 * 
 * @version 1.0.7 [20140418 duanyy]
 * - 优化Http头的读写机制
 */
public class HttpClient extends Client {
	/**
	 * cookies
	 * 
	 * <br>
	 * 保存服务器返回的cookies
	 */
	protected String cookies;
	
	/**
	 * 远程服务的根节点
	 * 
	 * <br>
	 * 一般为http://<ip>:<port>/<context>/services
	 * 
	 */
	protected String home;
	
	/**
	 * 缺省的encoding
	 * @since 1.0.7
	 */
	protected String defaultEncoding = "utf-8";
	
	/**
	 * 缺省的content-type
	 * @since 1.0.7
	 */
	protected String defaultContentType = "text/plain;charset=utf-8";
	
	public HttpClient(Properties props){	
		home = props.GetValue("client.remote.home", "");
		defaultEncoding = props.GetValue("http.encoding", defaultEncoding);
		defaultContentType = props.GetValue("http.contentType", defaultContentType);
	}
	
	@Override
	public Response invoke(String id, Request para, Response result) throws ClientException{
		if (home == null){
			throw new ClientException("client.no_remote_home",
					"Can not find the remote home,check parameter : client.remote.home");
		}
		String url = home + "/" + id;
		try {
			return invoke(new URL(url),para,result);
		} catch (MalformedURLException e) {
			throw new ClientException("client.error_url","URL error :" + url);
		}
	}
	
	/**
	 * 按照URL调用远程服务
	 * @param url URL
	 * @param para request
	 * @return response
	 */
	private Response invoke(URL url, Request para, Response result)throws ClientException {
		try {
			String method = para == null ? "GET":"POST";
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			if (cookies != null && cookies.length() > 0){
				conn.addRequestProperty("Cookie", cookies);
			}
			if (para != null)
				output(conn,para);
			
			int ret = conn.getResponseCode();
			if (ret != HttpURLConnection.HTTP_OK) {
				throw new ClientException("client.invoke_error", 
						"Error occurs when invoking service :"
						+ conn.getResponseMessage());
			}
			
			String newCookies = conn.getHeaderField("Set-Cookie");
			if (newCookies != null){
				cookies = newCookies;
			}
			return input(conn,result);
		}catch (IOException ex){
			throw new ClientException("client.io_error","Can not connect to remote host : " + url);
		}
	}

	/**
	 * 读入调用结果
	 * @param conn Http连接
	 * @param result 调用结果
	 * @return
	 * @throws ClientException
	 */
	private Response input(HttpURLConnection conn,Response result) throws ClientException{
		InputStream in = null;
		BufferedReader reader = null;
		
		try {
			String contentType = conn.getHeaderField("Content-Type");
			String encoding = conn.getHeaderField("Content-Encoding");
			encoding = encoding == null ? defaultEncoding : encoding;
			contentType = contentType == null ? defaultContentType : contentType;			
			result.setResponseAttribute("Content-Type",contentType);
			result.setResponseAttribute("Content-Encoding",encoding);
			
			String [] names = result.getResponseAttributeNames();
			if (names != null){
				for (String name:names){
					String value = conn.getHeaderField(name);
					if (value != null && value.length() > 0){
						result.setResponseAttribute(name, value);
					}
				}
			}
			
			in = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in,encoding));
			StringBuffer content = result.getBuffer();
			content.setLength(0);
			String line = null;
			while ((line = reader.readLine()) != null){
				content.append(line);
				content.append('\n');
			}
			
			return result;
		}catch (IOException ex){
			throw new ClientException("client.io_error","Can not read data from network.");
		}
		finally {
			IOTools.closeStream(in);
		}
	}

	/**
	 * 写出参数
	 * @param conn Http连接
	 * @param para 参数
	 * @throws ClientException
	 */
	private void output(HttpURLConnection conn, Request para) throws ClientException {
		OutputStream out = null;
		try {
			
			String encoding = para.getRequestAttribute("Content-Encoding", defaultEncoding);
			String contentType = para.getRequestAttribute("Content-Type",defaultContentType);
			
			conn.addRequestProperty("Content-Type", contentType);
			conn.addRequestProperty("Content-Encoding", encoding);
			
			String [] names = para.getRequestAttributeNames();
			if (names != null){
				for (String name:names){
					String value = para.getRequestAttribute(name, "");
					if (value != null && value.length() > 0){
						conn.addRequestProperty(name, value);
					}
				}
			}
			
			out = conn.getOutputStream();
			StringBuffer content = para.getBuffer();
			
			if (content != null && content.length() > 0){
				String data = content.toString();
				out.write(data.getBytes(encoding));
			}			

		}catch (IOException ex){
			throw new ClientException("client.io_error","Can not write data to network.");
		}finally {
			IOTools.closeStream(out);
		}
	}
	
	public static void main(String [] args){
		CommandLine cmd = new CommandLine(args);
		Settings settings = Settings.get();
		settings.SetValue("client.remote.home", "http://localhost/services");
		settings.addSettings(cmd);
		
		HttpClient client = new HttpClient(settings);
		
		try {
			Response response = client.invoke("/core/AclQuery?wsdl");
			
			System.out.println(response.getBuffer().toString());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}


}
