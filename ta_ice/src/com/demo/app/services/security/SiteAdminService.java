package com.demo.app.services.security;

import java.util.List;



import com.demo.app.domain.UserSearchCriteria;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;


public interface SiteAdminService {
	

	/*@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public void createNewAdmin(String username, String password, String role);
	
	/*@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public Integer createUser(User user, List<Roles> role);
	
	/*@Secured("ROLE_URLACCESS")*/
	public Integer updateUser(User user);
	
	/*@Secured("ROLE_URLACCESS")*/
	public User updatePassword(User user);
	
	/*@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public void deleteUser(User user);
	
	/*@Secured({"ROLE_URLACCESS","ROLE_ADMIN","ROLE_MANAGER","ROLE_USER"})*/
	public Boolean isUsernameUnique(String username);
	
/*	@Secured("ROLE_URLACCESS")
*/	public User getCurrentUser();
	
/*	@Secured({"ROLE_URLACCESS","ROLE_ADMIN","ROLE_MANAGER","ROLE_USER"})*/
	public List<User> getAllUsers();	
	
	
/*	@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public void createRole(Roles role);
	
/*	@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public void updateRole(Roles role);
	
/*	@Secured({"ROLE_ADMIN","ROLE_MANAGER"})*/
	public void deleteRole(Roles role);
	
	public List<Roles> getRolesForSelectedUser(User user);
	
	public void assignRolesForUser(User user, List<Roles> rolesList);
	
	/*@Secured("ROLE_URLACCESS")*/
	public List<Roles> getAllRoles();

	public UserSearchCriteria getSpecificUser(UserSearchCriteria userSearchCriteria);	

}