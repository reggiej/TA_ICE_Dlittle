package com.demo.app.domain;

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
@Table (name="SECURITY_TYPES")
public class SecurityTypes {
		
	@Id
	@GeneratedValue
	@Column (columnDefinition="SECURITY_TYPES_SEQ", unique=true)
	private Long securityTypeID;

	@Column (columnDefinition="SECURITY_TYPES_NAME")
	private String securityTypeName;
	    
	@Column (columnDefinition="SECURITY_TYPES_DESCRIPTION")
	private String securityTypeDescription;
	
    @Column (columnDefinition="EFFECTIVE_DT")
    private Date effectiveDate;
	    
	public SecurityTypes() {
	}
	
	public SecurityTypes( String securityTypeName ) {
	    this.securityTypeName = securityTypeName;
	}

	public SecurityTypes(Long securityTypeID, String securityTypeName, String securityTypeDescription, Date effectiveDate ) {
		this.securityTypeID = securityTypeID;
	    this.securityTypeName = securityTypeName;
	    this.securityTypeDescription = securityTypeDescription;
	    this.effectiveDate = new Date();
	}


	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Long getSecurityTypeID() {
		return securityTypeID;
	}

	public void setSecurityTypeID(Long securityTypeID) {
		this.securityTypeID = securityTypeID;
	}

	public String getSecurityTypeName() {
		return securityTypeName;
	}

	public void setSecurityTypeName(String securityTypeName) {
		this.securityTypeName = securityTypeName;
	}

	public String getSecurityTypeDescription() {
		return securityTypeDescription;
	}

	public void setSecurityTypeDescription(String securityTypeDescription) {
		this.securityTypeDescription = securityTypeDescription;
	}

	
	
	
}
