package com.demo.app.domain.address;

import java.io.Serializable;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
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
@Table (name="Address")
public class Address implements Serializable  {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 8817778962726766801L;

	@Id
	@GeneratedValue
	@Column (columnDefinition="ADDRESS_SEQ", unique=true)
	private Long addressID;

    @Column (columnDefinition="ADDRESS")
    private String address;
    
    @Column (columnDefinition="CITY")
    private String city;

    @Column (columnDefinition="STATE")
    private String state;
    
    @Column (columnDefinition="ZIP")
    private String zip;
    
    @Column (columnDefinition="EFFECTIVE_DT")
    private Date effectiveDate;
    
    
	

	public Address() {    
		
	}
	
       
    public Address(String address, String city, String state, 
    		String zip, Date effectiveDate) {
    	
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.effectiveDate = new Date();
        
    }


	public Long getAddressID() {
		return addressID;
	}


	public void setAddressID(Long addressID) {
		this.addressID = addressID;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getZip() {
		return zip;
	}


	public void setZip(String zip) {
		this.zip = zip;
	}


	public Date getEffectiveDate() {
		return effectiveDate;
	}


	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

    
	
	
	
	
}
   
