package com.nuexpert.schd.web;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.vo.Service;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ServiceController implements Serializable{
	private static final Logger log = Logger.getLogger(ServiceController.class);
	
	@ManagedProperty("#{serviceDAO}")
	private ServiceDAO serviceDAO;
	


	private String keywords;
	private List<Service> searchResults;	
	
	private List<String> categories;	
	private List<String> cities;
	
	public List<String> getCities() {
		return cities;
	}

	public void setCities(List<String> cities) {
		this.cities = cities;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	
	private Set<String> servicenames;

	public Set<String> getServicenames() {
		return servicenames;
	}

	public void setServicenames(Set<String> servicenames) {
		this.servicenames = servicenames;
	}

	private String userId=null;
	private ExternalContext externalContext;

	private Service serv;
	private String serviceId;
	
	private String city;
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	private String category;
	private String servicename;
	
    public String getServicename() {
		return servicename;
	}

	public void setServicename(String servicename) {
		this.servicename = servicename;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Service getServ() {
		return serv;
	}

	public void setServ(Service serv) {
		this.serv = serv;
	}

	@PostConstruct
	public void init() {
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = context.getViewRoot().getLocale();
		externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) 
				context.getExternalContext().getRequest();
		
		userId = request.getRemoteUser(); 
		serv=new Service();
		serv.setMinTime(45);
		serv.setIncrementTime(15);
		serv.setMaxTime(120);
		serv.setStartTime("09:00");
		serv.setEndTime("18:00");

		
		try {
			categories=serviceDAO.getAllCategories(locale.getLanguage());
			cities=serviceDAO.getCities(locale.getLanguage());
			if(userId!=null){
			List<Service> result = serviceDAO.getUserServices(userId);
			
			if (null!=result){
				searchResults = Collections.unmodifiableList(result);
			}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
    }
	

	
	public void addService(){
		//Service serv = new Service();
		serv.setProvider(userId);
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = context.getViewRoot().getLocale();
		serv.setLanguage(locale.getLanguage());
		serv.setServiceId(Long.toString(System.currentTimeMillis()));
		
		try {
			serviceDAO.addService(serv);
			externalContext.redirect("management.jsf");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	

	
	public List<Service> getServices(){
		
		try {
			return serviceDAO.getServices(userId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}

	public ServiceDAO getServiceDAO() {
		return serviceDAO;
	}

	public void setServiceDAO(ServiceDAO serviceDAO) {
		this.serviceDAO = serviceDAO;
	}

	public void searchServices(){
		try {
			//log.debug("searchServices with param (" + userId + "," + keywords + ")");
			
			List<Service> result = serviceDAO.searchServices(userId, keywords);
			
			if (null!=result){
				searchResults = Collections.unmodifiableList(result);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}
	
	public void searchCategory(){
		try {
			log.debug("searchCategory with param (" + category+ ")");
			
			List<Service> result = serviceDAO.searchCategory(category);
			
			if (null!=result){
				searchResults = Collections.unmodifiableList(result);
			}
			
			servicenames=new HashSet<String>();
			for(int i=0;i<searchResults.size();i++){
				servicenames.add(searchResults.get(i).getServiceName());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}
	
	public void searchCityCategory(){
		try {
			log.debug("searchCityCategory with param (" +city +","+ category+ ")");
			categories=serviceDAO.getCityCategories(city);
			List<Service> result = serviceDAO.searchCityCategory(city,category);
			
			if (null!=result){
				searchResults = Collections.unmodifiableList(result);
			}else{
				return;
			}
			
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}
	
	public void searchServicename(){
		try {
			log.debug("searchServicename with param (" + servicename+ ")");
			
			List<Service> result = serviceDAO.searchServicename(category,servicename);
			
			if (null!=result){
				searchResults = Collections.unmodifiableList(result);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}

	
	
	public List<Service> getHealthPromotion(){
		
		try {
			return serviceDAO.servicePromotion("health");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	public List<Service> getAutoPromotion(){
		
		try {
			return serviceDAO.servicePromotion("auto");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public List<Service> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<Service> searchResults) {
		this.searchResults = searchResults;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}


	
}
