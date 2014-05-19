/**
 * 
 */
package com.demo.app.web;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * @author Kunta L.
 *
 */
public class PhaseSync implements PhaseListener {

	/* (non-Javadoc)
	 * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -89104930944819191L;

	public void afterPhase(PhaseEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
	 */
	
	public void beforePhase(PhaseEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.faces.event.PhaseListener#getPhaseId()
	 */
	
	public PhaseId getPhaseId() {
		// TODO Auto-generated method stub
		return PhaseId.INVOKE_APPLICATION;
	}

}
