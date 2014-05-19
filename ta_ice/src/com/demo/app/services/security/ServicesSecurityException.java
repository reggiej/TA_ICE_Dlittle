/**
 *
 */
package com.demo.app.services.security;

import com.demo.app.services.ServicesException;


public class ServicesSecurityException extends ServicesException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4974889721845304239L;
	
	/**
	 *
	 * @param msg
	 */
	public ServicesSecurityException (String msg){
		super (msg);
	}
	
	/**
	 * @param msg
	 * @param cause
	 */
	public ServicesSecurityException (String msg, Throwable cause){
		super (msg, cause);
	}

}
