package com.demo.app.services.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.springframework.dao.DataAccessException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import com.demo.app.dao.db.SiteAdminDAO;
import com.demo.app.domain.UserSearchCriteria;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.util.ThreatConstants;

public class SiteAdminServiceImpl implements SiteAdminService {
	
	private SiteAdminDAO siteAdminDAO;
	private static Logger log = Logger.getLogger(SiteAdminService.class.getName());
	
	/*
	 * Default Constructor
	 */
	public SiteAdminServiceImpl() {
		log.fine("Initializing SiteAdmin Service");
	}
	
	/*public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException, DataAccessException {
		log.fine("called loadUserByUsername()");
		System.out.println ("called loadByUsername()");
		
		Account result = siteAdminDAO.getUser(getCurrentUser());
		return result;
	}*/
	
	public Integer updateUser(User user) {
		user.setEffectiveDate(new Date());
		return siteAdminDAO.updateUser(user);
	}


	public void createRole(Roles role) {
		if (role == null)
			throw new ServicesSecurityException(getClass() + "Parameter role cannot be null");
		else {
			siteAdminDAO.createRole(role);
		}
	}

	public Integer createUser(User user, List<Roles> roleList) {
		if ((user == null) || (roleList == null)) {
			throw new ServicesSecurityException(SiteAdminServiceImpl.class
					.getName() + "Parameters user or roleList cannot be null");
		}
		//if you need password encrypted...
		//user = encodePassword(user);
		// set effective date to today.
		user.setEffectiveDate(new Date());
		// set password changed on date to today.
		user.setPasswordChangedOnDate(new Date());
		System.out.println("Effective Date: " + user.getEffectiveDate());
		System.out.println("Password Change Date: " + user.getPasswordChangedOnDate());
		int numRowsModified = siteAdminDAO.createUser(user);
		numRowsModified = numRowsModified + siteAdminDAO.assignRoles(user, roleList);

		return numRowsModified;
	}
	

	public List<Roles> getAllRoles() {
		return siteAdminDAO.getAllRoles();
	}

	public List<User> getAllUsers() {
		return siteAdminDAO.getAllUsers();
	}

	public UserSearchCriteria getSpecificUser(UserSearchCriteria userSearchCriteria) {
		return siteAdminDAO.getSelUser(userSearchCriteria);
		
		
	}

	
	public User getCurrentUser() {
		UserDetails userDetails = null;
		User currentUser = null;
		
		Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if ( object instanceof UserDetails ) {
			userDetails = (UserDetails)object;
			
			User user = new User(userDetails.getUsername());
			currentUser = siteAdminDAO.getUser(user);
		}
		else {
			throw new ServicesSecurityException(SiteAdminServiceImpl.class
					.getName() + " object must of type UserDetails.");
		}
		System.out.println("current User is " + currentUser);
		return currentUser;
	}
	

	public Boolean isUsernameUnique(String username) {
		Boolean isUnique = Boolean.FALSE;
		if(siteAdminDAO.isUsernameUnique(username) == 0) {
			isUnique = Boolean.TRUE;
			log.fine("username IS unique");		
			
		}
		return isUnique;
	}
	
	/*
	 * @param User user raw password
	 * @return User user encoded password
	 
	private User encodePassword(User user) {
		PasswordEncoder passwordEncoder = new ShaPasswordEncoder(ThreatConstants.SECURITY_STRENGTH);
		// encodePassword( raw password, salt)
		String encryptedPassword = passwordEncoder.encodePassword(user.getPassword(), user.getUsername());
		user.setPassword(encryptedPassword);

		return user;
	}*/
	

	// TODO move createNewAdmin to external script. 
	public void createNewAdmin(String username, String password, String role) {
		User adminUser = new User ();
		adminUser.setUsername(ThreatConstants.SECURITY_ADMIN_USERNAME);
		adminUser.setFirstName(ThreatConstants.SECURITY_ADMIN_FIRSTNAME);
		adminUser.setLastName(ThreatConstants.SECURITY_ADMIN_LASTNAME);
		adminUser.setEnabled(Boolean.TRUE);		
		//adminUser = encodePassword(adminUser);
				
		List<Roles> roleList = new ArrayList<Roles>();
		roleList.add(new Roles(ThreatConstants.RTA_USER_ROLE_NAME));
		roleList.add(new Roles(role));
		
		createUser(adminUser, roleList);
	}
	

	public void deleteUser(User user) {
		// TODO Auto-generated method stub
	}

	
	public void deleteRole(Roles role) {
		// TODO Auto-generated method stub
		
	}

	
	public void updateRole(Roles role) {
		// TODO Auto-generated method stub
		
	}
	

	public List<Roles> getRolesForSelectedUser(User user) {
		return siteAdminDAO.getAssignedRolesForUser(user);
	}
	
    private void validateNewPassword(FacesContext context, UIComponent validate, Object value) {
//    	String encryptedNewPassword = service.encodePassword(newPassword, super.getCurrentUserID());
//    	String encryptedCurrentPassword = service.encodePassword(currentPassword, super.getCurrentUserID());
    	
//    	System.out.println("username: " + super.getCurrentUserID() + ", password: " + newPassword);
//    	System.out.println("New Password: " + newPassword);
//    	System.out.println("Confirm Password: " + confirmPassword);
//    	System.out.println("Current User's Password: " + currentUser.getPassword());
//    	System.out.println("Entered Encrypted Current Password: " + encryptedCurrentPassword);
//    	System.out.println("Entered Encrypted New Password: " + encryptedNewPassword);
    	
    	log.info("validateNewPassword");
    	
    	

//        if(newPassword.length() < 6) {
//            super.addErrorMessage("Length must be at least 6 characters.");
//            ((UIInput) validate).setValid(Boolean.FALSE);
//        }
//        if(newPassword.length() > 12) {
//            super.addErrorMessage("Length cannot be greater than 12 characters.");
//            ((UIInput) validate).setValid(Boolean.FALSE);
//        }
//        if(encryptedNewPassword.equals(currentUser.getPassword())) {
//        	super.addErrorMessage("New password cannot be the same as the previous password.");
//        	((UIInput) validate).setValid(Boolean.FALSE);
//        }

    }
    
    public void assignRolesForUser(User user, List<Roles> rolesList) {
    	siteAdminDAO.assignRoles(user, rolesList);
    }
    
	public User updatePassword(User user) {
		user.setChangePassword(Boolean.FALSE);
		user.setPasswordChangedOnDate(new Date());
		//user = encodePassword(user);
		return user;
	}

    
	/**
	 * @return the siteAdminDAO
	 */
	public SiteAdminDAO getSiteAdminDAO() {
		return siteAdminDAO;
	}

	/**
	 * @param siteAdminDAO the siteAdminDAO to set
	 */
	public void setSiteAdminDAO(SiteAdminDAO siteAdminDAO) {
		this.siteAdminDAO = siteAdminDAO;
	}

	public UserDetails loadUserByUsername(String arg0)
			throws UsernameNotFoundException, DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}
