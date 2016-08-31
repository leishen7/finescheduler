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
import com.nuexpert.schd.vo.Weather;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class WeatherDAO implements Serializable{
	private final static String QUERY_CITY_SQL="select distinct city from weather";
	private final static String CITY_WEATHER_SQL="select * from weather where city=?";
	
	public List<String> getWeatherCityList() throws Exception{		
		
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(QUERY_CITY_SQL); 
		
		ResultSet rs =  ps.executeQuery();
		List<String> resultList = new ArrayList<String>();
		
		while(rs.next()){
			resultList.add(rs.getString(1));
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		return resultList;
	}
	
public Weather getWeather(String city) throws Exception{
		
	if (StringUtils.isBlank(city)){
		return null;
	}
		Connection conn = DBUtil.getConnection();
		
		PreparedStatement ps =  conn.prepareStatement(CITY_WEATHER_SQL); 
		ps.setString(1, city);
		ResultSet rs =  ps.executeQuery();
		Weather weather=null;
		if(rs.next()){
			weather = new Weather();			
			weather.setCity(rs.getString("city"));
			weather.setCityCode(rs.getString("city_code"));
			weather.setUrl(rs.getString("url"));
			weather.setWidget(rs.getString("widget"));
	
		}
		
		rs.close();
		ps.close();
		conn.close();
		

		return weather;

	}

}
