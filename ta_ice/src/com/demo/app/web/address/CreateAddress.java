package com.demo.app.web.address;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.demo.app.domain.address.Address;
import com.demo.app.services.WebSecurityException;
import com.demo.app.services.address.AddressService;
import com.demo.app.util.ThreatConstants;
import com.demo.app.web.BaseUIBean;
import com.icesoft.faces.component.ext.RowSelectorEvent;


/**
 * The Class CreateAddress.
 * 
 * @author Kunta L.
 */

public class CreateAddress extends BaseUIBean {
	
	private static Logger log = Logger.getLogger(CreateAddress.class.getName());
	
	private AddressService addressService;
		
	private Address address;
	
	private Address selectedList;
	
	private List<Address> currentAddresss = new ArrayList<Address>();
	
	private List<Address> adderAddresss = new ArrayList<Address>();



	public CreateAddress() {
    	init();
    }
    
    

	public String getBeanName() {
		return ThreatConstants.BEAN_CREATE_ADDRESS_SECURITY;
	}
	
	/**
	 * Inits the CreateAddress instance.
	 */
	private void init (){
		addressService =(AddressService)getService(ThreatConstants.SERVICE_ADDRESS_REFERENCE);
		currentAddresss = addressService.getAllAddresss();
		address = new Address ();
		adderAddresss = new ArrayList<Address>();
		selectedList = new Address();
	//dummyAddress();
	}


	public String createNewAddress () {	
		System.out.println( "checking addrtess");
		if ((address != null)){		
			System.out.println( "adress is not null");
			address.setEffectiveDate(new Date());
		    currentAddresss.add(address);
			
			addressService.createAddress(address);
			System.out.println( "data is persistant");
			
			resetValues();
			super.addInfoMessage("Address  created.");
			return "todoPage";
		}
		else {
			throw new WebSecurityException(" createNewAddress() values cannot be null");
		}
		
		
	}


public void rowSelectionListener(RowSelectorEvent event) {
		 selectedList = currentAddresss.get(event.getRow());
		
		System.out.println("The Selected address is as follows " + selectedList.getAddress() + " " + selectedList.getCity() + " " + selectedList.getState() + " " + selectedList.getZip());
				
			if(adderAddresss != null && !adderAddresss.isEmpty()){
				System.out.println("Address not null!!!! ");
				helpUs(selectedList);
				
			}else {
                   System.out.println("Address is null!!!! " + selectedList.getAddressID());
				
						adderAddresss.add(selectedList);
						System.out.println("done2");
					}
				
			}
			
	public void helpUs(Address selectedList){
		
		for (Address newadd : adderAddresss){
			System.out.println("Address looping!!!! " + newadd.getAddressID());
			if(selectedList.getAddressID()!= newadd.getAddressID()){
				
				
				System.out.println("done");
				
			}
		
		}
	}

	private void resetValues() {
		address = new Address();
	}



	public Address getAddress() {
		return address;
	}



	public void setAddress(Address address) {
		this.address = address;
	}

	

	public List<Address> getCurrentAddresss() {
		return currentAddresss;
	}



	public void setCurrentAddresss(List<Address> currentAddresss) {
		this.currentAddresss = currentAddresss;
	}



	public List<Address> getAdderAddresss() {
		return adderAddresss;
	}



	public void setAdderAddresss(List<Address> adderAddresss) {
		this.adderAddresss = adderAddresss;
	}



	public void removeAddress(){
		addressService.removeAllAddress();
		currentAddresss = addressService.getAllAddresss();
	}



	public Address getSelectedList() {
		return selectedList;
	}



	public void setSelectedList(Address selectedList) {
		this.selectedList = selectedList;
	}
	
	
	
}
