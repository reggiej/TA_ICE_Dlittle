package com.demo.app.web.security;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;


/**
 * The Class CreateUser.
 * 
 * @author Kunta L.
 */

public class CreateUser extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(CreateUser.class.getName());
	
	private SiteAdminService siteAdminService;
		
	private User user;
	
	private List<Roles> roles;
	
	private List<SelectItem> allRoles;
	
    private List<String> assignedRoles;
    
    private String firstName;
    
    private String lastName;
    
    private String username;
    
    // During password reset, this holds the first password entered.
    private String password;
    
    // During password reset, this holds the confirmation password.
    private String confirmPassword;
    
    private String role;
    
    private String enabled;
    
    private Boolean passwordExpired;
    
	/**
	 * default empty constructor.
	 */
    public CreateUser() {
    	init();
    }
    
    
	/* (non-Javadoc)
	 * @see com.deloitte.asdw.web.BaseUIBean#getBeanName()
	 */
	public String getBeanName() {
		return ThreatConstants.BEAN_CREATE_USER_SECURITY;
	}
	
	/**
	 * Inits the CreateUser instance.
	 */
	private void init (){
		password = "";        
        confirmPassword = "";
        user = new User ();
		siteAdminService =(SiteAdminService)getService(ThreatConstants.SERVICE_SITEADMIN_REFERENCE);
		roles = siteAdminService.getAllRoles();
		allRoles = getAvailableRolesSelectItems();
		assignedRoles = new ArrayList<String>();
	}
	
	/**
	 * Role change listener.
	 * 
	 * @param valueChangeEvent the value change event
	 */
	public void roleChanged(ValueChangeEvent valueChangeEvent){
		log.fine("valueChangeEvent: " +  valueChangeEvent.getNewValue());
	}
	
	public void comparePasswords (ValueChangeEvent event) {
		confirmPassword = (String) event.getNewValue();
		if( !(password.equals(confirmPassword)) ) {
			super.addErrorMessage("Passwords do not match.");
		}
		else {
			user.setPassword(password);
			log.fine("Passwords match.");
		}
	}
	
	/**
	 * Unique username listener.
	 * 
	 * @param event the event
	 */
	public void uniqueUsernameListener (ValueChangeEvent event){
		log.fine("Old Value: " + (String) event.getOldValue());
		log.fine("New Value: " + (String) event.getNewValue());
		
		if ((event.getNewValue() != null) || (event.getNewValue() != "")) {
			if (siteAdminService.isUsernameUnique((String) event.getNewValue()) ) {
				log.fine("username is unique.");
			}
			else {
				log.fine("username already in use.");
				addErrorMessage("username already in use.");
			}
		}
		else {
			super.addErrorMessage("username cannot be null.");
		}
	}
	
	/**
	 * Creates the new account.
	 * 
	 * @return string
	 */
	public String createNewAccount () {	
		if ((siteAdminService != null) & ((user != null) | (assignedRoles != null))){
			List<Roles> rolesList = new ArrayList<Roles>();
			for(String role: assignedRoles) {
				rolesList.add(new Roles(role));
			}
			rolesList.add(new Roles(ThreatConstants.RTA_USER_ROLE_NAME));
			
			siteAdminService.createUser(user, rolesList);
			log.fine("after reset. now returning next");
			resetValues();
			super.addInfoMessage("User account created.");
			return "next";
		}
		else {
			throw new WebSecurityException(" createNewAccount() values cannot be null");
		}
	}

	/**
	 * Gets the available roles select items.
	 * 
	 * @return the available roles select items
	 */
	private List <SelectItem> getAvailableRolesSelectItems (){
		List <SelectItem> items = new ArrayList <SelectItem> ();
		if (roles != null){
			log.fine("roles is NOT null.");			
			for (Roles role: roles){
				SelectItem item = new SelectItem ();
				item.setValue(role.getRoleName());
				item.setLabel(role.getRoleDescription());
				items.add(item);
			}
		}
		else {
			log.fine("roles is null.");
		}
		return items;
	}
	
	/**
	 * Cancel.
	 * 
	 * @return the string
	 */
	public String cancel() {
		resetValues();
		return "next";
	}
	
	/**
	 * Clear values.
	 * 
	 * @return the string
	 */
	public String clearValues() {
		resetValues();
		return "createUser";
	}
	
	/**
	 * Refresh.
	 */
	private void refresh() {
		user = new User ();
		assignedRoles = new ArrayList<String>(); 
		roles = siteAdminService.getAllRoles();
		allRoles = getAvailableRolesSelectItems();
	}
	
	/**
	 * Reset values.
	 **/
	private void resetValues() {		
		log.fine ("Resetting value of CreateUser");
		resetToValue (new CreateUser());
		log.fine ("Successfully reset CreateUser object.");
	}
	
	public List<String> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<String> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	public SiteAdminService getService() {
		return siteAdminService;
	}

	/**
	 * Sets the service.
	 * 
	 * @param service the new service
	 */
	public void setService(SiteAdminService service) {
		this.siteAdminService = service;
	}
	
	/**
	 * Gets the all roles.
	 * 
	 * @return the availableRoles
	 */
	public List<SelectItem> getAllRoles() {
		return allRoles;
	}

	/**
	 * Sets the all roles.
	 * 
	 * @param allRoles the all roles
	 */
	public void setAllRoles(List<SelectItem> allRoles) {
		this.allRoles = allRoles;
	}

	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Gets the roles.
	 * 
	 * @return the roles
	 */
	public List<Roles> getRoles() {
		return roles;
	}

	/**
	 * Sets the roles.
	 * 
	 * @param roles the new roles
	 */
	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}
	
	/**
	 * Available roles.
	 * 
	 * @param event the event
	 */
	public void availableRoles (ValueChangeEvent event){
	}

	/**
	 * Gets the site admin service.
	 * 
	 * @return the site admin service
	 */
	public SiteAdminService getSiteAdminService() {
		return siteAdminService;
	}

	/**
	 * Sets the site admin service.
	 * 
	 * @param siteAdminService the new site admin service
	 */
	public void setSiteAdminService(SiteAdminService siteAdminService) {
		this.siteAdminService = siteAdminService;
	}

	/**
	 * Gets the first name.
	 * 
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name.
	 * 
	 * @param firstName the new first name
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the last name.
	 * 
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name.
	 * 
	 * @param lastName the new last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 * 
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getConfirmPassword() {
		return confirmPassword;
	}


	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}


	/**
	 * Gets the role.
	 * 
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role.
	 * 
	 * @param role the new role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Gets the enabled.
	 * 
	 * @return the enabled
	 */
	public String getEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 * 
	 * @param enabled the new enabled
	 */
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}


	/**
	 * Gets the password expired.
	 * 
	 * @return the password expired
	 */
	public Boolean getPasswordExpired() {
		return passwordExpired;
	}


	/**
	 * Sets the password expired.
	 * 
	 * @param passwordExpired the new password expired
	 */
	public void setPasswordExpired(Boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}
	
	
}
