package com.demo.app.web.security;



import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.springframework.security.ui.AbstractProcessingFilter;

import com.demo.app.util.ThreatConstants;
//import com.demo.app.web.BaseUIBean;

/**
 * 
 * @author Kunta L.
 *
 */
public class InsertAddress  {
	
	// properties
    private String address;

    private String city;
   
    private String state;

    private String zip;

    
    /**
     * default constructor
     */
    public InsertAddress()  {
    	
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
		return ThreatConstants.BEAN_ADDRESS_SECURITY;
	}



    public String getAddress() {
    	
		return address;	
		
	}
    
	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public void insert(ActionEvent e) throws java.io.IOException {
	        
        FacesContext.getCurrentInstance().getExternalContext().redirect("/ta_ice/j_spring_security_check?j_address=" + address + "&j_city=" + city);  
        System.out.println(address);
        System.out.println(city);
        System.out.println(state);
        System.out.println(zip);
	
	}
}

