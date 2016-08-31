package com.nuexpert.schd.servlet;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.nuexpert.schd.db.DBUtil;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.feed.GasPriceForcast;
import com.nuexpert.schd.feed.WeatherForcast;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.vo.ServiceBooking;

public class StartupServlet extends HttpServlet implements Runnable{
	
	ServiceBookingDAO serviceBookingDAO;
	
Thread refreshData; 

	public void init() throws ServletException{ 
		serviceBookingDAO=new ServiceBookingDAO();
		 Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	     try{
	        serviceBookingDAO.archiveHistory(cal.getTime());
	     }catch(Exception e){
	    	 System.out.println(e);
	     }
		   
		 refreshData=new Thread(this);
		 refreshData.setPriority(Thread.MIN_PRIORITY);
		 refreshData.start();
	}
	
	   @Override
	public void run() {
		   Calendar toCal;
		   Date fromTime=null;
		   int i=0;
		   while(true){
			   toCal = Calendar.getInstance();
			   toCal.add(Calendar.MINUTE, 120);
			   if(fromTime==null)
				   fromTime=toCal.getTime();

			   List<ServiceBooking> notifyList= serviceBookingDAO.getNotificationList(fromTime,toCal.getTime());
			   fromTime=toCal.getTime();
			   
			   for(ServiceBooking booking:notifyList){
				   EmailUtil.appointmentNotification(booking);
				   
			   }
			   
			   if(i==0 || i++>59) {
				   i=1;
				   try{
		
					   WeatherForcast.loadData();
					  
					   System.out.println("----------  load Weather data successfully ----------");   
				   }catch(Exception ex){
					   
					   System.out.println("----------"+ex);
				   }
				   try{
					  GasPriceForcast.loadData();
					  //System.out.println("----------  load GAS data successfully ----------");  
		 
				   }catch(Exception ex){
					   
					   System.out.println("----------"+ex);
				   }
				   
				   
				   
			   }
			   i++;
			   try{
				   Thread.sleep(1000*60*60*1);
			   }catch(InterruptedException ignored){
				   //System.out.println("----------  Servlet Initialized successfully ----------");   
			   }
		   }
		
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		System.out.println("Start to load data");
		//super.service(arg0, arg1);
	}

}
