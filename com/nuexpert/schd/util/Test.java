/**
 * 
 */
package com.nuexpert.schd.util;

import java.io.InputStream;
import java.net.URL;


/**
 * @author lshen
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
		URL url = new URL("http://weather.gc.ca/rss/city/on-143_e.xml");         
		InputStream xml = url.openStream(); 
		System.out.println(xml.toString());
		}catch(Exception e){
			e.printStackTrace();
		}

//WeatherView weather=new WeatherView();
//System.out.println(weather.retrieveFeed("http://weather.gc.ca/rss/city/on-143_e.xml"));
		//EmailUtil.send("sammyshen7@hotmail.com","support@finescheduler.com", "test", "Hello");


	}

}
