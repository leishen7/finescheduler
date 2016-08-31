package com.nuexpert.schd.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.nuexpert.schd.db.RssFeedDAO;
import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.feed.Feed;

@SuppressWarnings("serial")
@ManagedBean(name="switcher")
@SessionScoped
public class LanguageSwitcher implements Serializable{
	
	private static final Logger log = Logger.getLogger(LanguageSwitcher.class);
	private Locale locale;
	
	private String feedHeader;
	
	private String feedLink;
	
	private List<String> feedTitleList=new ArrayList<String>();

	private Map<String,String> feedMap =new HashMap<String,String>();
 
	private List<Feed> feedList;
	
	private static int newsCount=0;
	
	@ManagedProperty("#{rssFeedDAO}")
	private RssFeedDAO rssFeedDAO;
	

	public RssFeedDAO getRssFeedDAO() {
		return rssFeedDAO;
	}

	public void setRssFeedDAO(RssFeedDAO rssFeedDAO) {
		this.rssFeedDAO = rssFeedDAO;
	}

	@PostConstruct
    public void init() {
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 
     populateRssFeed();  
     String requestNews= FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("feedHeader");
    // System.out.println("requestNews="+requestNews);
     if(requestNews!=null){
    	 feedHeader = requestNews;
    	 feedLink=feedMap.get(feedHeader);
    	 System.out.println(feedLink+"hhhh---"+feedHeader);
     }else{
    	 	feedHeader=feedList.get(0).getTitle();
    		feedLink=feedList.get(0).getLink();
     }
     
     
    // System.out.println(feedHeader+">>>>>>>"+feedLink);

	
			
		
		  
		
        
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public void setLanguage(String language) {
        locale = new Locale(language);
  
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        /*
        try{
        	serviceController.populateCategory(locale);
        }catch (Exception e) {
			// TODO: handle exception
		}*/
        populateRssFeed();
        if(feedList.size()>0){
	        feedHeader=feedList.get(0).getTitle();
			feedLink=feedList.get(0).getLink();
        }
    }
    
	public String getFeedHeader() {
		return feedHeader;
	}

	public void setFeedHeader(String feedHeader) {
		this.feedHeader = feedHeader;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}
	

	public List<String> getFeedTitleList() {
		return feedTitleList;
	}

	public void setFeedTitleList(List<String> feedTitleList) {
		this.feedTitleList = feedTitleList;
	}
	
	
	private void populateRssFeed(){
		try{
        	feedList=rssFeedDAO.getFeedList(locale.getLanguage());
        	if(feedList.size()>0){
        		feedTitleList.clear();
        		feedMap.clear();
        		for(int i=0;i<feedList.size();i++){
        			feedTitleList.add(feedList.get(i).getTitle());
        			feedMap.put(feedList.get(i).getTitle(),feedList.get(i).getLink());
        		}
        		
        	}
        } catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public String feedChange(){
		//populateRssFeed();
		//System.out.println("hhhh---"+feedHeader);
		feedLink=feedMap.get(feedHeader);
		//System.out.println("hhhh---"+feedLink);
		
		return feedLink;
	}
}