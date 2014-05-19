package com.demo.app.domain.security;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * This class models the Authorities table. It relates a user to a user's assigned roles. 
 * @author Kunta L.
 *
 */

@SuppressWarnings("serial")
@Entity
@Table (name="AUTHORITIES")
public class Authorities implements Serializable {
	
	@Id
	@GeneratedValue
	@Column (columnDefinition="AUTHORITIES_SEQ", unique=true)
	private Long authoritiesId;
	
	@Column (columnDefinition="ROLE_SEQ")
	private String roleId;
	
	@Column (columnDefinition="USER_SEQ")
	private Long userId; 
	
	@Column (columnDefinition="ORG_SEQ")
	private Long orgId; 
    
    @Column (columnDefinition="EFFECTIVE_DT")
    private Date effectiveDate;
    
    public Authorities() {}
    
    public Authorities(Long authoritiesId, String roleId, Long userId, Long orgId, Date effectiveDate) {
    	this.authoritiesId = authoritiesId;
    	this.roleId = roleId;
    	this.userId = userId;
    	this.orgId = orgId;
    	this.effectiveDate = new Date();
    }
    
	public Long getAuthoritiesId() {
		return authoritiesId;
	}

	public void setAuthoritiesId(Long authoritiesId) {
		this.authoritiesId = authoritiesId;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Long getOrgId() {
		return orgId;
	}
}
