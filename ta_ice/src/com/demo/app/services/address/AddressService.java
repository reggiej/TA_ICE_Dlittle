package com.demo.app.services.address;



import java.util.List;

import com.demo.app.domain.address.Address;
import com.icesoft.faces.component.ext.RowSelectorEvent;



public interface AddressService {
	

	
	
	public void createAddress(Address address);

	public List<Address> getAllAddresss();
	
	public void removeAllAddress();
	

}