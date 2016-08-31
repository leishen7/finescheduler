package com.nuexpert.schd.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class DBUtil {
	private static final Logger log = Logger.getLogger(DBUtil.class);

	public static Connection getConnection() throws Exception{

		Context initContext = new InitialContext();
		Context envContext  = (Context)initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource)envContext.lookup("jdbc/schd_db");

		if(ds==null)
			throw new SQLException("Can't get data source");

		//get database connection
		Connection conn = ds.getConnection();

		if(conn==null)
			throw new SQLException("Can't get database connection");

		conn.setAutoCommit(true);
		
		return conn;
	}
	
	public static Timestamp getDBTimeStamp(Date date){
		return null==date?null:new Timestamp(date.getTime());
	}
	
	public static java.sql.Date getDBDate(Date date){
		return null==date?null:new java.sql.Date(date.getTime());
	}
	
	public static Date getDate(java.sql.Date date){
		return null==date?null:new Date(date.getTime());
	}
	
	public static void beginTransaction(Connection conn) throws Exception{
		if (null!=conn){
			log.debug("autoCommit="+conn.getAutoCommit());
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
	}
	
	public static boolean endTransaction(Connection conn, boolean commit){
		if (null==conn){
			return false;
		}
		
		boolean success =false;
		
		if (commit){
			try{
				conn.commit();
				success=true;
			}catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
		if (!commit || !success){
			try {
				conn.rollback();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return success;
	}
}
