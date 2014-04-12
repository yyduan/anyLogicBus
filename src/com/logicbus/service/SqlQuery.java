package com.logicbus.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.XmlTools;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.datasource.PoolConn;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.servant.ServiceDescription;

public class SqlQuery extends Servant {
	
	protected int m_max_count;
	
	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception{
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);	
		Element root = msg.getRoot();
		Element query = XmlTools.getFirstElementByPath(root, "query");
		if (query == null )	{
			throw new ServantException("client.args_not_found","Can not find xml node:query");
		}		
		String sql = query.getAttribute("Sql");
		if (sql.length() <= 0){
			throw new ServantException("client.args_not_found","Can not find xml node:query/@Sql");
		}
		
		String datasource = query.getAttribute("DataSource");
		if (datasource.length() <= 0){
			datasource = "logicbus";
		}
		
		int max_count = 0;
		{
			String value = query.getAttribute("MaxCount");
			
			if (value.length() > 0){
				try{
				max_count = Integer.parseInt(value);
				}catch (Exception ex){
					max_count = 0;
				}
			}
			if (max_count <= 0)
			{
				max_count  = m_max_count;
			}
		}
		
		Connection conn = PoolConn.getInstance().getConnection(datasource);
		if (conn == null){
			throw new ServantException("core.sql_error","Can not get a db connection:" + datasource);
		}
		Statement stmt = null;
		
		try
		{
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			logger.debug("SQL:" + sql);
			Document doc = root.getOwnerDocument();
			Element dataset = doc.createElement("dataset");
			dataset.setAttribute("Sql", sql);
			
			int column_count = 0;
			{
				//准备元数据
				Element meta = doc.createElement("meta");
				ResultSetMetaData metadata = rs.getMetaData();
				column_count = metadata.getColumnCount();
				
				for (int i = 1 ; i < column_count  + 1; i ++){
					Element column = doc.createElement("col");
					
					column.setAttribute("Id", String.valueOf(i));
					column.setAttribute("Name", metadata.getColumnName(i));
					column.setAttribute("DataType", metadata.getColumnTypeName(i));
					column.setAttribute("Size", String.valueOf(metadata.getColumnDisplaySize(i)));
					
					meta.appendChild(column);
				}
				dataset.appendChild(meta);
			}
			
			Element datarows = doc.createElement("rows");
			
			int count = 0;
			while (rs.next()){
				count ++;
				if (count > max_count)
					break;
				Element datarow = doc.createElement("row");
				for (int i = 1 ; i < column_count + 1; i ++)
				{
					Element cell = doc.createElement("c");
					cell.setAttribute("Id",String.valueOf(i));
					Object obj = rs.getObject(i);
					if (obj != null)
					{
						cell.appendChild(doc.createTextNode(obj.toString()));
					}
					datarow.appendChild(cell);
				}
				datarows.appendChild(datarow);
			}
			
			dataset.appendChild(datarows);
			root.appendChild(dataset);
		}catch (Exception ex){
			logger.error(ex);
			throw new ServantException("core.sql_error","在执行SQL语句过程中发生错误:" + ex.getMessage());
		}
		finally{
			if (stmt != null){
				try{
					stmt.close();
				}catch (SQLException ex){
					throw new ServantException("core.sql_error","在执行SQL语句过程中发生错误:" + ex.getMessage());					
				}
			}
			PoolConn.getInstance().Recycle(conn);
		}
		return 0;
	}
	
	@Override
	public void create(ServiceDescription sd) throws ServantException{
		super.create(sd);		
		String value = sd.getProperties().GetValue("sql.max_count", "100");
		m_max_count = Integer.parseInt(value);
	}
	
}
