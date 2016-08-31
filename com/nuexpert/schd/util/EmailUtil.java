package com.nuexpert.schd.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.nuexpert.schd.db.UserDAO;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;
import com.nuexpert.schd.vo.User;

public class EmailUtil {
	
	protected static final Logger log = Logger.getLogger(EmailUtil.class);
   private static Properties props = System.getProperties();
   private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   
   private static Map<String,String> phoneSMSMap;
   private static UserDAO userDAO = new UserDAO();

   
    // -- Attaching to default Session, or we could start a new one --
   static{
		phoneSMSMap=new HashMap<String, String>();
		phoneSMSMap.put("Rogers", "pcs.rogers.com");
		phoneSMSMap.put("Bell", "txt.bellmobility.ca");
		phoneSMSMap.put("Telus", "msg.telus.com");
		phoneSMSMap.put("Fido", "fido.ca");
		phoneSMSMap.put("koodo", "msg.koodomobile.com");
	   
	   try{
		   props.load(classLoader.getResourceAsStream("email.properties"));
	   }catch(IOException e){
		   log.error("Error in email properties:"+e);
	   }
	   
	   /*
	    props.put("mail.smtp.host", "smtp.gmail.com");
	    props.put("mail.smtp.port", "587");

	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable","true"); 
	    props.put("mail.smtp.EnableSSL.enable","true");
	    
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		 props.put("mail.smtp.user","finescheduler@gmail.com");
		props.put("mail.smtp.password","hangzhou");
		*/
   }
	 

  public static final SimpleDateFormat dateFormat=new SimpleDateFormat("h:mm a E,MMM dd ");
  
  public static final SimpleDateFormat timeFormat=new SimpleDateFormat("h:mm a ");
   
   
   
	
	  
	  
  public static void send(String to, String from, String subject, String body)
  {
    try
    {

      
      Session session = Session.getDefaultInstance(props,
    		  new javax.mail.Authenticator() {
    			protected PasswordAuthentication getPasswordAuthentication() {
    				return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
    			}
    		  });


      
      // -- Create a new message --
      MimeMessage msg = new MimeMessage(session);
      // -- Set the FROM and TO fields --
      msg.setFrom(new InternetAddress(from));
      msg.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(to, false));
 
      msg.setContent(msg, "text/html");
      // -- We could include CC recipients too --
      // if (cc != null)
      // msg.setRecipients(Message.RecipientType.CC
      // ,InternetAddress.parse(cc, false));
      // -- Set the subject and body text --
      msg.setSubject(subject);
      msg.setText(body,"utf-8", "html");
      // -- Set some other header information --
      msg.setHeader("X-Mailer", "LOTONtechEmail");
      msg.setSentDate(new Date());
      // -- Send the message --
      Transport.send(msg);
     // System.out.println("Message sent OK.");
    }
    catch (Throwable ex)
    {
      if(log.isDebugEnabled()){
    	  log.error(ex);
      }
    }
  }
  
  public static void sendBCC(String to,String bcc, String from, String subject, String body)
  {
    try
    {

      
      Session session = Session.getDefaultInstance(props,
    		  new javax.mail.Authenticator() {
    			protected PasswordAuthentication getPasswordAuthentication() {
    				return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
    			}
    		  });


      
      // -- Create a new message --
      MimeMessage msg = new MimeMessage(session);
      // -- Set the FROM and TO fields --
      msg.setFrom(new InternetAddress(from));
      msg.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(to, false));
      msg.setRecipients(Message.RecipientType.BCC,
    	        InternetAddress.parse(bcc, false));
 
      msg.setContent(msg, "text/html");
      // -- We could include CC recipients too --
      // if (cc != null)
      // msg.setRecipients(Message.RecipientType.CC
      // ,InternetAddress.parse(cc, false));
      // -- Set the subject and body text --
      msg.setSubject(subject);
      msg.setText(body,"utf-8", "html");
      // -- Set some other header information --
      msg.setHeader("X-Mailer", "LOTONtechEmail");
      msg.setSentDate(new Date());
      // -- Send the message --
      Transport.send(msg);
     // System.out.println("Message sent OK.");
    }
    catch (Throwable ex)
    {
      if(log.isDebugEnabled()){
    	  log.error(ex);
      }
    }
  }

public static void notifyProvider(Service serv,ServiceBooking booking){
StringBuffer emailBody=new StringBuffer();
try{
  User provider=userDAO.getUser(serv.getProvider());
  String subject="New appointment Requested" ;
  emailBody.append(booking.getUserName()+" "+booking.getStatus() + " appointment  from " +timeFormat.format(booking.getStartTime())+ " to "+dateFormat.format(booking.getEndTime())+".<br/>");
  emailBody.append("Contact Phone number is  " +booking.getPhone()+".<br/>");
  emailBody.append("<br/>You can click to <a href=\"" +props.getProperty("web.url")+"/confirmbooking?bookingid="+booking.getServiceBookingId()+"\"> confirm this booking</a>");
  emailBody.append("<br/>View  <a href=\"" +props.getProperty("web.url")+"/service.jsf?serviceId="+booking.getServiceId()+"\">"+serv.getServiceName()+"</a> schedule online");

  if(provider.getPhone()!=null && phoneSMSMap.get(provider.getPhoneProvider())!=null){
	  sendBCC(provider.getEmail(),getSMSEmail(provider.getPhone(),provider.getPhoneProvider()),booking.getEmail(),subject,emailBody.toString());
  }else{
	  send(provider.getEmail(),booking.getEmail(),subject,emailBody.toString());
  }
} catch (Exception e) {
	System.out.println(e);

}		  
}


