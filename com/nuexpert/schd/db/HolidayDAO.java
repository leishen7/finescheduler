package com.nuexpert.schd.db;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.vo.Holiday;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class HolidayDAO implements Serializable{
	private final static String QUERY_SQL="select * from holiday_cal where holiday_cal_id=?";
	
	public List<Holiday> getHolidays(String holidayCalId) throws Exception{
		
		if (StringUtils.isBlank(holidayCalId)){
			return null;
		}
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(QUERY_SQL); 
		
		ps.setString(1, holidayCalId);
		
		ResultSet rs =  ps.executeQuery();
		List<Holiday> resultList = new ArrayList<Holiday>();
		
		while(rs.next()){
			Holiday holiday = new Holiday();			
			holiday.setHolidayCalId(holidayCalId);
			holiday.setHolidayName(rs.getString("holiday_name"));
			holiday.setDate(rs.getDate("holiday_date"));
			resultList.add(holiday);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}

}
