package com.demo.app.services;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;


import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;



/*
 * Handles all security features on home.jspx
 */
public class SiteAdmin extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(SiteAdmin.class.getName());
	
	private User currentUser;
	
	private String currentUserRoles;
	
	private SiteAdminService service;
	
	private String userId;
	
	private Boolean passwordAge;
	
	private String currentPassword;
	
	private String newPassword;
	
	private String confirmPassword;
	
	private final int MILLISECONDS_PER_DAY = 86400000;
	
	 /**
     * default empty constructor
     */
    public SiteAdmin() {
    	init();
    }
    
    public void init() {
    	currentPassword = newPassword = confirmPassword = "";
   		service = (SiteAdminService)getService(
   				ThreatConstants.SERVICE_SITEADMIN_REFERENCE);
		currentUser = service.getCurrentUser();
		getCurrentUserRoles(currentUser);  
   		if( (currentUser.isPasswordExpired()) || (checkPasswordAge(currentUser)) ) {
   			passwordAge = Boolean.TRUE;
   		}
		
    }
    
   // int inta = Integer.parseInt(currentUser.getUserID());
    
    public Boolean checkPasswordAge(User user) {
		Boolean rtnValue = Boolean.FALSE;

		// Get msec from each, and subtract.
	    long diff = new Date().getTime() - user.getPasswordChangedOnDate().getTime();
	    
	    // convert millisecs to days
	    long passwordAgeInDays = diff / MILLISECONDS_PER_DAY;
		
	    log.info(user.getUsername() + " password age is " + passwordAgeInDays + " days old.");
		if(passwordAgeInDays > 90) {
			log.info("Time to reset your password.");
			user.setChangePassword(Boolean.TRUE);
			service.updateUser(user);
			rtnValue = Boolean.TRUE;
		} 
		else {
			log.info("Not time to reset your password.");
		}
		return rtnValue;
	}
    
    public void getCurrentUserRoles(User currentUser) {    	
    	List<Roles> currentUserRolesList = service.getRolesForSelectedUser(currentUser);
    	currentUserRoles = "(";
    	
    	for(int i = 0; i < currentUserRolesList.size(); i++) {
    		currentUserRoles = currentUserRoles.concat(currentUserRolesList.get(i).getRoleDescription());
    		log.info("Current User Role: " + currentUserRolesList.get(i).getRoleDescription());
    		if(i < (currentUserRolesList.size()-1)) {
    			currentUserRoles = currentUserRoles.concat(", ");
    		}
    	}
    	currentUserRoles = currentUserRoles.concat(")");
    	
    }
    
    public void validateNewPassword(FacesContext context, UIComponent validate, Object value) {
    	((UIInput) validate).setValid(Boolean.TRUE);
    }
    
    public void validateCurrentPassword(FacesContext context, UIComponent validate, Object value) {
    	((UIInput) validate).setValid(Boolean.TRUE);
    }
    
    
    public void validateConfirmPassword(FacesContext context, UIComponent validate, Object value) {
    	((UIInput) validate).setValid(Boolean.TRUE);
    	
    }
    

      
    public String updatePassword() {
    	currentUser.setPassword(newPassword);
		service.updatePassword(currentUser);
		log.info("after password reset. now returning next");
		resetValues();
		super.addInfoMessage("User's password reset.");

    	return "next";
    }
    
	/**
	 * Reset values.
	 */
	private void resetValues() {		
		log.info ("Resetting value of SiteAdmin");
		resetToValue (new SiteAdmin());
		log.info ("Successfully reset SiteAdmin object.");
	}
    
	public String getBeanName() {
		return ThreatConstants.BEAN_SITE_SECURITY;
	}	

	public SiteAdminService getService() {
		return service;
	}

	public void setService(SiteAdminService service) {
		this.service = service;
	}

	/*
	 *	Method for returning current logged-in user.
	 *
	 */
	public User getCurrentUser() {
		return currentUser;
	}
	
    public String home() {
    	return "home";
    } 
    
    public String login() {
    	return "login";
    }
    
    public String siteAdministration() {
    	return "siteAdministration";
    }
    
    public String createUser(){
    	return "createUser";
    }
    
    public String viewUsers(){
    	return "viewUsers";
    }
    
    public String sicCode() {
    	return "sicCodeFinder";
    }
    
    public String createUserList() {
    	return "userSearchList";
    }
    
    public String viewRTA() {
    	return "viewRTA";
    }
    
    public String globalSites() {
    	return "globalSites";
    }
    
    public String localSites() {
    	return "localSites";
    }
    
    public String managerSearchList() {
    	return "managerSearchList";
    }
    
    public String preferences() {
    	return "preferences";
    }    
    
    public String help() {
    	return "help";
    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Boolean getPasswordAge() {
		return passwordAge;
	}

	public void setPasswordAge(Boolean passwordAge) {
		this.passwordAge = passwordAge;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getCurrentUserRoles() {
		return currentUserRoles;
	}

	public void setCurrentUserRoles(String currentUserRoles) {
		this.currentUserRoles = currentUserRoles;
	}

}
