package com.demo.app.web.security;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.springframework.security.AccessDeniedException;
//import org.springframework.test.context.ContextConfiguration;

import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;
import com.demo.app.web.SelectableRow;


import com.icesoft.faces.component.ext.RowSelectorEvent;
/**
 * @author Kunta L.
 *
 */
//@ContextConfiguration
public class ViewUsers extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(ViewUsers.class.getName());
	
	private SiteAdminService service;
	
	private List<User> currentUsers;
	
	private User selectedUser;
	
	private List<SelectableRow<User>> userSearchResult;
	
    private User[] userInventory;
    
    private String userLookup;
    
    private List<Roles> roles;
    
    private List<Roles> assignedRolesList;
    
    private List<String> assignedRoles;
    
    private List<SelectItem> allRoles;
    
    private List<SelectItem> rolesSelectItems;
    
    private List<SelectItem> radioSelectItems;
    
    private UIComponent selectManyChkBox;
    
    private Boolean modifyFlag;
    
    private Boolean resetPassword;
    
    private Boolean updateRoles;
    
    private List<String> assignedRolesValues;
    
    private String newPassword;
    
    private String confirmPassword;
    
    private String role;

    public ViewUsers() {
    	init();
    }
    
    public void init() {
    	newPassword = confirmPassword = "";
    	resetPassword = updateRoles = Boolean.FALSE;
        updateRoles = Boolean.FALSE;
    	modifyFlag = Boolean.FALSE;
    	
    	service = (SiteAdminService) getService(ThreatConstants.SERVICE_SITEADMIN_REFERENCE);
    	
    	currentUsers = service.getAllUsers();
    	userInventory = (User[]) currentUsers.toArray(new User[currentUsers.size()]);
    	roles = service.getAllRoles();
    	allRoles = getAllRolesSelectItems();
    	assignedRoles = new ArrayList<String>();    	
    	userSearchResult = transformToSelectableRow(userInventory);
    }
    
    private void refresh() {
		resetPassword = updateRoles = Boolean.FALSE;
    	modifyFlag = Boolean.FALSE;
    	
		assignedRoles.clear();
		selectedUser = new User();	
		currentUsers = new ArrayList<User>();
		currentUsers = service.getAllUsers();
    	userInventory = (User[]) currentUsers.toArray(new User[currentUsers.size()]);
    	roles = service.getAllRoles();
    	allRoles = getAllRolesSelectItems();
    	assignedRoles = new ArrayList<String>();    	
    	userSearchResult = transformToSelectableRow(userInventory);
	}
    
    public void handleRowSelection(RowSelectorEvent event) {
    	log.info("row selected! row selected is " + event.getRow());
    	
    	selectedUser = new User();
    	assignedRoles.clear();
    	if (userSearchResult != null) {
    		modifyFlag = Boolean.TRUE;
    		for(SelectableRow<?> sr : userSearchResult) {
    			if(sr.isSelected()) {
    				selectedUser = (User) sr.getValue();
    				sr.setSelected(Boolean.FALSE);
    			}
    		}
    		assignedRolesList = service.getRolesForSelectedUser(selectedUser);
    		if(assignedRoles.isEmpty()){
    			for(Roles role : assignedRolesList) {
    				assignedRoles.add(role.getRoleName());
    			}
    		}
    	}
    	else {
    		modifyFlag = Boolean.FALSE;
    	}		
	}

    public String getBeanName() {
		return ThreatConstants.BEAN_VIEW_USERS_SECURITY;
	}
    

    
    public void roleChanged(ValueChangeEvent event) {
    	log.info("Old Role Value: " + event.getOldValue());
    	log.info("New Role Value: " + event.getNewValue());
    	selectManyChkBox = event.getComponent();
    	updateRoles = Boolean.TRUE;
    }
    
    public void passwordReset(ValueChangeEvent event) {
    	log.info("Old Password Value: " + event.getOldValue());
    	log.info("New Password Value: " + event.getNewValue());
    	confirmPassword = (String) event.getNewValue();
    	if( !(newPassword.equals(confirmPassword)) ) {
			super.addErrorMessage("Passwords do not match.");
		}
		else {
			selectedUser.setPassword(newPassword);
			resetPassword = Boolean.TRUE;
			log.info("Passwords match.");
		}
    }
    
    
	/**
	 * 
	 * 
	 */
	public String updateUser() {
		log.info("updateUser");
		try {
			
			if(resetPassword.equals(Boolean.TRUE)) {
				log.info("...updating ChangePassword");
				selectedUser = service.updatePassword(selectedUser);
	        	resetPassword = Boolean.FALSE;
			}
			if(updateRoles.equals(Boolean.TRUE)) {
	        	log.info("...updating roles");
	        	List<Roles> rolesObjectList = new ArrayList<Roles>();
	        	rolesObjectList.add(new Roles(ThreatConstants.RTA_USER_ROLE_NAME));
	        	for(String role : assignedRoles) {
	        		rolesObjectList.add(new Roles(role));
	        	}
	        	service.assignRolesForUser(selectedUser, rolesObjectList);
	        	updateRoles = Boolean.FALSE;
	        }

	        service.updateUser(selectedUser);
			super.addInfoMessage("User account modified.");
		} catch(AccessDeniedException e) {
			super.addErrorMessage(e.getMessage());
		} 
	    resetValues();
		return "next";
	}
	
	public String cancel() {
		resetValues();
		return "next";
	}
	
	public void clearValues(ActionEvent event) {
		refresh();
	}
	
	public void comparePasswords (ValueChangeEvent event) {
		if(!newPassword.equals(confirmPassword)) {
			super.addErrorMessage("Passwords do not match.");
		}
		else {
			log.info("Passwords match.");
		}
	}
	
	private void resetValues() {
		
		log.info ("Resetting value of ViewUsers");
		resetToValue (new ViewUsers());
		log.info ("Successfully reset ViewUsers object.");
	}
    
    private List<SelectItem> getAllRolesSelectItems (){
    	rolesSelectItems = new ArrayList <SelectItem> ();
		
		if (roles != null){
			log.info("rolesSelectItems is NOT null.");

			for (Roles role: roles){
				SelectItem item = new SelectItem ();
				item.setValue(role.getRoleName());
				item.setLabel(role.getRoleDescription());
				rolesSelectItems.add(item);
			}
		}
		else {
			log.info("rolesSelectItems is empty.");
			
		}
		return rolesSelectItems;
	}
    
    
    public List<SelectableRow<User>> transformToSelectableRow(User[] userInventory) {
    	
    	List<SelectableRow<User>> selectedRow = new ArrayList<SelectableRow<User>>();
    	if (userInventory != null) {
			for (User user : userInventory) {
				if (user != null) {
					SelectableRow<User> thisRow = new SelectableRow<User>(user);
					selectedRow.add(thisRow);
				}
			}
		}
    	return selectedRow;
	}
    
    public void radioChanged(ValueChangeEvent event){		
		if(event.getNewValue() instanceof String) {
			String value = (String)event.getNewValue();
			log.info("value: " + value);
		}
	}
    
	public List<SelectItem> getRadioSelectItems() {
		return radioSelectItems;
	}

	public void setRadioSelectItems(List<SelectItem> radioSelectItems) {
		this.radioSelectItems = radioSelectItems;
	}
	public List<SelectableRow<User>> getUserSearchResult() {
		return userSearchResult;
	}

	public void setUserSearchResult(List<SelectableRow<User>> userSearchResult) {
		this.userSearchResult = userSearchResult;
	}
	
	public Boolean isModifyFlag() {
		return modifyFlag;
	}

	public void setModifyFlag(Boolean modifyFlag) {
		this.modifyFlag = modifyFlag;
	}
	
	 public Boolean getModifyFlag() {
		return modifyFlag;
	}

	/**
     * Determines the sortColumnName order.
     *
     * @param   sortColumn to sortColumnName by.
     * @return  whether sortColumnName order is ascending or descending.
     */
    protected Boolean isDefaultAscending(String sortColumn) {
        return true;
    }

    public SiteAdminService getService() {
		return service;
	}
    
	public void setService(SiteAdminService service) {
		this.service = service;
	}

	public List<User> getCurrentUsers() {
		return currentUsers;
	}

	public void setCurrentUsers(List<User> currentUsers) {
		this.currentUsers = currentUsers;
	}

	public User getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(User selectedUser) {
		this.selectedUser = selectedUser;
	}

	public void setUserInventory(User[] userInventory) {
		this.userInventory = userInventory;
	}

	public List<Roles> getRoles() {
		return roles;
	}

	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}

	public List<String> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<String> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	public List<SelectItem> getRolesSelectItems() {
		return rolesSelectItems;
	}

	public void setRolesSelectItems(List<SelectItem> rolesSelectItems) {
		this.rolesSelectItems = rolesSelectItems;
	}

	/**
	 * @return the availableRoles
	 */
	public List<SelectItem> getAllRoles() {
		return allRoles;
	}

	/**
	 * @param availableRoles the availableRoles to set
	 */
	public void setAllRoles(List<SelectItem> allRoles) {
		this.allRoles = allRoles;
	}

	public List<Roles> getAssignedRolesList() {
		return assignedRolesList;
	}

	public void setAssignedRolesList(List<Roles> assignedRolesList) {
		this.assignedRolesList = assignedRolesList;
	}

	public List<String> getAssignedRolesValues() {
		return assignedRolesValues;
	}

	public void setAssignedRolesValues(List<String> assignedRolesValues) {
		this.assignedRolesValues = assignedRolesValues;
	}

	public Boolean getResetPassword() {
		return resetPassword;
	}

	public void setResetPassword(Boolean resetPassword) {
		this.resetPassword = resetPassword;
	}

	public Boolean getUpdateRoles() {
		return updateRoles;
	}

	public void setUpdateRoles(Boolean updateRoles) {
		this.updateRoles = updateRoles;
	}

	public User[] getUserInventory() {
		return userInventory;
	}

	public UIComponent getSelectManyChkBox() {
		return selectManyChkBox;
	}

	public void setSelectManyChkBox(UIComponent selectManyChkBox) {
		this.selectManyChkBox = selectManyChkBox;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	
}
