package com.nuexpert.schd.feed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

public class ReadingFeedParser {
  static final String TITLE = "title";
  static final String ENTRY="entry";
  static final String DESCRIPTION = "description";
  static final String CHANNEL = "channel";
  static final String LANGUAGE = "language";
  static final String COPYRIGHT = "copyright";
  static final String LINK = "link";
  static final String AUTHOR = "author";
  static final String ITEM = "item";
  static final String PUB_DATE = "pubDate";
  static final String GUID = "guid";
  static final String CONTENT ="content";

  final URL url;

  public ReadingFeedParser(String feedUrl) {
    try {
      this.url = new URL(feedUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public Feed readFeed() {
    Feed feed = null;
    try {
      boolean isFeedHeader = true;
      // Set header values intial to the empty string
      String description = "";
      String title = "";
      String link = "";
      String language = "";
      String copyright = "";
      String author = "";
      String pubdate = "";
      String guid = "";

      // First create a new XMLInputFactory
      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      // Setup a new eventReader
      InputStream in = read();
      XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
      // read the XML document
      String localPart="";
       while (eventReader.hasNext()) {
        XMLEvent event = eventReader.nextEvent();

        if(event.isStartElement())
          localPart = event.asStartElement().getName().getLocalPart();
        else if(event.isEndElement())
        	localPart = event.asEndElement().getName().getLocalPart();
        else
        	 continue;
        System.out.println(localPart);

        	System.out.println(getCharacterData(event, eventReader));

        FeedMessage message=new FeedMessage();
          if (TITLE.equals(localPart)) {
        	  if(feed==null){
        		  title=getCharacterData(event, eventReader);
        		  feed= new Feed(title, link, description, language, copyright, null);
        		  continue;
        	  }
        	  
            message = new FeedMessage();

            message.setTitle(getCharacterData(event, eventReader));

            feed.getMessages().add(message);
            event = eventReader.nextEvent();
            continue;         
          
          }else if(LINK.equals(localPart)){
        	  message.setLink(getCharacterData(event, eventReader));
          }else if(DESCRIPTION.equals(localPart)){
        	  message.setDescription(getCharacterData(event, eventReader));
          }else if("encoded".equals(localPart)){
        	  //System.out.println(event)
        	  message.setContent(getCharacterData(event, eventReader));
          }
        
      }
      
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    return feed;
  }

  private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
      throws XMLStreamException {
    String result = "";
    event = eventReader.nextEvent();
    if (event instanceof Characters) {
      result = event.asCharacters().getData();
    }
    return result;
  }

  private InputStream read() {
    try {

        return url.openStream();
  /*
    	InputStream in=new FileInputStream(new File("c:\\shenlei\\zreading.txt"));
    	return in;*/
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
} 