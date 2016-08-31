package com.nuexpert.schd.feed;

import java.util.Calendar;

import javax.faces.bean.ManagedProperty;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.nuexpert.schd.db.UserDAO;

public class CacheStartListener implements ServletContextListener {
	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("destroy Cache");

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Start to load into Cache");
		Calendar c=Calendar.getInstance();
		Thread thread=new Thread();
		while(true){
			System.out.println("Refresh Gas Price");
			GasPriceForcast.loadData();
			try{
				thread.wait(60*60*1000);
			}catch(InterruptedException ie){
				
			}
			
		}
		

	}

}
