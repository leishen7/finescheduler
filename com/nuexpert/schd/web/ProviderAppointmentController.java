package com.nuexpert.schd.web;

import static com.nuexpert.schd.util.Status.CONFIRMED;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;

import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.util.Status;
import com.nuexpert.schd.util.WebUtil;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.ServiceBooking;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ProviderAppointmentController extends AbstractScheduleController{
	
	@ManagedProperty("#{serviceBookingDAO}")
	private ServiceBookingDAO serviceBookingDAO;	
	

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
									appt.setStyleClass("ownEvent");
									
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
								}else if(userId.equals(booking.getUserId())){
									appt.setStyleClass("otherService");
								}else{
									appt.setStyleClass("ownEvent");
									
								}
							
							
							eventModel.addEvent(appt);
						
					}
				}			
			}
								
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
				getServiceBookingDAO().updateStatus(Status.CONFIRMED,existingAppointment.getServiceBooking().getServiceBookingId());
				existingAppointment.getServiceBooking().setStatus(Status.CONFIRMED);
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
				getServiceBookingDAO().updateStatus(Status.REJECTED,existingAppointment.getServiceBooking().getServiceBookingId());
				existingAppointment.getServiceBooking().setStatus(Status.REJECTED);
				existingAppointment.setTitle(getEventTitle(existingAppointment.getServiceBooking()));
				eventModel.updateEvent(existingAppointment);
				EmailUtil.notifyUser(existingAppointment.getServiceBooking());
				
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
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
				return booking.getUserName();
	
		}
		
		return null;
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
