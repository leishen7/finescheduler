package com.nuexpert.schd.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.vo.User;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class UserDAO implements Serializable{
	private final static String INSERT_SQL="insert into user(user_id, first_name, last_name,password,email,phone,phone_provider,country,province,city,address,postal_code,create_date) values(?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?,CURRENT_TIMESTAMP)";
	private final static String QUERY_SQL = "select * from user where user_id=?";
	private final static String PHONE_SQL = "select * from user where phone=?";

	private final static String LOAD_BY_NAME_SQL = "select * from user where first_name like ? and last_name like ?";
	
	private final static String INSERT_USER_ROLE ="insert into user_roles (user_id, role) values (?, ?)";
	private final static String PROVIDER_ROLE ="select count(*) from  user_roles where role='provider' and user_id=?";
	
	
	private final static String UPDATE_SQL="update user set first_name=?, last_name=?,password=?,email=?,phone=?,phone_provider=?,country=?,province=?,city=?,address=?,postal_code=? where user_id=?";

	private final static String QUERY_CITY = "select distinct city from user";
	
	public void addUser(User user) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(INSERT_SQL); 

		int index = 1;
		ps.setString(index++, user.getUserId());
		ps.setString(index++, user.getFirstname());
		ps.setString(index++, user.getLastname());
		ps.setString(index++, user.getPassword());
		if(user.getEmail()==null)
			ps.setString(index++,  user.getUserId());
		else
			ps.setString(index++, user.getEmail());

		ps.setString(index++, user.getPhone());
		ps.setString(index++, user.getPhoneProvider());
		ps.setString(index++, user.getCountry());
		ps.setString(index++, user.getProvince());
		ps.setString(index++, user.getCity());
		ps.setString(index++, user.getAddress());
		ps.setString(index++, user.getPostalCode());		
		ps.executeUpdate();
		
		ps.close();
		
		conn.close();
	}
	
	public void updateUser(User user) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(UPDATE_SQL); 

		int index = 1;

		ps.setString(index++, user.getFirstname());
		ps.setString(index++, user.getLastname());
		ps.setString(index++, user.getPassword());
		ps.setString(index++, user.getEmail());
		ps.setString(index++, user.getPhone());
		ps.setString(index++, user.getPhoneProvider());
		ps.setString(index++, user.getCountry());
		ps.setString(index++, user.getProvince());
		ps.setString(index++, user.getCity());
		ps.setString(index++, user.getAddress());
		ps.setString(index++, user.getPostalCode());	
		ps.setString(index++, user.getUserId());
		ps.executeUpdate();
		
		ps.close();
		
		conn.close();
	}
	
	public void addUserRole(String userId, String role) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps  =  conn.prepareStatement(INSERT_USER_ROLE); 
		
		int index = 1;
		ps.setString(index++,  userId);
		ps.setString(index++,  role);
		ps.executeUpdate();
		
		ps.close();
		conn.close();
	}
	
	public User getUser(String userId) throws Exception{
		
		if (StringUtils.isBlank(userId)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(QUERY_SQL); 

		ps.setString(1, userId);

		ResultSet rs = ps.executeQuery();
		User user=null;
		if(rs.next()){	
			user= new User();
			user.setUserId(rs.getString("user_id"));
			user.setPassword(rs.getString("password"));
			user.setFirstname(rs.getString("first_name"));
			user.setLastname(rs.getString("last_name"));
			user.setPhone(rs.getString("phone"));
			user.setPhoneProvider(rs.getString("phone_provider"));
			user.setEmail(rs.getString("email"));
			user.setCountry(rs.getString("country"));
			user.setProvince(rs.getString("province"));
			user.setCity(rs.getString("city"));
			user.setAddress(rs.getString("address"));
			user.setPostalCode(rs.getString("postal_Code"));
			if(user.getLastname()!=null)
				user.setUserName(user.getFirstname()+" "+user.getLastname());
			else
				user.setUserName(user.getFirstname());
			
		}
		
		rs.close();
		ps.close();
		conn.close();
		return user;		
	}
	
	
public User findUserByPhone(String phone) throws Exception{
		
		if (StringUtils.isBlank(phone)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(PHONE_SQL); 

		ps.setString(1, phone);

		ResultSet rs = ps.executeQuery();
		User user=null;
		if(rs.next()){	
			user= new User();
			user.setUserId(rs.getString("user_id"));
			user.setFirstname(rs.getString("first_name"));
			user.setLastname(rs.getString("last_name"));
			user.setPhone(rs.getString("phone"));
			user.setPhoneProvider(rs.getString("phone_provider"));
			user.setEmail(rs.getString("email"));
			user.setCountry(rs.getString("country"));
			user.setProvince(rs.getString("province"));
			user.setCity(rs.getString("city"));
			user.setAddress(rs.getString("address"));
			user.setPostalCode(rs.getString("postal_Code"));
		
			
		}
		
		rs.close();
		ps.close();
		conn.close();
		return user;		
	}
	
	public List<User> loadUsers(String firstName, String lastName) throws Exception{
		if (StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(LOAD_BY_NAME_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(firstName)+"%");
		ps.setString(index++, StringUtils.trimToEmpty(lastName)+"%");
		ResultSet rs = ps.executeQuery();
		List<User> resultList = new ArrayList<User>();
		
		while(rs.next()){
			User user= new User();
			user.setUserId(rs.getString("user_id"));
			user.setFirstname(rs.getString("first_name"));
			user.setLastname(rs.getString("last_name"));
			user.setPhone(rs.getString("phone"));
			user.setPhoneProvider(rs.getString("phone_provider"));
			user.setEmail(rs.getString("email"));
			user.setCountry(rs.getString("country"));
			user.setProvince(rs.getString("province"));
			user.setCity(rs.getString("city"));
			user.setAddress(rs.getString("address"));
			user.setPostalCode(rs.getString("postal_Code"));
			resultList.add(user);
		}
		
		rs.close();
		ps.close();
		conn.close();		
		
		return resultList;	
	}
	
public static boolean isProvider(String userId) throws Exception{
		
		if (StringUtils.isBlank(userId)){
			return false;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(PROVIDER_ROLE); 

		ps.setString(1, userId);

		ResultSet rs = ps.executeQuery();
		boolean result=false;
		if(rs.next()){			
			if(rs.getInt(1)>0)
				result= true;
		
			
		}
		
		rs.close();
		ps.close();
		conn.close();
		return result;		
	}

public List<String> getCities() throws Exception{
	
	
	Connection conn = DBUtil.getConnection();
	Statement s =  conn.createStatement();
	ResultSet rs = s.executeQuery(QUERY_CITY);
	List<String> resultList = new ArrayList<String>();
	while(rs.next()){
		resultList.add(rs.getString(1));
	}
	return resultList;

}
	
}
