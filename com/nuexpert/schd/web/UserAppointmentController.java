package com.nuexpert.schd.web;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;

import com.nuexpert.schd.db.HolidayDAO;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;
import static com.nuexpert.schd.util.Status.*;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class UserAppointmentController extends AbstractScheduleController{
	
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
						
						if (!booking.getStartTime().equals(booking.getEndTime())){
							
							if (StringUtils.equalsIgnoreCase(booking.getUserId(), userId)){
								appt = new Appointment(booking, getEventTitle(booking), booking.getStartTime(), booking.getEndTime());	
								appt.setNotes(booking.getNotes());
								if((booking.getStartTime().getTime()-new Date().getTime())/(1000*60*60)<4){
									appt.setStyleClass("pastEvent");
								}else if(booking.getStatus().equals(CONFIRMED)){
									appt.setStyleClass("confirmedEvent");
								}else if(getServ().getServiceId().equals(booking.getServiceId()))
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
								appt.setNotes(booking.getNotes());
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
}
