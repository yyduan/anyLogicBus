package com.logicbus.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anysoft.util.CommandLine;
import com.anysoft.util.Settings;
import com.logicbus.backend.ServantException;

/**
 * 查询语句操作类
 * 
 * @author duanyy
 * @since 1.1.3
 */
public class Select extends DBOperation {

	public Select(Connection conn) {
		super(conn);
	}

	protected PreparedStatement stmt = null;
	protected ResultSet rs = null;
	
	/**
	 * 执行SQL语句
	 * @param sql SQL语句
	 * @param params 参数列表
	 * @return
	 * @throws SQLException
	 */
	public Select execute(String sql,Object... params) throws ServantException{
		close();
		
		try {
			stmt = conn.prepareStatement(sql);
			
			if (params != null){
				for (int i = 0 ; i < params.length ; i ++){
					stmt.setObject(i + 1, params[i]);
				}
			}
			
			rs = stmt.executeQuery();
			return this;
		}
		catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}
	}
	
	/**
	 * 获取查询结果（单返回值）
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Object single()throws ServantException{
		try {
			if (rs != null && rs.next()){
				return rs.getObject(1);
			}
			return null;
		}
		catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}		
	}

	/**
	 * 获取查询结果(单行返回值)
	 * 
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes"})
	public Map singleRow()throws ServantException{
		return singleRow(null);
	}	

	/**
	 * 获取查询结果(单行返回值)
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 * @since 1.2.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map singleRow(Map result)throws ServantException{
		try {
			if (rs != null && rs.next()){
				if (result == null)
				result = new HashMap();
				
				ResultSetMetaData metadata = rs.getMetaData();
				int columnCount = metadata.getColumnCount();
				for (int i = 0 ; i < columnCount ; i++){
					Object value = rs.getObject(i+1);
					if (value == null)continue;
					//1.2.0 支持列的别名
					String name = metadata.getColumnLabel(i+1);
					if (name == null){
						name = metadata.getColumnName(i+1);
					}
					result.put(name.toLowerCase(), value);
				}
				
				return result;
			}
			return null;
		}
		catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}
	}		
	
	/**
	 * 获取查询结果
	 * 
	 * <p>查询结果通过监听器获取
	 * 
	 * @param rowListener 行监听器
	 * @throws SQLException
	 */
	public void result(RowListener rowListener)throws ServantException{
		if (rs == null || rowListener == null){
			return ;
		}
		try{
			ResultSetMetaData metadata = rs.getMetaData();
			int columnCount = metadata.getColumnCount();
			while (rs.next()){
				Object cookies = rowListener.rowStart(columnCount);
				
				for (int i = 0 ; i < columnCount ; i++){
					//1.2.0 支持列的别名
					String name = metadata.getColumnLabel(i+1);
					if (name == null){
						name = metadata.getColumnName(i+1);
					}					
					rowListener.columnFound(
							cookies,
							i, 
							name.toLowerCase(), 
							rs.getObject(i+1)
							);
				}
				
				rowListener.rowEnd(cookies);
			}
		}
		catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}
	}

	/**
	 * 获取查询结果
	 * 
	 * <p>查询结果通过列表返回，可直接作为JSON数据
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public List result()throws ServantException{
		InnerRowListner data = new InnerRowListner();
		result(data);
		return data.getResult();
	}
	
	@Override
	public void close() throws ServantException {
		close(stmt,rs);
	}	

	/**
	 * 内置的行数据监听器
	 * 
	 * @author duanyy
	 *
	 */
	public static class InnerRowListner implements RowListener{
		@SuppressWarnings("rawtypes")
		protected ArrayList result = new ArrayList();
		@SuppressWarnings("rawtypes")
		public List getResult(){
			return result;
		}
		@SuppressWarnings("rawtypes")
		@Override
		public Object rowStart(int column) {
			return new HashMap(5);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void columnFound(Object cookies,int columnIndex, String name, Object value) {
			if (value != null){
				Map<String, Object> map = (Map)cookies;
				map.put(name, value);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void rowEnd(Object cookies) {
			result.add(cookies);
		}
		
	}
	
	public static void main(String [] args){
		CommandLine cmd = new CommandLine(args);		
		Settings settings = Settings.get();
		
		settings.SetValue("dbcp.master","java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		settings.SetValue("dbcp.secondary","java:///com/logicbus/datasource/dbcp.xml#com.logicbus.datasource.XmlResourceConnectionPool");
		
		settings.addSettings(cmd);
		
		ConnectionPool pool = ConnectionPoolFactory.getPool();
		
		Connection conn = null;
		Select select = null;
		try{
			conn = pool.getConnection("Default", 3000);
			select = new Select(conn);
			
			Object found = select.execute("select count(*) count from user").single();
			System.out.println(found);
		}catch (ServantException ex){
			ex.printStackTrace();
		}
		finally{
			SQLTools.close(select,conn);
		}
	}

}
