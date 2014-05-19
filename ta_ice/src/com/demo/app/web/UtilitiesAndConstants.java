/**
 * 
 */
package com.demo.app.web;

import java.util.List;

import javax.faces.model.SelectItem;


/**
 * @author Kunta L.
 * 
 */
public class UtilitiesAndConstants {

	private List<SelectItem> usStates;
	
	/*
	public UtilitiesAndConstants() {
		init();
	}
	
	private void init() {
		initializeStates();
	}

	private void initializeStates() {
		usStates = new ArrayList<SelectItem>();
		SelectItem blankItem = new SelectItem ();
		blankItem.setLabel(ThreatConstants.BLANK);
		blankItem.setValue(ThreatConstants.BLANK);
		usStates.add(blankItem);
		for (State state : State.US_STATES) {
			if (state != null) {
				SelectItem item = new SelectItem();
				item.setLabel(state.getAbbreviation() + " - "
						+ state.getFullState());
				item.setValue(state.getAbbreviation());
				usStates.add(item);
			}
		}
	}*/
	
	/**
	 * @return the usStates
	 */
	public List<SelectItem> getUsStates() {
		return usStates;
	}
}


