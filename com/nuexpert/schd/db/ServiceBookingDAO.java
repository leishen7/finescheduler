package com.nuexpert.schd.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;

import com.nuexpert.schd.vo.ServiceBooking;
import com.nuexpert.schd.vo.User;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class ServiceBookingDAO implements Serializable{
	private static final Logger log = Logger.getLogger(ServiceBookingDAO.class);
	
	private final static String QUERY_SQL="select s.service_name,u.user_id providerId,CONCAT(u.first_name,' ',u.last_name) provider, b.service_booking_id,b.start_time, b.end_time,b.notes,b.status from service_booking b ,service s,user u where b.service_id=s.service_id and s.provider=u.user_id and b.user_id=?"; 
	private final static String SERVICE_SQL="select b.service_booking_id,b.service_id, b.user_id, b.status, s.service_name, b.start_time, b.end_time,b.notes,b.status,b.user_name,b.phone,b.email from service_booking b join service s on b.service_id=s.service_id where s.service_id=? ";

	private final static String INSERT_SQL="insert into service_booking(service_id, user_id, start_time, end_time, status,user_name,phone,email,notes,booked_date) values (?, ?, ?, ?,?,?,?,?,?, CURRENT_DATE)";
	private final static String UPDATE_SQL="update service_booking set status='requested',start_time=?, end_time=?, booked_date=CURRENT_DATE where service_booking_id=?";
	private final static String DELETE_SQL="delete from service_booking where service_booking_id=?";
	private final static String BOOKING_BY_PROVIDER_SQL="select b.service_id,b.user_id,s.service_name,u.user_id providerId,CONCAT(u.first_name,' ',u.last_name) provider, b.service_booking_id,b.start_time, b.end_time,b.notes,b.status,b.user_name,b.phone,b.email from service_booking b ,service s,user u where b.service_id=s.service_id and ((s.provider=u.user_id and s.provider=?) or (b.user_id=u.user_id and b.user_id=?)) and (b.status is NULL or b.status<>'Rejected')";
	private final static String QUERY_MAX_ID_SQL="select max(service_booking_id) as last_id from service_booking";
	private final static String UPDATE_STATUS_SQL="update service_booking set status=? where service_booking_id=?";
	
	private final static String GET_SQL="select s.service_name,u.user_id providerId,CONCAT(u.first_name,' ',u.last_name) provider, b.service_booking_id,b.service_id,b.user_id,b.start_time, b.end_time,b.notes,b.status,b.email,b.phone from service_booking b ,service s,user u where b.service_booking_id=? and b.service_id=s.service_id and s.provider=u.user_id"; 

	private final static String USERNAME_SQL="select distinct b.user_name from service_booking b ,service s where b.service_id=s.service_id and  s.provider=? and b.user_name is not null";

	private final static String CHOOSE_USERNAME_SQL="select user_id,email,phone from service_booking where user_name=?";
	
	//for shared services provided by the same provider
	//private final static String OVERLAP_SQL="select count(*) from service_booking b join service s on b.service_id=s.service_id where s.provider=(select provider from service where service_id=?) and ((?<b.start_time and ?>b.start_time) or (?>=b.start_time and ?<b.end_time))"; 
	private final static String OVERLAP_SQL="select count(*) from service_booking b join service s on b.service_id=s.service_id where b.service_id=? and ((?<b.start_time and ?>b.start_time) or (?>=b.start_time and ?<b.end_time))"; 

	private final static String NOTIFICATION_SQL="select * from service_booking where status='confirmed' and start_time>? and start_time<?"; 

	
	public void addServiceBooking(ServiceBooking booking){
		if (null==booking){
			log.error("booking is null");
			
			return;
		}
		
		boolean needCommit = false;
		Integer lastId = null;
		Connection conn = null;
		
		try{
			conn = DBUtil.getConnection();
			
			DBUtil.beginTransaction(conn);
			
			PreparedStatement ps =  conn.prepareStatement(INSERT_SQL); 
			
			int index = 1;
			ps.setString(index++, booking.getServiceId());
			ps.setString(index++, booking.getUserId());
			ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getStartTime()));
			ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getEndTime()));
			ps.setString(index++, booking.getStatus());
			if(booking.getUserName()==null && booking.getUserId()!=null){
					UserDAO userDAO= new UserDAO();
					User user=userDAO.getUser(booking.getUserId());
					booking.setUserName(user.getFirstname()+" "+user.getLastname());
					booking.setPhone(user.getPhone());
					booking.setEmail(user.getEmail());
				
			}
			ps.setString(index++, booking.getUserName());
			ps.setString(index++, booking.getPhone());
			ps.setString(index++, booking.getEmail());
			ps.setString(index++, booking.getNotes());
			ps.executeUpdate();
			needCommit = true;
			DBUtil.endTransaction(conn, needCommit);
			lastId = getLastServiceBookingId();
			booking.setServiceBookingId(lastId);
			
			ps.close();
			
			
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}finally{
			
			try{
				
				conn.close();
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
	}
	
	public Integer getLastServiceBookingId() throws Exception{
		Integer id= null;
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(QUERY_MAX_ID_SQL); 
		
		ResultSet rs =  ps.executeQuery();

		if(rs.next()){
			id=rs.getInt("last_id");
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return id;
	}
	
	public void updateServiceBooking(ServiceBooking booking) throws Exception{
		if (null==booking){
			log.error("booking is null");
			
			return;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(UPDATE_SQL); 
		
		int index = 1;
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getStartTime()));
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getEndTime()));
		ps.setInt(index++, booking.getServiceBookingId());
		
		ps.executeUpdate();		
		
		ps.close();
		conn.close();
	}
	
	public void deleteServiceBooking(Integer serviceBookingId) throws Exception{
		if (null==serviceBookingId){
			log.error("serviceBookingId is null");
			return;
		}
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(DELETE_SQL); 
		
		int index = 1;
		ps.setInt(index++, serviceBookingId);
		
		ps.executeUpdate();		
		
		ps.close();
		conn.close();
	}
	

	
	public void updateStatus(String status,Integer serviceBookingId) throws Exception{
		if (null==serviceBookingId){
			log.error("serviceBookingId is null");
			return;
		}
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(UPDATE_STATUS_SQL); 
		
		int index = 1;
		ps.setString(index++, status);
		ps.setInt(index++, serviceBookingId);
		
		ps.executeUpdate();		
		
		ps.close();
		conn.close();
	}		
	
	public List<ServiceBooking> getServiceBookings(String userId) throws Exception{
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(QUERY_SQL); 
		
		ps.setString(1, userId);
		
		ResultSet rs =  ps.executeQuery();
		List<ServiceBooking> resultList = new ArrayList<ServiceBooking>();
		
		while(rs.next()){
			ServiceBooking servBooking = new ServiceBooking();
			
			servBooking.setUserId(userId);
			servBooking.setServiceName(rs.getString("service_name"));
			servBooking.setProviderId(rs.getString("providerId"));
			servBooking.setProvider(rs.getString("provider"));
			servBooking.setServiceBookingId(rs.getInt("service_Booking_Id"));
			servBooking.setStartTime(rs.getTimestamp("start_time"));
			servBooking.setEndTime(rs.getTimestamp("end_time"));
			servBooking.setNotes(rs.getString("notes"));
			servBooking.setStatus(rs.getString("status"));
 
			resultList.add(servBooking);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}
	
	
public List<ServiceBooking> getServiceBookingsById(String serviceId) throws Exception{
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(SERVICE_SQL); 
		
		ps.setString(1, serviceId);
		
		ResultSet rs =  ps.executeQuery();
		List<ServiceBooking> resultList = new ArrayList<ServiceBooking>();
		
		while(rs.next()){
			ServiceBooking servBooking = new ServiceBooking();
			
			servBooking.setServiceBookingId(rs.getInt("service_booking_id"));
			servBooking.setServiceId(rs.getString("service_id"));
			servBooking.setUserId(rs.getString("user_id"));
			servBooking.setServiceName(rs.getString("service_name"));
			servBooking.setStartTime(rs.getTimestamp("start_time"));
			servBooking.setEndTime(rs.getTimestamp("end_time"));
			servBooking.setNotes(rs.getString("notes"));
			servBooking.setStatus(rs.getString("status"));
			servBooking.setUserName(rs.getString("user_name"));
			servBooking.setPhone(rs.getString("phone"));
			servBooking.setEmail(rs.getString("email"));
 
			resultList.add(servBooking);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}
	
	public List<ServiceBooking> getServiceBookingsByProvider(String provider) throws Exception{
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(BOOKING_BY_PROVIDER_SQL); 
		
		ps.setString(1, provider);
		ps.setString(2, provider);
		
		ResultSet rs =  ps.executeQuery();
		List<ServiceBooking> resultList = new ArrayList<ServiceBooking>();
		
		while(rs.next()){
			ServiceBooking servBooking = new ServiceBooking();
			
			servBooking.setServiceBookingId(rs.getInt("service_booking_id"));
			servBooking.setServiceId(rs.getString("service_id"));
			servBooking.setUserId(rs.getString("user_id"));
			servBooking.setServiceName(rs.getString("service_name"));
			servBooking.setProviderId(rs.getString("providerId"));
			servBooking.setProvider(rs.getString("provider"));
			servBooking.setStartTime(rs.getTimestamp("start_time"));
			servBooking.setEndTime(rs.getTimestamp("end_time"));
			servBooking.setNotes(rs.getString("notes"));
			servBooking.setStatus(rs.getString("status"));
			servBooking.setUserName(rs.getString("user_name"));
			servBooking.setPhone(rs.getString("phone"));
			servBooking.setEmail(rs.getString("email"));
 
			resultList.add(servBooking);
		}
		
		rs.close();
		ps.close();
		conn.close();		
		
		return resultList;
	}
	
	
	
public List<String> getUserNameList(String provider) throws Exception{
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(USERNAME_SQL); 
		
		ps.setString(1, provider);
		
		ResultSet rs =  ps.executeQuery();
		List<String> resultList = new ArrayList<String>();
		
		while(rs.next()){

			resultList.add(rs.getString("user_name"));
		}
		
		rs.close();
		ps.close();
		conn.close();		
		
		return resultList;
	}

public void updateUser(User user) throws Exception{
	
	if(user.getUserName()!=null){
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(CHOOSE_USERNAME_SQL); 
		
		ps.setString(1, user.getUserName());
		
		ResultSet rs =  ps.executeQuery();
		if(rs.next()){
			user.setPhone(rs.getString("phone"));
			user.setEmail(rs.getString("email"));
			user.setUserId(rs.getString("user_id"));
		}
		
	}
}
	
	
	public boolean isNotAvailable(ServiceBooking booking){
		int count=0;
		Connection conn =null; 
		PreparedStatement ps =null;  
		ResultSet rs=null;
		try{
			conn=DBUtil.getConnection();
			ps=conn.prepareStatement(OVERLAP_SQL); 

		int index = 1;
		ps.setString(index++, booking.getServiceId());
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getStartTime()));
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getEndTime()));
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getStartTime()));
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(booking.getStartTime()));
		
		rs =  ps.executeQuery();

		if(rs.next()){
			count=rs.getInt(1);
		}
		
		}catch (Exception e){
			log.error(e.getMessage(), e);
		}finally{

			try{
				rs.close();
				ps.close();
				conn.close();
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
		if(count>0)
			return true;
		
		return false;
		
	}
	
	
public ServiceBooking getServiceBooking(String serviceBookingId) throws Exception{
		
		Connection conn = DBUtil.getConnection();
		PreparedStatement ps =  conn.prepareStatement(GET_SQL); 
		
		ps.setString(1, serviceBookingId);
		
		ResultSet rs =  ps.executeQuery();
		ServiceBooking servBooking =null;
		if(rs.next()){
			servBooking = new ServiceBooking();
			
			servBooking.setServiceName(rs.getString("service_name"));
			servBooking.setProviderId(rs.getString("providerId"));
			servBooking.setProvider(rs.getString("provider"));
			servBooking.setServiceBookingId(rs.getInt("service_Booking_Id"));
			servBooking.setStartTime(rs.getTimestamp("start_time"));
			servBooking.setEndTime(rs.getTimestamp("end_time"));
			servBooking.setNotes(rs.getString("notes"));
			servBooking.setStatus(rs.getString("status"));
			servBooking.setEmail(rs.getString("email"));
			servBooking.setPhone(rs.getString("phone"));
			servBooking.setServiceId(rs.getString("service_id"));
			servBooking.setUserId(rs.getString("user_id"));
			

		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return servBooking;
	}

public void archiveHistory(Date archiveDate) throws Exception{

	
	Connection conn = DBUtil.getConnection();
	PreparedStatement ps =  conn.prepareStatement("insert into service_booking_history select * from service_booking where start_time<?"); 
	ps.setTimestamp(1, DBUtil.getDBTimeStamp(archiveDate));
	ps.executeUpdate();		
	
	ps =  conn.prepareStatement("delete from service_booking where start_time<?"); 	
	ps.setTimestamp(1, DBUtil.getDBTimeStamp(archiveDate));
	ps.executeUpdate();
	
	ps =  conn.prepareStatement("delete from service_promotion where effective_date<?"); 	
	ps.setTimestamp(1, DBUtil.getDBTimeStamp(archiveDate));
	ps.executeUpdate();
	ps.close();
	conn.close();
}

public List<ServiceBooking> getNotificationList(Date fromTime,Date toTime){
	List<ServiceBooking> resultList = new ArrayList<ServiceBooking>();
	Connection conn =null; 
	PreparedStatement ps =null;  
	ResultSet rs=null;
	try{
		conn=DBUtil.getConnection();
		ps=conn.prepareStatement(NOTIFICATION_SQL); 

		int index = 1;
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(fromTime));
		ps.setTimestamp(index++, DBUtil.getDBTimeStamp(toTime));
		
		rs =  ps.executeQuery();

		
		while(rs.next()){
			ServiceBooking servBooking = new ServiceBooking();
			
			servBooking.setServiceBookingId(rs.getInt("service_booking_id"));
			servBooking.setServiceId(rs.getString("service_id"));
			servBooking.setUserId(rs.getString("user_id"));
			servBooking.setServiceName(rs.getString("service_name"));
			servBooking.setProviderId(rs.getString("provider"));
			servBooking.setProvider(rs.getString("provider"));
			servBooking.setStartTime(rs.getTimestamp("start_time"));
			servBooking.setEndTime(rs.getTimestamp("end_time"));
			servBooking.setNotes(rs.getString("notes"));
			servBooking.setStatus(rs.getString("status"));
			servBooking.setUserName(rs.getString("user_name"));
			servBooking.setPhone(rs.getString("phone"));
			servBooking.setEmail(rs.getString("email"));
	
			resultList.add(servBooking);
		}
	
	}catch (Exception e){
		log.error(e.getMessage(), e);
	}finally{

		try{
			rs.close();
			ps.close();
			conn.close();
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
		
	}

	return resultList;
}

}
