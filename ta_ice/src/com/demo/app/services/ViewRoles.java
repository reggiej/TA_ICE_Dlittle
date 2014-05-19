package com.demo.app.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.swing.tree.DefaultTreeModel;

import com.demo.app.domain.security.Roles;
import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;
import com.demo.app.web.security.WebSecurityException;
/**
 * @author Kunta L.
 *
 */
public class ViewRoles extends BaseUIBean {

	private static Logger log = Logger.getLogger(ViewRoles.class.getName());
	
	private SiteAdminService service;
	
	private List<Roles> roles;
	
	private Roles role;
	
	private List<Roles> availableRoles;
	
	private List<Roles> assignedRoles;
	
	private DefaultTreeModel model;
	
    /**
     * default empty constructor
     */
    public ViewRoles() {
    	init();

    }
    
	public String getBeanName() {
		return ThreatConstants.BEAN_VIEW_ROLES_SECURITY;
	}
	
	private void init (){
		role = new Roles ();
		service = (SiteAdminService)getService(ThreatConstants.SERVICE_SITEADMIN_REFERENCE);
		roles = service.getAllRoles();
		availableRoles = getAvailableRolesSelectItems();
	}
	
	/**
	 * @return the availableRoles
	 */
	public List<Roles> getAvailableRoles() {
		return availableRoles;
	}

	/**
	 * @param availableRoles the availableRoles to set
	 */
	public void setAvailableRoles(List<Roles> availableRoles) {
		this.availableRoles = availableRoles;
	}

	public List<Roles> getRoles() {
		return roles;
	}

	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}

	public void availableRoles (ValueChangeEvent event){
	}
	
	
	/**
	 * 
	 * @param event
	 */
	public void createNewRole (ActionEvent event){	

		if ( (service != null) & (role != null) ){
			service.createRole(role);
		}
		else {
			throw new WebSecurityException(" createNewRole Parameter user cannot be null");
		}
	}

	private List <Roles> getAvailableRolesSelectItems (){
		List <Roles> items = new ArrayList <Roles> ();
		if (roles != null){
			log.fine("roles is NOT null.");
			
			for (Roles role: roles){
				log.fine("Role Description: " + role.getRoleDescription());
				log.fine("Role ID: " + role.getRoleID());
				log.fine("Role Name: " + role.getRoleName());
			}
			
		}
		else {
			log.fine("roles is null.");
		}
		return items;
	}
	
    /**
     * Gets the tree's default model.
     *
     * @return tree model.
     */
    public DefaultTreeModel getModel() {
        return model;
    }

	public List<Roles> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<Roles> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	public SiteAdminService getService() {
		return service;
	}

	public void setService(SiteAdminService service) {
		this.service = service;
	}
 
    
}
