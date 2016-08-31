package com.nuexpert.schd.feed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@SuppressWarnings("serial")
@ManagedBean
@ApplicationScoped
public class GasPriceForcast implements Serializable{
	
	private final static String FORCAST_URL="http://tomorrowsgaspricetoday.com";
	private final static String FORCAST_PRICE_URL="http://tomorrowsgaspricetoday.com/gas-prices/?city=133&Toronto-Gas-Prices";
	private static Calendar c=Calendar.getInstance(TimeZone.getTimeZone("Canada/Central"));
	private static String forcast;
	
	public void setForcast(String forcast) {
		this.forcast = forcast;
	}

	public String getForcast(){
		/*
		Calendar current=Calendar.getInstance(TimeZone.getTimeZone("Canada/Central"));
		if(forcast==null || current.after(c) ){
			loadData();
			System.out.println("Refresh Gas Data");
			c.setTime(current.getTime());
			c.add(Calendar.HOUR, 4);
		}*/
		return forcast;
	}
	
	public static void loadData(){
		
		URL priceURL;
		URL url;
		String data;  
		  try {
			  priceURL=new URL(FORCAST_PRICE_URL);
		      url = new URL(FORCAST_URL);
		    } catch (MalformedURLException e) {
		      throw new RuntimeException(e);
		    }

		  StringBuffer strBuf=new StringBuffer();
		  try {

			  //strBuf.append("Gas price will ");
			    BufferedReader bufReader= new BufferedReader(new InputStreamReader(url.openStream()));
				 
			//BufferedReader bufReader= new BufferedReader(new FileReader("C:\\shenlei\\gas.htm"));
			  
			  
			   
			   while ((data = bufReader.readLine()) != null) { 
				   int pos2=data.indexOf("TGPT FRIENDS");

				   if(pos2>0){
					   String str = bufReader.readLine();
					   int pos1=str.indexOf(" PRICE");
					   pos2=str.indexOf(" prices ");
					   if(pos1>0 && (pos2<0 || pos1<pos2))
						   pos2=pos1;
					   if(pos2>0){
						   str=str.substring(pos2);
						   int pos3=str.indexOf("AND");
						   int pos4=str.indexOf("&#");
						   int pos5=str.indexOf("!");
						   if(pos4>0 && (pos3<0 || pos4<pos3))
							   pos3=pos4;
						   if(pos5>0 && (pos3<0 ||pos5<pos3))
							   pos3=pos5;
						   
						   pos5=str.indexOf("#8230");
						   if(pos5>0 && (pos3<0 ||pos5<pos3))
							   pos3=pos5;
	
							
							  if(pos3>23)
								  str=str.substring(0,pos3);
					
							  str=StringUtils.remove(str, "<strong>");
							  str=StringUtils.remove(str, "</strong>");
							  str=StringUtils.remove(str, "</div>");
							 if(str.length()>150){
								 str=str.substring(0,149);
								 if(str.lastIndexOf(".")>25)
									 str=str.substring(0,str.lastIndexOf(".")-1);
								 else  if(str.lastIndexOf(",")>25)
									 str=str.substring(0,str.lastIndexOf(","))+".";
									 
							 }
							  strBuf.append("Gas "+str);
							  break;
					   }
				  
				   }
			

			  }   

			   
				// bufReader= new BufferedReader(new FileReader("C:\\shenlei\\price.htm"));
				bufReader= new BufferedReader(new InputStreamReader(priceURL.openStream()));
				  
				   while ((data = bufReader.readLine()) != null) { 
					   int pos=data.indexOf("<span id=\"gas-info-span\">");
					   if( pos>0){
						   String str2=data.substring(pos+25,pos+30);
						   if(NumberUtils.isNumber(str2)){
							   strBuf.append(" Tomorrow's average Gas price in Toronto will be "+str2+".");
							   break;
						   }
					   }else{
						   pos=data.indexOf("gas-info-span</span>");
						   if(pos>0){
							   String str2=data.substring(pos+32,pos+37);
							   if(NumberUtils.isNumber(str2)){
								   strBuf.append(" Tomorrow's average Gas price in Toronto will be "+str2+".");
								   break;
							   }
						   }
					   }
				   }
			  
			
		
      } catch (Throwable e) {
    	  System.out.println("Error when getting Gas Data "+e);
      }
	
		if(strBuf.length()>20)
			forcast= strBuf.toString();
		else
			forcast="";
		
	}

	public static void main(String[] args) {
		GasPriceForcast gasPriceForcast=new GasPriceForcast();
		
		System.out.println(gasPriceForcast.getForcast());
	}

}
