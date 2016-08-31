package com.nuexpert.schd.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang3.StringUtils;

import com.nuexpert.schd.db.WeatherDAO;
import com.nuexpert.schd.vo.Weather;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class WeatherForcast implements Serializable{
	
	private final static String SOURCE_URL="http://weather.gc.ca/rss/city/on-143_e.xml";
	
	//private static List<String>  weekForcast;
	private static Map<String, List<String>> weatherMap=new ConcurrentHashMap<String, List<String>>();
	private static WeatherDAO weatherDAO=new WeatherDAO();
	static public List<String> getWeekForcast(String city){
		if(StringUtils.isBlank(city))
			return null;
		/*
		Calendar current=Calendar.getInstance(TimeZone.getTimeZone("Canada/Central"));
		if(weekForcast==null || current.after(c) ){
			loadData();
			System.out.println("Refresh Weather Data");
			c.setTime(current.getTime());
			c.add(Calendar.HOUR, 1);
		}*/
		return weatherMap.get(city);
	}
	
	public static void loadData() throws Exception{
		List<String> weatherCityList=weatherDAO.getWeatherCityList();
		for(String city:weatherCityList){
			
			Weather weather=weatherDAO.getWeather(city);
			if(weather!=null){
		
			List<String> weatherList=new ArrayList<String>();
		 	RSSFeedParser parser = new RSSFeedParser(weather.getUrl());
		    Feed feed = parser.readFeed();
		    //System.out.println(feed.getTitle());
		    for (FeedMessage message : feed.getMessages()) {
		    	if(message.getTitle().contains(":") ){
		    		StringTokenizer st = new StringTokenizer(message.getTitle(),":");
		    		String dayOfWeek=st.nextToken();
		    		if(dayOfWeek.contains("Current Conditions")){
		    			weatherList.add(city+" weather:"+st.nextToken());
		    		}else if(dayOfWeek.contains("night")){
		    			//weatherList.add("Night:"+st.nextToken());
		    		}else{
		    		
		    			weatherList.add(st.nextToken());
		    		}
		    	}
		     // System.out.println(message.getTitle());

		    }

		 
		
		    weatherMap.put(city, weatherList);
		}
		}
	}



}
