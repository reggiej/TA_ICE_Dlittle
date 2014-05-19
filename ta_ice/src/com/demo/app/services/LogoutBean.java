package com.demo.app.services;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.springframework.security.ui.AbstractProcessingFilter;

import com.demo.app.util.ThreatConstants;
/**
 * 
 * @author Kunta L.
 *
 */
public class LogoutBean { 

    /**
     * default constructor
     */
    public LogoutBean() {
        Exception ex = (Exception) FacesContext
        .getCurrentInstance()
        .getExternalContext()
        .getSessionMap()
        .get(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);

        if (ex != null)
        	FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                    .getMessage(), ex.getMessage()));
    }
    
	public String getBeanName() {
		return ThreatConstants.BEAN_LOGOUT_SECURITY;
	}
	
    public String help() {
    	return "help";
    }
    
    public String login() {
    	return "login";
    }

    /**
     * Method that is backed to a submit button of a form.
     */
    public String send() {
        return ("success");
    }

    public void logout(ActionEvent e) throws java.io.IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect("/iceDemo/j_spring_security_logout");
    }    
}
