package com.nuexpert.schd.web;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;

import com.nuexpert.schd.db.HolidayDAO;
import com.nuexpert.schd.db.ServiceBookingDAO;
import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.vo.Appointment;
import com.nuexpert.schd.vo.Service;
import com.nuexpert.schd.vo.ServiceBooking;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ServiceView implements Serializable {
	protected static final Logger log = Logger.getLogger(ServiceView.class);
	@ManagedProperty("#{serviceDAO}")
	private ServiceDAO serviceDAO;
	
	private String serviceId;
	private Service serv;
	
	String urlPath;
	
	private ExternalContext context;
	
    public Service getServ() {
		return serv;
	}

	public void setServ(Service serv) {
		this.serv = serv;
	}

	@PostConstruct
	public void init() {
		
		try{
			context=FacesContext.getCurrentInstance().getExternalContext();
			urlPath=context.getRequestContextPath();
			serviceId = context.getRequestParameterMap().get("serviceId");
			serv=serviceDAO.getService(serviceId);

						
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}		
	}
    
	public void updateService(){

		
		try {
			serviceDAO.updateService(serv);
			String url=urlPath+"/provider/management.jsf";
		
			//System.out.println("URL===="+url);
			context.redirect(url);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void deleteService(){

		
		try {
			serviceDAO.deleteService(serv.getServiceId());
			String url=urlPath+"/provider/management.jsf";
			
			//System.out.println("URL="+url);
			context.redirect(url);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public ServiceDAO getServiceDAO() {
		return serviceDAO;
	}

	public void setServiceDAO(ServiceDAO serviceDAO) {
		this.serviceDAO = serviceDAO;
	}


}
