package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkMySQL {
	private Connection con = null;  
	private PreparedStatement pstm =null; 							
	private ResultSet rs =null;
	private static final String url ="jdbc:mysql://localhost:3306/wechat?"
			+"characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
	public LinkMySQL(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con=DriverManager.getConnection(url,"root","123456");
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void Insert(String sql){			//插入、删除、更新数据
		try{    
			pstm=con.prepareStatement(sql);
			pstm.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ResultSet doSelect(String sql){	//查询数据
		try {
			pstm=con.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			rs=pstm.executeQuery();
			return rs;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
}