public static void notifyProviderCancell(ServiceBooking booking){
StringBuffer emailBody=new StringBuffer();

try{
  User provider=userDAO.getUser(booking.getProviderId());
  String subject="Appointment cancelled";
  emailBody.append(booking.getUserName()+" has cancelled appointment from " + timeFormat.format(booking.getStartTime())+ " to "+dateFormat.format(booking.getEndTime()));
  emailBody.append(".<br/> The contact phone number is  " +booking.getPhone());
  emailBody.append("<br/>View your service sechdule online at <a href=\"" +props.getProperty("web.url")+"/service.jsf?serviceId="+booking.getServiceId()+"\">"+booking.getServiceName()+"</a> online");
  System.out.println("Cancel notification to "+provider.getEmail());
  if(provider.getPhone()!=null && phoneSMSMap.get(provider.getPhoneProvider())!=null){
	  sendBCC(provider.getEmail(),getSMSEmail(provider.getPhone(),provider.getPhoneProvider()),booking.getEmail(),subject,emailBody.toString());
  }else{
	  send(provider.getEmail(),booking.getEmail(),subject,emailBody.toString());
  }

} catch (Exception e) {
	System.out.println(e);

}		  
}


public static void notifyUser( ServiceBooking booking){
StringBuffer emailBody=new StringBuffer();
try{
  String subject="Appointment Confirmed ";
  emailBody.append(" Your appointment of "+booking.getServiceName() +" from "  +timeFormat.format(booking.getStartTime())+ " to "+dateFormat.format(booking.getEndTime()));
  emailBody.append(" has been "+booking.getStatus());
  if(booking.getProvider()!=null)
	  emailBody.append(" by "+booking.getProvider()+". <br/>");
  else
	  emailBody.append(". <br/>");
  emailBody.append("<br/>You can view your <a href=\"" +props.getProperty("web.url")+"/service.jsf?serviceId="+booking.getServiceId()+"\">"+booking.getServiceName()+"</a> appointment online.");

 // send("leishen7@gmail.com","support@finescheduler.com",subject,emailBody.toString());
  System.out.println("Notify user:"+booking.getEmail());
  send(booking.getEmail(),props.getProperty("mail.smtp.user"),subject,emailBody.toString());
} catch (Exception e) {
	System.out.println(e);

}		  
}


public static void appointmentNotification( ServiceBooking booking){
StringBuffer emailBody=new StringBuffer();
try{
  String subject="You have an appointment";
  emailBody.append(booking.getServiceName() +" with "+booking.getProvider()+" appointment will start on "  +timeFormat.format(booking.getStartTime()));
  sendBCC(booking.getEmail(),getSMSEmail(booking.getPhone(),booking.getPhoneProvider()),props.getProperty("mail.smtp.user"),subject,emailBody.toString());
  User provider=userDAO.getUser(booking.getProviderId());

  emailBody.delete(0, emailBody.length());
  emailBody.append(booking.getServiceName() +" with "+booking.getUserName()+" appointment will start on "  +timeFormat.format(booking.getStartTime()));
  sendBCC(provider.getEmail(),getSMSEmail(provider.getPhone(),provider.getPhoneProvider()),booking.getEmail(),subject,emailBody.toString());
} catch (Exception e) {
	System.out.println(e);

}		  
}


public static void contactUs(String question,String questionDesc, User user){
StringBuffer emailBody=new StringBuffer();

try{
  
  //String subject=booking.getServiceName() +" notification from "+ booking.getProvider();
  if("Forget Password".equals(question)){
		  send(user.getEmail(),props.getProperty("mail.smtp.user"),"finescheduler.com support","Your password at finescheduler.com is "+ user.getPassword());
		  WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.forget.password"), WebUtil.getMessage("message.forget.password.detail")));
			
  }else{
	  emailBody.append(user.getFirstname()+" ");
	  if(user.getLastname()!=null)
		  emailBody.append(user.getLastname()+" ");
	  emailBody.append(" contact us of question " +question);
	  if(user.getPhone()!=null)
	  emailBody.append(" His/her phone is  "+user.getPhone());
	  if(questionDesc!=null && questionDesc.length()>1 )
		  emailBody.append(" His/her question is  "+questionDesc);
	  send(props.getProperty("mail.smtp.user"),user.getEmail(),user.getFirstname()+" contact us about "+question,emailBody.toString());
	  WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.contact.us"), WebUtil.getMessage("message.contact.us.detail")));
		
	  
  }
		  

} catch (Exception e) {
	System.out.println(e);

}		  
}

private static String getSMSEmail(String phone, String phoneProvider){
	return "1"+phone+"@"+phoneSMSMap.get(phoneProvider);
}

/**
* @param args
*/
public static void main(String[] args) {

EmailUtil.send("leishen7@gmail.com", "shenlei@rogers.com", "test", "Hello");


}


}
