/**
 * 
 */
package com.demo.app.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Kunta Little
 *
 */
public class UserSearchCriteria implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	private Long userID;

    private String firstName;
    
    private String lastName;

    private String userMidname;
    
    private String username;
    
    private String userTitle;
    
    private String userSecurePH;
    
    private String userCommPH;
    
    private String userCellPH;
    
    private String password;

    private Boolean enabled;

    private Date effectiveDate;
    
    private Date passwordChangedOnDate;
    
    private Boolean passwordExpired;

    private Long orgId;
    
	public void setPasswordExpired(Boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	public UserSearchCriteria() {    }
	
    public UserSearchCriteria(Long userID) {
    	this.userID = userID;    	
    	this.effectiveDate = new Date();
	}
	
    public UserSearchCriteria(String username) {
    	this.username = username;    	
    	this.effectiveDate = new Date();
	}
    
    public UserSearchCriteria(String password, String username) {
    	this.username = username;    	
    	this.password = password;
    	this.effectiveDate = new Date();
   }

    public UserSearchCriteria(String username, String firstName, String lastName, String userMidname, 
    		String userTitle, String userSecurePH, String userCommPH, String userCellPH,
                   String password, Boolean enabled, Date effectiveDate, Long orgId) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userMidname = userMidname;
        this.userTitle = userTitle;
        this.userSecurePH = userSecurePH;
        this.userCommPH = userCommPH;
        this.userCellPH = userCellPH;
        this.password = password;
        this.enabled = enabled;
        this.effectiveDate = new Date();
        this.passwordChangedOnDate = new Date();
        this.passwordExpired = Boolean.TRUE;
        this.orgId = orgId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	
	public Date getPasswordChangedOnDate() {
		return passwordChangedOnDate;
	}

	public void setPasswordChangedOnDate(Date passwordChangedOnDate) {
		this.passwordChangedOnDate = passwordChangedOnDate;
	}

	public Boolean getPasswordExpired() {
		return passwordExpired;
	}
	
	public Boolean isPasswordExpired() {
		return passwordExpired;
	}

	public void setChangePassword(Boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	public void setUserMidname(String userMidname) {
		this.userMidname = userMidname;
	}

	public String getUserMidname() {
		return userMidname;
	}

	public void setUserTitle(String userTitle) {
		this.userTitle = userTitle;
	}

	public String getUserTitle() {
		return userTitle;
	}

	public void setUserSecurePH(String userSecurePH) {
		this.userSecurePH = userSecurePH;
	}

	public String getUserSecurePH() {
		return userSecurePH;
	}

	public void setUserCommPH(String userCommPH) {
		this.userCommPH = userCommPH;
	}

	public String getUserCommPH() {
		return userCommPH;
	}

	public void setUserCellPH(String userCellPH) {
		this.userCellPH = userCellPH;
	}

	public String getUserCellPH() {
		return userCellPH;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
	
	
}
   
