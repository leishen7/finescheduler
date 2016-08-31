package com.nuexpert.schd.web;

import static com.nuexpert.schd.util.Status.CONFIRMED;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;

import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.db.UserDAO;
import com.nuexpert.schd.util.WebUtil;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.ServiceBooking;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ScheduleController extends AbstractScheduleController{
	
	@ManagedProperty("#{serviceBookingDAO}")
	private ServiceBookingDAO serviceBookingDAO;
	
	@ManagedProperty("#{loginController}")
	private LoginController loginController;
	
    public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	@PostConstruct
	public void init() {
		eventModel = new DefaultScheduleModel();
		
		try{
			super.init();
			if(getServ()!=null){
				//List<ServiceBooking> bookingList = getServiceBookingDAO().getServiceBookingsByProvider(getServ().getProvider());for shared service
				List<ServiceBooking> bookingList = getServiceBookingDAO().getServiceBookingsById(getServ().getServiceId());
				Appointment appt=null;
				if (null!=bookingList){
					for(ServiceBooking booking: bookingList){
						
						if (!booking.getStartTime().equals(booking.getEndTime())){
							
							if (StringUtils.equalsIgnoreCase(booking.getUserId(), userId)){
								appt = new Appointment(booking, getEventTitle(booking), booking.getStartTime(), booking.getEndTime());		
								appt.setNotes(booking.getNotes());
								if((booking.getStartTime().getTime()-new Date().getTime())/(1000*60*60)<4){
									appt.setStyleClass("pastEvent");
								}else if(booking.getStatus().equals(CONFIRMED)){
									appt.setStyleClass("confirmedEvent");
								}
								else if(getServ().getServiceId().equals(booking.getServiceId()))
									appt.setStyleClass("ownEvent");
								else
									appt.setStyleClass("otherService");
								
							}else{
								appt = new Appointment(booking, "", booking.getStartTime(), booking.getEndTime());	
								appt.setStyleClass("otherUser");
							}
							
							eventModel.addEvent(appt);
						}
					}
				}				
			}else{
				
				List<ServiceBooking> bookingList = getServiceBookingDAO().getServiceBookings(userId);
				Appointment appt=null;
				if (null!=bookingList){
					for(ServiceBooking booking: bookingList){
						
						if (!booking.getStartTime().equals(booking.getEndTime())){
							
							if (StringUtils.equalsIgnoreCase(booking.getUserId(), userId)){
								appt = new Appointment(booking, getEventTitle(booking), booking.getStartTime(), booking.getEndTime());						
								if((booking.getStartTime().getTime()-new Date().getTime())/(1000*60*60)<4){
									appt.setStyleClass("pastEvent");
								}else if(booking.getStatus().equals(CONFIRMED)){
									appt.setStyleClass("confirmedEvent");
								}else 
									appt.setStyleClass("ownEvent");
								
							}else{
								appt = new Appointment(booking, "", booking.getStartTime(), booking.getEndTime());
								appt.setStyleClass("otherUser");
							}
							
							eventModel.addEvent(appt);
						}
					}
				}			
			}
								
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}		
	}
    
    @Override
    public void onDateSelect(SelectEvent selectEvent) {
		if(userId==null){
			//RequestContext.getCurrentInstance().execute("PF('loginDialog').show();");	
			ExternalContext externalContext=FacesContext.getCurrentInstance().getExternalContext();
	        String originalURL = (String) externalContext.getRequestHeaderMap().get("referer");

	       if (originalURL != null) {

	        getLoginController().setOriginalURL(originalURL);
	       }
	        
			try{
			externalContext.redirect(externalContext.getRequestContextPath() + "/login2.jsf");
			}catch (Exception e) {
				// TODO: handle exception
			}
			
				return;
	
		}
		
		Date start=(Date) selectEvent.getObject();
		Date end =(Date) selectEvent.getObject();
		if((start.getTime()-new Date().getTime())/(1000*60*60)<4){
			return;
		}
		if(start.getHours()<=1){
			return;

		}else{
			c.setTime(start);
			//c.add(Calendar.HOUR, -1);
			//start=c.getTime();
			int serviceTime=getServ().getMinTime();
			c.add(Calendar.MINUTE, serviceTime);
			end=c.getTime();
			newAppointment = new Appointment(null, "", start, end);
		}
		RequestContext.getCurrentInstance().execute("PF('addingDialog').show();");	
	}
    
    @Override
	public void onEventSelect(SelectEvent selectEvent) {
		existingAppointment = (Appointment) selectEvent.getObject();
		
		if (existingAppointment.getServiceBooking()!=null 
				&& (getServ()==null || existingAppointment.getServiceBooking().getServiceId().equals(getServ().getServiceId()))
				&& !StringUtils.isBlank(existingAppointment.getServiceBooking().getUserId()) 
				&& StringUtils.equalsIgnoreCase(existingAppointment.getServiceBooking().getUserId(), userId)
				&& ((existingAppointment.getStartDate().getTime()-new Date().getTime())/(1000*60*60)>3)){
			RequestContext.getCurrentInstance().execute("PF('editingDialog').show();");	
		}
	}
	
	@Override
	public ServiceBookingDAO getServiceBookingDAO() {
		return serviceBookingDAO;
	}

	public void setServiceBookingDAO(ServiceBookingDAO serviceBookingDAO) {
		this.serviceBookingDAO = serviceBookingDAO;
	}

	


	@Override
	public ServiceBooking createServiceBooking() {

		
		ServiceBooking booking = new ServiceBooking();
		
		booking.setServiceId(getServ().getServiceId());
		
		return booking;
	}

	@Override
	public String getEventTitle(ServiceBooking booking) {
		StringBuffer sb=new StringBuffer();
		
		if (null!=booking){
			sb.append(booking.getServiceName()+ "  ");
			if(getServ()!=null && getServ().getProviderUser()!=null){
					sb.append(" "+getServ().getProviderUser().getUserName());
				
			}else{
				
				if(booking.getProvider()!=null)
					sb.append(" "+booking.getProvider());
			}
		
		}
		return sb.toString();
	}
	
	public void login(){
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		
		try {
			if (StringUtils.isBlank(request.getRemoteUser())){
				//request.logout();
				request.login(getUser().getUserId(), getUser().getPassword());
				
				userId = request.getRemoteUser();
				init();
							
			}
		} catch (Exception e) {
			//log.error(e.getCause(), e);
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("login.failed.header"), WebUtil.getMessage("login.failed.message")));
			
			//context.addMessage(null, new FacesMessage("Login failed."));
		}
	}
}
