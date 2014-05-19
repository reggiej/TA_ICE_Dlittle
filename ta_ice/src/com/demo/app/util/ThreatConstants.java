/**
 * 
 */
package com.demo.app.util;

import java.util.ResourceBundle;


/**
 * Constants used throughout the Threat
 *	@author Kunta L.
 */

public interface ThreatConstants {
	
	
	
	static final public String THREAT_MANAGER_ROLE_NAME = "ROLE_MANAGER";
		
	static final public String THREAT_ADMIN_ROLE_NAME = "ROLE_ADMIN";
	
	static final public String THREAT_USER_ROLE_NAME = "ROLE_URLACCESS";
	

	static final public String RESOURCE_BUNDLE_LOCATION ="threat";
		
	static final public ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(RESOURCE_BUNDLE_LOCATION);
	
    // Security
	
    static public final int SECURITY_STRENGTH = 256;
	
	static public final String SECURITY_ADMIN_FIRSTNAME = "Sys";
	
	static public final String SECURITY_ADMIN_LASTNAME = "Admin";
	
	static public final String SECURITY_ADMIN_USERNAME = "admin";

	static final public String BEAN_LOGIN_SECURITY = "loginBean";
	
	static final public String BEAN_ADDRESS_SECURITY = "InsertAddress";
	
	static final public String BEAN_LOGOUT_SECURITY = "logoutBean";
	
	static final public String BEAN_SITE_SECURITY = "siteAdmin";
	
	static final public String BEAN_SITE_CALENDAR = "calendarController";
	
	static final public String BEAN_RESET_USER_PASSWORD_SECURITY = "resetUserPassword";
	
	static final public String BEAN_VIEW_ROLES_SECURITY = "viewRoles";
	
	static final public String BEAN_VIEW_INBOX = "viewInbox";
	
	static final public String BEAN_CREATE_REQUEST_INBOX = "createRequest";
	
	static final public String SERVICE_SITEADMIN_REFERENCE = "siteAdminService";

	static final public String SERVICE_ADDRESS_REFERENCE = "addressService";

	
	static final public String SERVICE_SITECAL_REFERENCE = "calendarViewService";
	
	static final public String RTA_MANAGER_ROLE_NAME = "ROLE_MANAGER";
		
	static final public String RTA_USER_ROLE_NAME = "ROLE_URLACCESS";
	
	static final public String RTA_ADMIN_ROLE_NAME = "ROLE_ADMIN";
		
	static final public String BEAN_CREATE_USER_SECURITY = "createUser";
	
	static final public String BEAN_CREATE_ADDRESS_SECURITY = "createAddress";

	
	static final public String BEAN_CREATE_ADMIN_SECURITY = "createAdmin";
	
	static final public String BEAN_VIEW_USERS_SECURITY = "viewUsers";
	
	static final public String ADMIN_FIRSTNAME = "Sys";
	
	static final public String ADMIN_LASTNAME = "Admin";
	
	static final public String ADMIN_USERNAME = "Admin";

	public static final String NOT_AVAILABLE = null;

	public static final String THREAT_SCHEMA_NAME = "JACK";


	

	
}
