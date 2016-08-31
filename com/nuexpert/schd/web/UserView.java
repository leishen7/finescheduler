package com.nuexpert.schd.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.nuexpert.schd.db.UserDAO;
import com.nuexpert.schd.util.EmailUtil;
import com.nuexpert.schd.util.WebUtil;
import com.nuexpert.schd.vo.User;

@ManagedBean
@RequestScoped
public class UserView implements Serializable{

	private User user;
	
	@ManagedProperty("#{userDAO}")
	private UserDAO userDAO;
	
	@ManagedProperty("#{loginController}")
	private LoginController loginController;
	
	
	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	private List<String> cities;
	
	private String question="";
	
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
	
	private String questionDesc="";

	public String getQuestionDesc() {
		return questionDesc;
	}

	public void setQuestionDesc(String questionDesc) {
		this.questionDesc = questionDesc;
	}

	private List<String> questions;
    
    public List<String> getQuestions() {
		return questions;
	}

	public void setQuestions(List<String> questions) {
		this.questions = questions;
	}
	
	private List<String> phoneProviders;
	
	public List<String> getPhoneProviders() {
		return phoneProviders;
	}

	public void setPhoneProviders(List<String> phoneProviders) {
		this.phoneProviders = phoneProviders;
	}
	

	@PostConstruct
    public void init() {
    	
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) 
				context.getExternalContext().getRequest();
		
		phoneProviders=new ArrayList<String>();
		phoneProviders.add("Rogers");
		phoneProviders.add("Bell");
		phoneProviders.add("Telus");
		phoneProviders.add("Fido");
		phoneProviders.add("koodo");


		
		String userId = request.getRemoteUser();  
    	user=new User();
    	
        questions=new ArrayList<String>();
        questions.add("Forget Password");
        questions.add("Service Provider");
        questions.add("Customer");
        questions.add("Technical Issue");
        questions.add("Other");

       try{
    	   cities=userDAO.getCities();
		}catch(Exception e){
			e.printStackTrace();
		}

        
        if(userId !=null){
        	try{
        		user = userDAO.getUser(userId);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
        }

    }





    public List<String> getCities() {
        return cities;
    }


    public void displayLocation() {
        FacesMessage msg;
        if(getUser().getCity() != null && getUser().getCountry() != null)
            msg = new FacesMessage("Selected", getUser().getCity()+ " of " + getUser().getCountry());
        else
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid", "City is not selected."); 
            
        FacesContext.getCurrentInstance().addMessage(null, msg);  
    }

	public void save() {
		try{
			userDAO.addUser(user);
			userDAO.addUserRole(user.getUserId(), "StandardUser");
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Welcome " + getUser().getFirstname() + " " + getUser().getLastname()));
			loginController.setUsername(user.getUserId());
			loginController.setPassword(user.getPassword());
			loginController.login();
			
	
		}catch(Exception e){
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.duplicated"), WebUtil.getMessage("message.notfound.detail")));
			e.printStackTrace();
		}
	}
	
	public void savep() {
		try{
			userDAO.addUser(user);
			userDAO.addUserRole(user.getUserId(), "StandardUser");
			userDAO.addUserRole(user.getUserId(), "provider");
			FacesContext context=FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest) 
					context.getExternalContext().getRequest();
			request.login(user.getUserId(), user.getPassword());
			ExternalContext ex=	context.getExternalContext();
			
			ex.redirect(ex.getRequestContextPath()+"/provider/providerappointment.jsf");

		}catch(Exception e){
			WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.duplicated"), WebUtil.getMessage("message.notfound.detail")));
			e.printStackTrace();
		}

	}
	
	public void update() {
		try{
			userDAO.updateUser(user);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Welcome " + getUser().getFirstname() + " " + getUser().getLastname()));
			ExternalContext ex=	FacesContext.getCurrentInstance().getExternalContext();
			
			ex.redirect(ex.getRequestContextPath()+"/user/userappointment.jsf");
	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void contactUs() {
		try{
			/*
			System.out.println("question="+question);
			System.out.println("email="+user.getEmail());
			System.out.println("first name="+user.getFirstname());
			System.out.println("lastname="+user.getLastname());
			System.out.println("phone="+user.getPhone());*/
			User theUser=userDAO.getUser(user.getEmail());
			if(theUser==null && "Forget Password".equals(question)){
				WebUtil.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, WebUtil.getMessage("message.notfound"), WebUtil.getMessage("message.notfound.detail")));
			}else{
				if( "Forget Password".equals(question)){
					EmailUtil.contactUs(question,questionDesc,theUser);
				}else{
					EmailUtil.contactUs(question,questionDesc,user);
				}
			}
	
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	


}

