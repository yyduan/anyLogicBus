package com.logicbus.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.anysoft.util.CommandLine;
import com.anysoft.util.Settings;
import com.logicbus.backend.ServantException;

/**
 * Update操作
 * @author duanyy
 * @since 1.1.3
 */
public class Update extends DBOperation {

	protected Update(Connection _conn) {
		super(_conn);
	}

	@Override
	public void close() throws Exception {
		
	}

	/**
	 * 执行单个SQL语句
	 * @param sql 
	 * @param params 参数列表
	 * @return
	 * @throws SQLException
	 */
	public int execute(String sql,Object...params) throws ServantException{
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			
			if (params != null){
				for (int i = 0 ; i < params.length ; i ++){
					System.out.println();
					stmt.setObject(i + 1, params[i]);
				}
			}
			
			return stmt.executeUpdate();
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}
		finally{
			close(stmt);
		}
	}
	
	/**
	 * 执行多个SQL语句
	 * @param sqls
	 * @return
	 * @throws SQLException
	 */
	public int[] executeBatch(String...sqls) throws ServantException{
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			
			for (String sql:sqls){
				stmt.addBatch(sql);
			}			
			return stmt.executeBatch();
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","Error occurs when executing sql:" + ex.getMessage());
		}
		finally{
			close(stmt);
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
		Update update = null;
		try{
			conn = pool.getConnection("Default", 3000);
			update = new Update(conn);
			update.execute("delete from user where user_id = ?", "duanyy");
			update.execute("insert into user(user_id,name,type,salt,password,state) values(?,?,?,?,?,?)",
					"duanyy","duanyy","user","","","U0A");
			update.execute("update user set state = ? where user_id = ?","U0B","duanyy");
			
			SQLTools.commit(conn);
		}catch (ServantException ex){
			ex.printStackTrace();
			try {
				SQLTools.rollback(conn);
			}catch (ServantException e){
				e.printStackTrace();
			}
		}
		finally{
			SQLTools.close(update,conn);
		}		
	}
}
