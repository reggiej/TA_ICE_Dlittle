package com.demo.app.services.address;

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

import com.demo.app.dao.db.AddressDAO;
import com.demo.app.dao.db.SiteAdminDAO;
import com.demo.app.domain.UserSearchCriteria;
import com.demo.app.domain.address.Address;
import com.demo.app.domain.security.Roles;
import com.demo.app.domain.security.User;
import com.demo.app.services.security.ServicesSecurityException;
import com.demo.app.services.security.SiteAdminServiceImpl;
import com.demo.app.util.ThreatConstants;
import com.icesoft.faces.component.ext.RowSelectorEvent;

public class AddressServiceImpl implements AddressService {
	
	private AddressDAO addressDAO;
	private static Logger log = Logger.getLogger(AddressService.class.getName());
	
	/*
	 * Default Constructor
	 */
	public AddressServiceImpl() {
		log.fine("Initializing Address Service");
	}
	

	
	
	


	public void createAddress(Address address) {
		if ((address == null)) {
			throw new ServicesSecurityException(AddressServiceImpl.class
					.getName() + "Parameters address cannot be null");
		}else{
			
		addressDAO.svAddress(address);
		System.out.println("This is the new address: " + address.getAddress() + ", " + address.getCity() + ", " +address.getState() + ", " + address.getZip() );
		
		}
	}







	public AddressDAO getAddressDAO() {
		return addressDAO;
	}







	public void setAddressDAO(AddressDAO addressDAO) {
		this.addressDAO = addressDAO;
	}







	public List<Address> getAllAddresss() {
		
		return addressDAO.getAddresss();
	}







	public void removeAllAddress() {
		addressDAO.removeAddress();
		
	}















	
	/*public void rowSelected(RowSelectorEvent event) {
		addressDAO.
	    System.out.println(event.getRow());
	}*/
	
	
	
}
