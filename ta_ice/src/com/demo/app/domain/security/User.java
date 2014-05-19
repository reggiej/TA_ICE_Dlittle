package com.demo.app.domain.security;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * This class models the User table.
 * @author Kunta L.
 *
 */
@Entity
@Table (name="USERS")
public class User implements Serializable, Comparable<User>  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Id
	@GeneratedValue
	@Column (columnDefinition="USER_SEQ", unique=true)
	private Long userID;

    @Column (columnDefinition="FIRSTNAME")
    private String firstName;
    
    @Column (columnDefinition="LASTNAME")
    private String lastName;

    @Column (columnDefinition="USER_MI_NAME")
    private String userMidname;
    
    @Column (columnDefinition="USERNAME")
    private String username;
    
    @Column (columnDefinition="USER_TITLE")
    private String userTitle;
    
    @Column (columnDefinition="USER_SECURE_PH")
    private String userSecurePH;
    
    @Column (columnDefinition="USER_COMM_PH")
    private String userCommPH;
    
    @Column (columnDefinition="USER_CELL_PH")
    private String userCellPH;
    
    @Column (columnDefinition="PASSWORD")
    private String password;

    @Column (columnDefinition="ENABLED")
    private Boolean enabled;

    @Column (columnDefinition="EFFECTIVE_DT")
    private Date effectiveDate;
    
    @Column (columnDefinition="PASSWORD_CH_EFFECTIVE_TS")
    private Date passwordChangedOnDate;
    
    @Column (columnDefinition="CHANGE_PASSWORD")
    private Boolean passwordExpired;

    @Column (columnDefinition="ORG_SEQ")
	private Long orgId; 
    
	public void setPasswordExpired(Boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	public User() {    }
	
    public User(Long userID) {
    	this.userID = userID;    	
    	this.effectiveDate = new Date();
	}
	
    public User(String username) {
    	this.username = username;    	
    	this.effectiveDate = new Date();
	}
    
    public User(String password, String username) {
    	this.username = username;    	
    	this.password = password;
    	this.effectiveDate = new Date();
   }

    public User(String username, String firstName, String lastName, String userMidname, 
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 * This method compares this object with o1 object. Returned int value has the following meanings.
     *	positive – this object is greater than o1
	 *	zero – this object equals to o1
	 *	negative – this object is less than o1
	 * 
	 */
	
	public int compareTo(User o) {
		int result = 1;
		if(this.getUsername().equals(o.getUsername())) {
			result = 0;
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof User)) {
			User param = (User) obj;
			if (getUsername() != null)
				return param.getUsername().equals(this.getUsername());
			else
				return false;
		} else
			return false;
	}
	
	public int hashcode() {
		int hash = 1;
	    hash = hash * 31 + userID.hashCode();
	    hash = hash * 31 + (effectiveDate == null ? 0 : username.hashCode());
	    
	    return hash;
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
   
