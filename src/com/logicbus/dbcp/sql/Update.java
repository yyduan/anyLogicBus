package com.logicbus.dbcp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.logicbus.backend.ServantException;

/**
 * Update操作
 * 
 * @author duanyy
 * @since 1.2.5
 * 
 */
public class Update extends DBOperation {

	public Update(Connection _conn) {
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
}
