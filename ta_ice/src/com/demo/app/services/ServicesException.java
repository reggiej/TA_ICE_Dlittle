/**
 *
 */
package com.demo.app.services;

/**
 * @author Kunta L.
 *
 */
public class ServicesException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 785353710369397745L;
	
	/**
	 *
	 * @param msg
	 */
	public ServicesException (String msg){
		super (msg);
	}
	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public ServicesException (String msg, Throwable cause){
		super (msg, cause);
	}

}
