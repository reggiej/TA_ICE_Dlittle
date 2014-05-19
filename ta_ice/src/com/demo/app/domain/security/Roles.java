package com.demo.app.domain.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * This class models the Roles table. It contains the roles for the system.
 * @author Kunta L.
 *
 */
@Entity
@Table (name="ROLES")
public class Roles {
		
	@Id
	@GeneratedValue
	@Column (columnDefinition="ROLES_SEQ", unique=true)
	private Long roleID;

	@Column (columnDefinition="ROLENAME")
	private String roleName;
	    
	@Column (columnDefinition="ROLE_DESCRIPTION")
	private String roleDescription;
	
    @Column (columnDefinition="EFFECTIVE_DT")
    private Date effectiveDate;
	    
	public Roles() {
	}
	
	public Roles( String roleName ) {
	    this.roleName = roleName;
	}

	public Roles(Long roleID, String roleName, String roleDescription, Date effectiveDate ) {
		this.roleID = roleID;
	    this.roleName = roleName;
	    this.roleDescription = roleDescription;
	    this.effectiveDate = new Date();
	}

	public Long getRoleID() {
		return roleID;
	}

	public void setRoleID(Long roleID) {
		this.roleID = roleID;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
}
