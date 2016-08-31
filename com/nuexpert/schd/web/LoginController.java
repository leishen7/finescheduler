package com.nuexpert.schd.web;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.omg.PortableInterceptor.RequestInfo;

import com.nuexpert.schd.db.ServiceDAO;
import com.nuexpert.schd.db.UserDAO;
import com.nuexpert.schd.util.WebUtil;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class LoginController implements Serializable{
	private static final Logger log = Logger.getLogger(LoginController.class);
	
	private ServiceDAO serviceDAO;
	
	private String username;
	private String password;
	private String originalURL;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@PostConstruct
    public void init() {
		serviceDAO=new ServiceDAO();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        //originalURL = (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);
        
        originalURL = (String) externalContext.getRequestHeaderMap().get("referer");

        if (originalURL == null) {
            originalURL = externalContext.getRequestContextPath() + "/index.jsf";
        } else {
            String originalQuery = (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_QUERY_STRING);

            if (originalQuery != null) {
                originalURL += "?" + originalQuery;
            }
        }
        
        //log.debug("originalURL==>" + originalURL);
    }	

	public String getOriginalURL() {
		return originalURL;
	}
	public void setOriginalURL(String originalURL) {
		this.originalURL = originalURL;
	}
	public void login(){
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
		
		try {
			if (StringUtils.isBlank(request.getRemoteUser())){
				//request.logout();
				request.login(this.username, this.password);			
				
			}else if(!request.getRemoteUser().equals(this.username)){
				request.logout();
				request.login(this.username, this.password);
			}
			
			if(originalURL!=null && originalURL.indexOf("service.jsf?")>0){
				externalContext.redirect(originalURL);
			}else {
				
				if(UserDAO.isProvider(this.username)){	
					String recentServiceId=serviceDAO.getProviderRecentServiceId(this.username);
					if(recentServiceId==null)
						externalContext.redirect(externalContext.getRequestContextPath()+"/provider/providerappointment.jsf");
					else
						externalContext.redirect(externalContext.getRequestContextPath()+"/provider/serviceappointment.jsf?serviceId="+recentServiceId);
				}else{
					String recentServiceId=serviceDAO.getUserRecentServiceId(this.username);
					if(recentServiceId==null)
						externalContext.redirect(externalContext.getRequestContextPath()+"/user/userappointment.jsf");
					else
						externalContext.redirect(externalContext.getRequestContextPath()+"/user/appointment.jsf?serviceId="+recentServiceId);
				}
			}
		} catch (Exception e) {
			//log.error(e.getCause(), e);
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("login.failed.header"), WebUtil.getMessage("login.failed.message")));
			
			//context.addMessage(null, new FacesMessage("Login failed."));
		}
		
	}

	public void logout(){
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) 
				context.getExternalContext().getRequest();
		String userId = request.getRemoteUser();
		if(userId!=null){
			try {
				request.logout();
			} catch (ServletException e) {
				log.error(e.getCause(), e);
				context.addMessage(null, new FacesMessage("Logout failed."));
			}
		}
		
		try {
			externalContext.redirect(externalContext.getRequestContextPath() + "/index.jsf");
		} catch (IOException e) {
			log.error(e.getCause(), e);
			context.addMessage(null, new FacesMessage("redirect failed."));
		}
	}
}
