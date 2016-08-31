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
import javax.faces.bean.ManagedProperty;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServicePromotion;
import com.nuexpert.schd.vo.User;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class ServiceDAO implements Serializable{
	private final static String INSERT_SQL="insert into service(service_id, service_name, provider,description,start_time,end_time,min_time,increment_time,max_time,buffer_between_bookings,category,language) values(?, ?, ?,?,?,?,?,?,?,?,?,?)";
	private final static String UPDATE_SQL="update service set service_name=?, description=?,start_time=?,end_time=?,min_time=?,increment_time=?,max_time=?,category=?,language=? where service_id=?";
	private final static String QUERY_SQL = "select * from service where provider=?";
	private final static String USER_SERVICE_SQL = "select * from service where service_id in (select distinct service_id from service_booking where user_id=?)";
	private final static String USER_RECENT_SERVICE_SQL = "select max(service_id) service_id from service where service_id in (select distinct service_id from service_booking where user_id=?)";
	private final static String PROVIDER_RECENT_SERVICE_SQL = "select max(service_id) service_id from service where provider=?";
	private final static String DELETE_SQL = "delete from service where service_id=?";
	private final static String GET_SERVICE_SQL = "select * from service where service_id=?";
	private final static String LOAD_SQL = "select service_id, service_name from service where provider=? and service_name like ?";
	private final static String SEARCH_SQL = "select * from service where provider!=? and service_name like ?";
	
	private final static String CATEGORY_SQL = "select * from service where category= ?";
	
	private final static String CITY_CATEGORY_SQL = "select * from service s INNER JOIN user u ON s.provider=u.user_id where u.city=? and s.category= ?";
	private final static String CITY_SQL = "select * from service s INNER JOIN user u ON s.provider=u.user_id where u.city=? ";
	private final static String SERVICENAME_SQL = "select * from service where category= ? and service_name=?";

	private final static String CITY_CATEGORIES_SQL = "select distinct category from service s INNER JOIN user u ON s.provider=u.user_id where u.city=?";
	private final static String CITYS_SQL = "select distinct u.city city from service s,user u where s.provider=u.user_id and s.language=?";
	
	
	private final static String NO_SERVICE_DAY_INSERT="insert into no_service_day(service_id, day_of_week) values(?, ?)";
	private final static String DELETE_NO_SERVICE_DAY = "delete from no_service_day where service_id=?";
	
	
	private final static String GET_NO_SERVICE_DAY = "select * from no_service_day where service_id=?";
	
	private final static String SERVICE_CAT_SQL = "select s.service_id, s.service_name,u.first_name,u.last_name ,s.description from service s,user u where s.provider=u.user_id and s.category=? order by s.update_time desc ";
	
	private final static String SERVICE_PROMOTION_SQL = "select * from service_promotion where service_id=? and effective_date>=CURRENT_DATE order by effective_date";
	private final static String INSERT_SERVICE_PROMOTION="insert into service_promotion(service_id, promotion,effective_date) values(?, ?, ?)";

	
	@ManagedProperty("#{userDAO}")
	private UserDAO userDAO;
	
	
	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public void addService(Service serv) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(INSERT_SQL); 
		int index = 1;
		ps.setString(index++, serv.getServiceId());
		ps.setString(index++, serv.getServiceName());
		ps.setString(index++, serv.getProvider());
		ps.setString(index++, serv.getDescription());
		ps.setString(index++, serv.getStartTime());
		ps.setString(index++, serv.getEndTime());
		ps.setInt(index++, serv.getMinTime());
		ps.setInt(index++, serv.getIncrementTime());
		ps.setInt(index++, serv.getMaxTime());
		ps.setInt(index++, serv.getBufferBetweenBookings());
		ps.setString(index++, serv.getCategory());
		ps.setString(index++,serv.getLanguage());
		
		ps.executeUpdate();
		
		ps=conn.prepareStatement(NO_SERVICE_DAY_INSERT);
		for(int i=0;i<serv.getNoServiceDay().length;i++){
			ps.setString(1, serv.getServiceId());
			ps.setInt(2, serv.getNoServiceDay()[i]);
			ps.executeUpdate();
			
		}
		
		ps.close();
		conn.close();
	}
	
	public void updateService(Service serv) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(UPDATE_SQL); 
		int index = 1;
		ps.setString(index++, serv.getServiceName());
		ps.setString(index++, serv.getDescription());
		ps.setString(index++, serv.getStartTime());
		ps.setString(index++, serv.getEndTime());
		ps.setInt(index++, serv.getMinTime());
		ps.setInt(index++, serv.getIncrementTime());
		ps.setInt(index++, serv.getMaxTime());
		ps.setString(index++, serv.getCategory());
		ps.setString(index++, serv.getLanguage());
		ps.setString(index++, serv.getServiceId());
		ps.executeUpdate();
		
		//Delete first
		ps =  conn.prepareStatement(DELETE_NO_SERVICE_DAY); 
		ps.setString(1, serv.getServiceId());
		ps.executeUpdate();	
		//Insert later
		ps=conn.prepareStatement(NO_SERVICE_DAY_INSERT);
		for(int i=0;i<serv.getNoServiceDay().length;i++){
			ps.setString(1, serv.getServiceId());
			ps.setInt(2, serv.getNoServiceDay()[i]);
			ps.executeUpdate();
			
		}
		
		ps.close();
		conn.close();
	}
	
	public void deleteService(String serviceId) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(DELETE_NO_SERVICE_DAY);
		ps.setString(1, serviceId);
		ps.executeUpdate();		
		
		ps =  conn.prepareStatement(DELETE_SQL); 
		ps.setString(1, serviceId);
		ps.executeUpdate();		

		ps.close();
		conn.close();

	}
	
	public Service getService(String serviceId) throws Exception{
		if(serviceId==null)
			return null;
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(GET_SERVICE_SQL); 

		ps.setString(1, serviceId);
		ResultSet rs = ps.executeQuery();
		Service serv = new Service();
		
		if(rs.next()){
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setProvider(rs.getString("provider"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setCategory(rs.getString("category"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
		}
		
		ps=conn.prepareStatement(GET_NO_SERVICE_DAY);
		ps.setString(1, serviceId);
		rs = ps.executeQuery();
		List<Integer> days= new ArrayList<Integer>();
		int dayOfWeek;
		while(rs.next()){
			dayOfWeek=rs.getInt("day_of_week");
			days.add(new Integer(dayOfWeek));
		}
		
		Integer[] noServiceDays=new Integer[days.size()];
		for(int i=0;i<days.size();i++){
			noServiceDays[i]=days.get(i);
		}
		serv.setNoServiceDay(noServiceDays);
		
		rs.close();
		ps.close();
		conn.close();
		
		return serv;
	}
	
	public List<Service> getServices(String provider) throws Exception{
		
		if (StringUtils.isBlank(provider)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(QUERY_SQL); 
		int index = 1;
		ps.setString(index++, provider);
		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;		
	}
	
public String getUserRecentServiceId(String userId) throws Exception{
		
		if (StringUtils.isBlank(userId)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(USER_RECENT_SERVICE_SQL); 
		int index = 1;
		ps.setString(index++, userId);
		
		ResultSet rs = ps.executeQuery();
		
		if(rs.next()){
			return rs.getString("service_id");
		}else{
			return null;
		}
}

public String getProviderRecentServiceId(String provider) throws Exception{
	
	if (StringUtils.isBlank(provider)){
		return null;
	}
	
	Connection conn = DBUtil.getConnection();
	
	PreparedStatement ps =  conn.prepareStatement(PROVIDER_RECENT_SERVICE_SQL); 
	int index = 1;
	ps.setString(index++, provider);
	
	ResultSet rs = ps.executeQuery();
	
	if(rs.next()){
		return rs.getString("service_id");
	}else{
		return null;
	}
}
	
	
public List<Service> getUserServices(String userId) throws Exception{
		
		if (StringUtils.isBlank(userId)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(USER_SERVICE_SQL); 
		int index = 1;
		ps.setString(index++, userId);
		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("provider"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;		
	}
	
	public List<Service> loadServices(String provider, String serviceName) throws Exception{
		if (StringUtils.isBlank(provider)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(LOAD_SQL); 
		int index = 1;
		ps.setString(index++, provider);
		ps.setString(index++, StringUtils.trimToEmpty(serviceName)+"%");
		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
 
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}
	
	public List<Service> searchServices(String userId, String serviceName) throws Exception{
		if (StringUtils.isBlank(userId)){
			return null;
		}		
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(SEARCH_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(userId));
		ps.setString(index++, StringUtils.trimToEmpty(serviceName)+"%");
		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("provider"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}	
	
	public List<String> getCityCategories(String city) throws Exception{
	
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(CITY_CATEGORIES_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(city));
		ResultSet rs = ps.executeQuery();
		List<String> resultList = new ArrayList<String>();
		while(rs.next()){
			resultList.add(rs.getString(1));
		}
		return resultList;

	}
	
	public List<String> getAllCategories(String language) throws Exception{
	
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement("select distinct category from service where language=?"); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(language));
		ResultSet rs = ps.executeQuery();
		List<String> resultList = new ArrayList<String>();
		while(rs.next()){
			resultList.add(rs.getString(1));
		}
		return resultList;

	}
	
	public List<String> getCities(String language) throws Exception{
	
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(CITYS_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(language));
		ResultSet rs = ps.executeQuery();
		List<String> resultList = new ArrayList<String>();
		while(rs.next()){
			resultList.add(rs.getString(1));
		}
		return resultList;

	}
	
	
	public List<Service> searchCategory(String category) throws Exception{
		if (StringUtils.isBlank(category)){
			return null;
		}		
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(CATEGORY_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(category));

		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("provider"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}	
	
	public List<Service> searchCityCategory(String city, String category) throws Exception{
		if (StringUtils.isBlank(city) && StringUtils.isBlank(category)){
			return null;
		}		
		PreparedStatement ps=null;
		Connection conn = DBUtil.getConnection();
		int index = 1;
		if(StringUtils.isNotBlank(city) && StringUtils.isNotBlank(category)){
			ps =  conn.prepareStatement(CITY_CATEGORY_SQL); 
			
			ps.setString(index++, StringUtils.trimToEmpty(city));
			ps.setString(index++, StringUtils.trimToEmpty(category));
		}else if(StringUtils.isNotBlank(city)){
			ps =  conn.prepareStatement(CITY_SQL); 
			
			ps.setString(index++, StringUtils.trimToEmpty(city));
		}else{
			ps =  conn.prepareStatement(CATEGORY_SQL);
			
			ps.setString(index++, StringUtils.trimToEmpty(category));
		}
		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("provider"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}	
	
	
	public List<Service> searchServicename(String category,String servicename) throws Exception{
		if (StringUtils.isBlank(category)){
			return null;
		}		
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(SERVICENAME_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(category));
		ps.setString(index++, StringUtils.trimToEmpty(servicename));

		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("provider"));
			serv.setDescription(rs.getString("description"));
			serv.setStartTime(rs.getString("start_time"));
			serv.setEndTime(rs.getString("end_time"));
			serv.setMinTime(rs.getInt("min_time"));
			serv.setIncrementTime(rs.getInt("increment_time"));
			serv.setMaxTime(rs.getInt("max_time"));
			serv.setBufferBetweenBookings(rs.getInt("buffer_between_bookings"));
			serv.setLanguage(rs.getString("language"));
			serv.setProviderUser(userDAO.getUser(serv.getProvider()));
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}	
	
	public List<Service> servicePromotion(String category) throws Exception{
		if (StringUtils.isBlank(category)){
			return null;
		}		
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(SERVICE_CAT_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(category));

		
		ResultSet rs = ps.executeQuery();
		
		List<Service> resultList = new ArrayList<Service>();
		
		while(rs.next()){
			Service serv = new Service();
			serv.setCategory(category);
			serv.setServiceId(rs.getString("service_id"));
			serv.setServiceName(rs.getString("service_name"));
			serv.setProvider(rs.getString("first_name")+" "+rs.getString("last_name"));
			serv.setDescription(rs.getString("description"));
	

 
			resultList.add(serv);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}	
	
	
	
	public List<ServicePromotion> getPromotion(String serviceId) throws Exception{
		if (StringUtils.isBlank(serviceId)){
			return null;
		}		
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(SERVICE_PROMOTION_SQL); 
		int index = 1;
		ps.setString(index++, StringUtils.trimToEmpty(serviceId));

		
		ResultSet rs = ps.executeQuery();
		
		List<ServicePromotion> resultList = new ArrayList<ServicePromotion>();
		
		while(rs.next()){
			ServicePromotion servProm = new ServicePromotion();
			servProm.setServiceId(rs.getString("service_id"));
			servProm.setPromotion(rs.getString("promotion"));
			servProm.setEffectiveDate(rs.getDate("effective_date"));

 
			resultList.add(servProm);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}
	
	public void addServicePromotion(ServicePromotion promotion) throws Exception{
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(INSERT_SERVICE_PROMOTION); 
		int index = 1;
		ps.setString(index++, promotion.getServiceId());
		ps.setString(index++, promotion.getPromotion());
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(promotion.getEffectiveDate()));
		
		ps.executeUpdate();		
		ps.close();
		conn.close();
	}
}
