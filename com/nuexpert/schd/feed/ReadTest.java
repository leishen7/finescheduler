package com.nuexpert.schd.feed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReadTest {
	  public static void main(String[] args) {
		  
		  String str="";
		  try {
			 
			  ReadingFeedParser parser = new ReadingFeedParser("http://feed.feedsky.com/clzzxf");
			    Feed feed = parser.readFeed();
			    System.out.println(feed.getTitle());
			    System.out.println("---------------------------------");
			    for (FeedMessage message : feed.getMessages()) {
			      System.out.println(message.getTitle());
			      System.out.println(message.getLink());
			      System.out.println(message.getContent());
			      System.out.println("---------------------------------");
			    }
			      } catch (Exception e) {
			        throw new RuntimeException(e);
			      }
		  
		  System.out.println(str);
	} 
	  
	  
}