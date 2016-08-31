package com.nuexpert.schd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import com.nuexpert.schd.db.HolidayDAO;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ProviderScheduleController extends AbstractScheduleController {

	// appointment from others
	private Appointment userAppointment = new Appointment();
	
	@ManagedProperty("#{serviceDAO}")
	private ServiceDAO serviceDAO;	
	@ManagedProperty("#{serviceBookingDAO}")
	private ServiceBookingDAO serviceBookingDAO;
	@ManagedProperty("#{holidayDAO}")
	private HolidayDAO holidayDAO;	
	
	private Map<String, Service> serviceStore = new HashMap<String, Service>();
	
    @PostConstruct
	public void init() {
		try{
			log.debug("user ==> " + userId);	
			
			super.init();
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
	}
    
	public Appointment getUserAppointment() {
		return userAppointment;
	}

	public void setUserAppointment(Appointment userAppointment) {
		this.userAppointment = userAppointment;
	}
	
	public void rejectEvent(ActionEvent actionEvent) {
		
		log.debug("userAppointment" + userAppointment);
		log.debug("userAppointment.getId()" + userAppointment.getId());
		
		if(null!=userAppointment && userAppointment.getId() != null){
			eventModel.deleteEvent(userAppointment);
			
			try{
				serviceBookingDAO.deleteServiceBooking(userAppointment.getServiceBooking().getServiceBookingId());
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void acceptEvent(ActionEvent actionEvent) {
		if(null!=userAppointment && userAppointment.getId() != null){
			try{
//				serviceBookingDAO.acceptServiceBooking(userAppointment.getServiceBooking().getServiceBookingId());
				userAppointment.setStyleClass("confirmedEvent");
				eventModel.updateEvent(userAppointment);				
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void onEventSelect(SelectEvent selectEvent) {
		Appointment appt = (Appointment) selectEvent.getObject();
//		serviceName = appt.getServiceBooking().getServiceName();
		
		if (!StringUtils.isBlank(appt.getServiceBooking().getUserId()) && StringUtils.equalsIgnoreCase(appt.getServiceBooking().getUserId(), userId)){
			existingAppointment=appt;
			RequestContext.getCurrentInstance().update("mgmtTab:scheduleForm:existingAppointment");
			RequestContext.getCurrentInstance().execute("PF('editingDialog').show();");	
			
		}else{
			existingAppointment=appt;
			RequestContext.getCurrentInstance().update("mgmtTab:scheduleForm:existingAppointment");
			RequestContext.getCurrentInstance().execute("PF('acceptingDialog').show();");	
		}
	}
	
	public ServiceDAO getServiceDAO() {
		return serviceDAO;
	}

	public void setServiceDAO(ServiceDAO serviceDAO) {
		this.serviceDAO = serviceDAO;
	}

	public List<String> listServiceNames(String serviceName){
		try {
			log.debug("getServices with param (" + userId + "," + serviceName + ")");
			
			List<Service> servList = serviceDAO.loadServices(userId, serviceName);

			if (null!=servList){
				List<String> servNameList = new ArrayList<String>();
				serviceStore.clear();
				
				for (Service serv: servList){
					serviceStore.put(serv.getServiceName(), serv);
					servNameList.add(serv.getServiceName());
				}
				
				return servNameList;
			}
			
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	public Service getServiceByName(String serviceName){
		return serviceStore.get(serviceName);
	}

	@Override
	public ServiceBookingDAO getServiceBookingDAO() {
		return serviceBookingDAO;
	}

	public void setServiceBookingDAO(ServiceBookingDAO serviceBookingDAO) {
		this.serviceBookingDAO = serviceBookingDAO;
	}

	public void setHolidayDAO(HolidayDAO holidayDAO) {
		this.holidayDAO = holidayDAO;
	}

	@Override
	public String getEventTitle(ServiceBooking booking) {
		if (null!=booking){
			return booking.getUserId();
		}
		
		return null;
	}

	@Override
	public ServiceBooking createServiceBooking() {
/*		Service selectedService = getServiceByName(serviceName);
		
		if (null==selectedService){
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.servicenotprovided.summary"), WebUtil.getMessage("message.servicenotprovided.detail")));
			
			log.debug("Invalid service name");
			return null;
		}
		
		ServiceBooking booking = new ServiceBooking();
		booking.setServiceId(selectedService.getServiceId());
		
		return booking;*/
		
		return null;
	}
}

