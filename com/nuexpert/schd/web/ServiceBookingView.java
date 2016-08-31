package com.nuexpert.schd.web;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.vo.ServiceBooking;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ServiceBookingView implements Serializable {
	private static final Logger log = Logger.getLogger(ServiceBookingView.class);

	
	private String userId=null;
	

	@ManagedProperty("#{serviceBookingDAO}")
	private ServiceBookingDAO serviceBookingDAO;



	private List<ServiceBooking> bookingList;

    public List<ServiceBooking> getBookingList() {
		return bookingList;
	}

	public void setBookingList(List<ServiceBooking> bookingList) {
		this.bookingList = bookingList;
	}

	@PostConstruct
	public void init() {
	
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
			userId = requestMap.get("userId");
			
			if (null==userId){
				log.warn("cannto acquired userId...");
				return;
			}
			
			log.debug("load events of  user ==> " + userId);


			bookingList = serviceBookingDAO.getServiceBookings(userId);
			
			log.debug("Number of Service Booking for " + userId+" is "+bookingList.size());
		
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}



	public ServiceBookingDAO getServiceBookingDAO() {
		return serviceBookingDAO;
	}

	public void setServiceBookingDAO(ServiceBookingDAO serviceBookingDAO) {
		this.serviceBookingDAO = serviceBookingDAO;
	}
}

