package com.demo.app.web.security;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.springframework.security.ui.AbstractProcessingFilter;
import com.demo.app.util.ThreatConstants;
//import com.I7212.rta.web.BaseUIBean;

/**
 * 
 * @author Kunta L.
 *
 */
public class LoginBean {
	
	// properties
    private String userId;

    private String password;
    
    /**
     * default constructor
     */
    public LoginBean()  {
    	
        Exception ex = (Exception) FacesContext
        .getCurrentInstance()
        .getExternalContext()
        .getSessionMap()
        .get(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);
        
        if (ex != null)
        	FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                    .getMessage(), ex.getMessage()));
    }

    public String getBeanName() {
		return ThreatConstants.BEAN_LOGIN_SECURITY;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void login(ActionEvent e) throws java.io.IOException {
    	FacesContext.getCurrentInstance().getExternalContext().redirect("/ta_ice/j_spring_security_check?j_username=" + userId + "&j_password=" + password);
    
    	System.out.println(userId);
    }
    
    
}

