package com.logicbus.backend.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.logicbus.backend.ServantException;

/**
 * SQL工具类
 * @author hmyyduan
 *
 */
public class SQLUtil {
	public static PoolConn poolConn = null;
	
	/**
	 * 从数据库连接池获取指定数据源的数据库连接
	 * @param datasource 数据源Id
	 * @return 数据库连接
	 * @throws ServantException 当无法找到连接的时候抛出此异常
	 */
	public static Connection getConnection(String datasource) throws ServantException{
		Connection conn =  poolConn.getConnection(datasource);
		if (conn == null){
			throw new ServantException("core.sql_error","Can not get a db connection:" + datasource);
		}
		return conn;
	}
	
	/**
	 * 请求数据库连接池回收数据库连接
	 * @param conn 数据库连接
	 */
	public static void recycleConnection(Connection conn){
		poolConn.Recycle(conn);
	}
	
	/**
	 * 批量执行DML语句
	 * @param conn 数据库连接
	 * @param sqls SQL语句连接
	 * @return 每个SQL执行的状态
	 * @throws ServantException 当SQL语句执行错误时，抛出此异常
	 */
	public static int [] executeBatch(Connection conn,String [] sqls) throws ServantException{
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			
			for (int i = 0 ; i< sqls.length ; i ++){
				stmt.addBatch(sqls[i]);
			}
			
			return stmt.executeBatch();
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","SQL batch execute error:" + ex.getMessage());
		}finally {
			closeStatement(stmt);
		}
	}

	/**
	 * 执行单个SQL语句
	 * @param conn 数据库联接
	 * @param sql SQL语句
	 * @return SQL执行状态
	 * @throws ServantException
	 */
	public static int executeBatch(Connection conn,String sql) throws ServantException{
		Statement stmt = null;
		try {
			stmt = conn.createStatement();		
			return stmt.executeUpdate(sql);
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","SQL batch execute error:" + ex.getMessage());
		}finally {
			closeStatement(stmt);
		}
	}
	
	/**
	 * 创建Statement
	 * @param conn 数据库连接
	 * @return Statement
	 * @throws ServantException
	 */
	public static Statement createStatement(Connection conn) throws ServantException{
		try {
			return conn.createStatement();
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","Statement create error:" + ex.getMessage());
		}
	}
	public static PreparedStatement prepareStatement(Connection conn,String sql) throws ServantException{
		try {
			return conn.prepareStatement(sql);
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","Statement create error:" + ex.getMessage());
		}
	}	
	
	public static CallableStatement prepareCall(Connection conn,String sql) throws ServantException{
		try {
			return conn.prepareCall(sql);
		}catch (SQLException ex){
			throw new ServantException("core.sql_error","Statement create error:" + ex.getMessage());
		}
	}	
	/**
	 * 执行查询SQL语句并返回数据集
	 * @param stmt Statement
	 * @param sql SQL语句
	 * @return 数据集
	 * @throws ServantException
	 */
	public static ResultSet executeQuery(Statement stmt, String sql) throws ServantException {
		try {
			return stmt.executeQuery(sql);
		} catch (SQLException ex) {
			throw new ServantException("core.sql_error",
					"SQL execute error:" + ex.getMessage());
		}
	}
	
	/**
	 * 关闭Statement
	 * @param stmt Statement
	 */
	public static void closeStatement(Statement stmt){
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 关闭ResultSet
	 * @param rs ResultSet
	 */
	public static void closeResultSet(ResultSet rs){
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	public static void commit(Connection conn) throws ServantException{
		try {
			conn.commit();
		} catch (SQLException ex) {
			throw new ServantException("core.sql_error",ex.getMessage());
		}
	}
	public static void rollback(Connection conn) throws ServantException{
		try {
			conn.rollback();
		} catch (SQLException ex) {
			throw new ServantException("core.sql_error",ex.getMessage());
		}
	}	
	static {
		poolConn = PoolConn.getInstance();
	}
}
