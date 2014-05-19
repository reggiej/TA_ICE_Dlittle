package com.demo.app.dao.db;


import java.util.List;

import com.demo.app.domain.address.Address;
import com.icesoft.faces.component.ext.RowSelectorEvent;



/**
 * @author Kunta L.
 *
 */

public interface AddressDAO {
	
	

	public void svAddress (Address address);

	public List<Address> getAddresss();
	
	public void removeAddress();
	
}