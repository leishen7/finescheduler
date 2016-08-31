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
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;

import com.nuexpert.schd.db.HolidayDAO;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.util.Status;
import com.nuexpert.schd.util.WebUtil;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;
import com.nuexpert.schd.vo.ServicePromotion;
import com.nuexpert.schd.vo.User;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ServiceAppointmentController extends AbstractScheduleController{
	
	@ManagedProperty("#{serviceBookingDAO}")
	private ServiceBookingDAO serviceBookingDAO;	
	
	private User servUser;
	
	private List<String> usernames;

	public List<String> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}

	public User getServUser() {
		return servUser;
	}

	public void setServUser(User servUser) {
		this.servUser = servUser;
	}

	@PostConstruct
	public void init() {
		eventModel = new DefaultScheduleModel();
		servUser= new User();
		
		try{
			super.init();
			if(getServ()!=null){
				List<ServiceBooking> bookingList = getServiceBookingDAO().getServiceBookingsById(getServ().getServiceId());
				Appointment appt=null;
				if (null!=bookingList){
					for(ServiceBooking booking: bookingList){
						
						if (booking.getStatus()==null || !booking.getStatus().equals("Rejected")){
							
			
								appt = new Appointment(booking, getEventTitle(booking), booking.getStartTime(), booking.getEndTime());	
								appt.setNotes(booking.getNotes());
								if((booking.getStartTime().getTime()-new Date().getTime())/(1000*60*60)<4){
									appt.setStyleClass("pastEvent");
								}else if(booking.getStatus().equals(CONFIRMED)){
									appt.setStyleClass("confirmedEvent");
								}else if(userId.equals(booking.getUserId())){
									appt.setStyleClass("otherService");
			
								}else{
									if(getServ().getServiceId().equals(booking.getServiceId()))
										appt.setStyleClass("ownEvent");
									else
										appt.setStyleClass("otherUser");
								}
							
								
						
							
							eventModel.addEvent(appt);
						}
					}
				}				
			}else{
				
				List<ServiceBooking> bookingList = getServiceBookingDAO().getServiceBookingsByProvider(userId);
				Appointment appt=null;
				if (null!=bookingList){
					for(ServiceBooking booking: bookingList){
		
								appt = new Appointment(booking, getEventTitle(booking), booking.getStartTime(), booking.getEndTime());						
								appt.setNotes(booking.getNotes());
								if((booking.getStartTime().getTime()-new Date().getTime())/(1000*60*60)<4){
									appt.setStyleClass("pastEvent");
								}else if(booking.getStatus().equals(CONFIRMED)){
									appt.setStyleClass("confirmedEvent");
								}else 
									appt.setStyleClass("ownEvent");
							
							
							eventModel.addEvent(appt);
						
					}
				}			
			}
	
				usernames=getServiceBookingDAO().getUserNameList(userId);
	
								
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}		
	}
    
    @Override
	public void onEventSelect(SelectEvent selectEvent) {
		existingAppointment = (Appointment) selectEvent.getObject();
		
		if (existingAppointment.getServiceBooking()!=null 
				&& !StringUtils.isBlank(existingAppointment.getServiceBooking().getUserId()) 
				&&(getServ()==null || getServ().getServiceId().equals(existingAppointment.getServiceBooking().getServiceId()))
				//&& ((existingAppointment.getStartDate().getTime()-new Date().getTime())/(1000*60*60)>3)){
			&& ((existingAppointment.getServiceBooking().getStartTime().getTime()-new Date().getTime())/(1000*60*60)>3)){
			if(existingAppointment.getServiceBooking().getStatus()!=null && existingAppointment.getServiceBooking().getStatus().equals(Status.REQUESTED)){
				RequestContext.getCurrentInstance().execute("PF('acceptingDialog').show();");	
			}else{
				RequestContext.getCurrentInstance().execute("PF('editingDialog').show();");	
			}
		}
	}
    
	public void acceptEvent(ActionEvent actionEvent) {
		if(null!=existingAppointment && null!=existingAppointment.getId()){
		
			
			try{
				getServiceBookingDAO().updateStatus("confirmed",existingAppointment.getServiceBooking().getServiceBookingId());
				existingAppointment.getServiceBooking().setStatus("confirmed");
				existingAppointment.setTitle(getEventTitle(existingAppointment.getServiceBooking()));
				eventModel.updateEvent(existingAppointment);
				EmailUtil.notifyUser(existingAppointment.getServiceBooking());
				
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	
	public void rejectEvent(ActionEvent actionEvent) {
		if(null!=existingAppointment && null!=existingAppointment.getId()){
			if((existingAppointment.getStartDate().getTime()-new Date().getTime())/(1000*60*60)<3){
				WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.booking.notavailable"), WebUtil.getMessage("message.booking.notavailable.detail")));
				return;
			}
			
			try{
				getServiceBookingDAO().updateStatus("Rejected",existingAppointment.getServiceBooking().getServiceBookingId());
				existingAppointment.getServiceBooking().setStatus("Rejected");
				existingAppointment.setTitle(getEventTitle(existingAppointment.getServiceBooking()));
				eventModel.updateEvent(existingAppointment);
				EmailUtil.notifyUser(existingAppointment.getServiceBooking());
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	
public void addServUser(ActionEvent actionEvent) {
		

		
		
		ServiceBooking booking = createServiceBooking();
		
		if (null==booking || StringUtils.isBlank(getServUser().getUserName()) || getServUser().getUserName().length()<4 ){
			log.error("Failed to create ServiceBooking");
			return;
		}
		String email=getServUser().getEmail();
		String phone=getServUser().getPhone();
		User user=null;
		if(!StringUtils.isBlank(email)){
			email.trim();
			try{
				user=getUserDAO().getUser(email);
			}catch(Exception ex){
				log.error(ex);
			}
			
		}
		
		if(user==null && !StringUtils.isBlank(phone)){
			phone.trim();
			try{
				user=getUserDAO().findUserByPhone(phone);
			}catch(Exception ex){
				log.error(ex);
			}
			
		}
		
		
		
		if(user!=null){
			newAppointment.setStyleClass("otherUser");
			newAppointment.setTitle(user.getUserName());
			booking.setUserId(user.getUserId());
			booking.setServiceName(getServ().getServiceName());
			booking.setStartTime(newAppointment.getStartDate());
			booking.setEndTime(newAppointment.getEndDate());
			booking.setUserName(user.getUserName());
			booking.setEmail(user.getEmail());
			booking.setPhone(user.getPhone());
			booking.setStatus("booked");
			booking.setProviderId(getServ().getProvider());
			booking.setProvider(getServ().getProviderUser().getUserName());
		}else{
			newAppointment.setStyleClass("ownEvent");
			newAppointment.setTitle(getServUser().getUserName());
			booking.setUserId(userId);
			booking.setServiceName(getServ().getServiceName());
			booking.setStartTime(newAppointment.getStartDate());
			booking.setEndTime(newAppointment.getEndDate());
			booking.setUserName(getServUser().getUserName());
			booking.setEmail(getServUser().getEmail());
			booking.setPhone(getServUser().getPhone());
			booking.setStatus("booked");
			booking.setProviderId(getServ().getProvider());
			booking.setProvider(getServ().getProviderUser().getUserName());
		}		
		
		if(newAppointment.getId() == null){
			eventModel.addEvent(newAppointment);
	
			getServiceBookingDAO().addServiceBooking(booking);
			//newAppointment.setTitle(userId);
			newAppointment.setServiceBooking(booking);
			if(!booking.getUserId().equals(userId))
				EmailUtil.notifyUser(booking);
		
	
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
		if (null!=booking){

			if(booking.getStatus()!=null){
				return booking.getUserName()+ "  "+booking.getStatus();
			}else{
				return booking.getUserName();
			}

		}
		
		return null;
	}
	
	public void chooseUsername(){
		try{
			getServiceBookingDAO().updateUser(servUser);
		}catch(Exception ex){
			log.error(ex);
		}
		
	}
	
	
	public void onDateSelect(SelectEvent selectEvent) {
		if(userId==null){
			try{
				FacesContext.getCurrentInstance().getExternalContext().redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/login.jsf");
			} catch (Exception e) {
				log.error(e.getCause(), e);
	
			}
			}
		
		Date start=(Date) selectEvent.getObject();
		Date end =(Date) selectEvent.getObject();
		if((start.getTime()-new Date().getTime())/(1000*60*60)<4){
			return;
		}
		if(start.getHours()<=1){
		

			newAppointment = new Appointment(null, "", start, end);
			RequestContext.getCurrentInstance().execute("PF('promDialog').show();");	

		}else{
			c.setTime(start);
			/*
			c.add(Calendar.HOUR, -1);
			start=c.getTime();
			*/
			int serviceTime=getServ().getMinTime();
			c.add(Calendar.MINUTE, serviceTime);
			end=c.getTime();
			newAppointment = new Appointment(null, "", start, end);
			RequestContext.getCurrentInstance().execute("PF('addingDialog').show();");	
		}
	
	}
	
	public void addPromotion(ActionEvent actionEvent) {

		ServicePromotion promotion=new ServicePromotion();
		promotion.setServiceId(getServ().getServiceId());
		promotion.setPromotion(newAppointment.getNotes());
		promotion.setEffectiveDate(newAppointment.getStartDate());
		
			try{
				getServiceDAO().addServicePromotion(promotion);
				Appointment appt = new Appointment(promotion.getPromotion(), promotion.getEffectiveDate(),promotion.getEffectiveDate(),true);
				appt.setStyleClass("promotion");
				eventModel.addEvent(appt);
				
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		
	}
	
	
	public void cancelEvent(ActionEvent actionEvent) {
		if(null!=existingAppointment && null!=existingAppointment.getId()){
			if((existingAppointment.getServiceBooking().getStartTime().getTime()-new Date().getTime())/(1000*60*60)<3){
				WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.booking.notavailable"), WebUtil.getMessage("message.booking.notavailable.detail")));
				return;
			}
			
			eventModel.deleteEvent(existingAppointment);
			
			try{
				getServiceBookingDAO().deleteServiceBooking(existingAppointment.getServiceBooking().getServiceBookingId());
				  Runnable r = new Runnable()
		             {
		                @Override
		                public void run()
		                {
		                	EmailUtil.notifyUser(existingAppointment.getServiceBooking());
		                }
		             };
		             new Thread(r).start();
			
				
				
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}	
	
}
