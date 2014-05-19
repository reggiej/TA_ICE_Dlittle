package com.demo.app.dao.db;

import java.util.List;

import com.demo.app.domain.UserSearchCriteria;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;


/**
 * @author Kunta L.
 *
 */

public interface SiteAdminDAO {
	
	public Integer createUser(User user);
	
	public Integer isUsernameUnique(String username);
	
	public Integer updateUser(User user);
	
	public User getUser(User user);
	
	public List<User> getAllUsers();
	
	public void createRole(Roles role);
	
	public void updateRole(Roles role);
	
	public void removeRole(Roles role);
	
	public Integer assignRoles(User user, List<Roles> roles);
	
	public List<Roles> getAllRoles();
	
	public List<Roles> getAssignedRolesForUser(User user);

	public UserSearchCriteria getSelUser(UserSearchCriteria userSearchCriteria);
}