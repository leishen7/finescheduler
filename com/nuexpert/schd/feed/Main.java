package com.nuexpert.schd.feed;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.xpath.*;
import javax.xml.parsers.*;

/**
 * 
 * @author Peter Wang
 */
public class Main {

	public static void main(String[] args) throws Exception {

		//
		try {
			//
			DocumentBuilderFactory builderfactory = DocumentBuilderFactory
					.newInstance();
			builderfactory.setNamespaceAware(true);

			DocumentBuilder builder = builderfactory.newDocumentBuilder();
			
			// Read from internet
			//URL url = new URL("http://feed.feedsky.com/clzzxf");
			//InputStream inputStream = url.openStream();
			//Reader reader = new InputStreamReader(inputStream, "UTF-8");
			//InputSource inputSource = new InputSource(reader);
			//Document xmlDocument = builder.parse(inputSource);

			// Read RSS from a file for testing purpose
			Document xmlDocument = builder.parse(new File(Main.class
					.getResource("zreading.xml").getFile()));

			XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
			XPath xPath = factory.newXPath();

			XPath xpath = XPathFactory.newInstance().newXPath();
			//XPathExpression expr = xpath.compile("//rss/channel/item/title/text()");
			//NodeList nodes = (NodeList) expr.evaluate(xmlDocument,	XPathConstants.NODESET);
			//for (int i = 0; i < nodes.getLength(); i++) {
			//	String title = nodes.item(i).getNodeValue();
			//	System.out.println(title);
			//}
			String title = "";
			String link = "";
			String description = "";
			String language = "";
			String copyright = "";			
			
			//
			XPathExpression expr = xpath.compile("//rss/channel/title/text()");
			NodeList nodes = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
			    title = nodes.item(i).getTextContent();
			    System.out.println(" rss title = " + title);
			}
			
			expr = xpath.compile("//rss/channel/link/text()");
			nodes = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
			    link = nodes.item(i).getTextContent();
			    System.out.println(" rss link = " + link);
			}
			
			expr = xpath.compile("//rss/channel/description/text()");
			nodes = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
				description = nodes.item(i).getTextContent();
			    System.out.println(" rss description = " + description);
			}
			
			expr = xpath.compile("//rss/channel/language/text()");
			nodes = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
				language = nodes.item(i).getTextContent();
			    System.out.println(" rss language = " + language);
			}
			
			expr = xpath.compile("//rss/channel/copyright/text()");
			nodes = (NodeList)expr.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
				copyright = nodes.item(i).getTextContent();
			    System.out.println(" rss copyright = " + copyright);
			}
			
			Feed feed = new Feed(title, link, description, language,	copyright, null);
			
			//
			System.out.println("//rss/channel/item/*");
			expr = xpath.compile("//rss/channel/item/*");
			nodes = (NodeList) expr.evaluate(xmlDocument,	XPathConstants.NODESET);
	        //
			FeedMessage fMesg = new FeedMessage();	
	        for (int i = 0; i < nodes.getLength(); i++) {
        	
	        	        	  
	        	//
	        	String nodeName = nodes.item(i).getNodeName();
	            String nodeValue = nodes.item(i).getTextContent(); 
	            //
	            if( nodeName.equals( "title" ) ) {
                    fMesg.setTitle(nodeValue);
	            } 
	            else if( nodeName.equals( "description" ) ) {
	            	fMesg.setDescription(nodeValue);
	            } 
	            else if( nodeName.equals( "content" ) ) {
	            	fMesg.setContent(nodeValue);
	            }
	        }
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}
	
	
}