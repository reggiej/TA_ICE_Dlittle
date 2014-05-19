package com.demo.app.web.security;

import java.util.logging.Logger;

import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;

/**
 * 
 * @author Kunta L.
 *
 */
public class CreateAdmin extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(CreateAdmin.class.getName());
	
	private SiteAdminService service;
	
	private final String ADMIN_USERNAME = "admin";
	
	private final String ADMIN_PASSWORD = "Admin";
	
	private final String ADMIN_ROLE = "ROLE_ADMIN";
	
	
    /**
     * default empty constructor
     */
    public CreateAdmin() {
    	init();
    }
    
    public void init() {
    	service =(SiteAdminService) getService(ThreatConstants.SERVICE_SITEADMIN_REFERENCE);
    }
    
	public String getBeanName() {
		return ThreatConstants.BEAN_CREATE_ADMIN_SECURITY;
	}
	
    public void createNewAdmin() {
    	log.fine("Calling createNewAdmin()");
    	if (service != null) {
    		service.createNewAdmin("ADMIN_USERNAME", "Admin", "ROLE_ADMIN");
    	}
    }
	
	public SiteAdminService getService() {
		return service;
	}

	public void setService(SiteAdminService service) {
		this.service = service;
	}

	public String getADMIN_USERNAME() {
		return ADMIN_USERNAME;
	}

	public String getADMIN_PASSWORD() {
		return ADMIN_PASSWORD;
	}

	public String getADMIN_ROLE() {
		return ADMIN_ROLE;
	}

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		CreateAdmin.log = log;
	}
	
	
    
}
