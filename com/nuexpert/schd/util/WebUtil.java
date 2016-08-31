package com.nuexpert.schd.util;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

public class WebUtil {
	private static final Logger log = Logger.getLogger(WebUtil.class);
	private final static ResourceBundle bundle = ResourceBundle.getBundle("fineschedule", FacesContext.getCurrentInstance().getViewRoot().getLocale());

	public static String getMessage(String key){
		
		if (null!=bundle){
			return bundle.getString(key);
		}
		
		log.error("getBundle failed.");
		
		return null;
	}
	
	public static void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public static int extractHour(String timeStr){
		StringTokenizer st= new StringTokenizer(timeStr,":");
		return Integer.parseInt(st.nextToken());
	}
	
	public static int extractMinute(String timeStr){
		StringTokenizer st= new StringTokenizer(timeStr,":");
		st.nextToken();
		return Integer.parseInt(st.nextToken());
	}
	
	public static int minuteBetween(String startTime, String endTime){
		int hourDiff=extractHour(endTime)-extractHour(startTime);
		int minuteDiff=extractMinute(endTime)-extractMinute(startTime);
		return hourDiff*60+minuteDiff;
	}
	
	public static int minuteBetween(Date start, Date end){
		if( start == null || end == null ) return 0;
		 return (int)((end.getTime()/60000) - (start.getTime()/60000));
		
	}
}
