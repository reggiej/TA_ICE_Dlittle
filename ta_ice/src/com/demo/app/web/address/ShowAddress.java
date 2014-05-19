/*package com.demo.app.web.address;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.demo.app.domain.address.Address;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.services.WebSecurityException;
import com.demo.app.services.address.AddressService;
import com.demo.app.services.security.SiteAdminService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;


*//**
 * The Class CreateUser.
 * 
 * @author Kunta L.
 *//*

public class ShowAddress extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(ShowAddress.class.getName());
	
	private AddressService addressService;
		
	private Address address;
	
	private List<Address> currentAddresss;
  
    public ShowAddress() {
    	init();
    }
    
    

	public String getBeanName() {
		return ThreatConstants.BEAN_CREATE_ADDRESS_SECURITY;
	}
	
	*//**
	 * Inits the CreateAddress instance.
	 *//*
	@PostConstruct
	private void init (){
		addressService =(AddressService)getService(ThreatConstants.SERVICE_ADDRESS_REFERENCE);
		currentAddresss = addressService.getAllAddresss();
        address = new Address ();
		
		
	}
	
	

	public List<Address> getCurrentAddresss() {
		return currentAddresss;
	}



	public void setCurrentAddresss(List<Address> currentAddresss) {
		this.currentAddresss = currentAddresss;
	}



	public String createNewAddress () {	
		if ((addressService != null)){
			
			addressService.createAddress(address);
			log.fine("after reset. now returning next");			
			resetValues();
			super.addInfoMessage("Address  created.");
			return null;
		}
		else {
			throw new WebSecurityException(" createNewAddress() values cannot be null");
		}
	}



	private void resetValues() {
		address = new Address ();
		
	}



	public Address getAddress() {
		return address;
	}



	public void setAddress(Address address) {
		this.address = address;
	}



		
}
*/