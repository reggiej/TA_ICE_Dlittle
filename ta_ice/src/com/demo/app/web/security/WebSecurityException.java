/**
 *
 */
package com.demo.app.web.security;

import com.demo.app.web.WebException;


/**
 * @author kalittl
 *
 */
public class WebSecurityException extends WebException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8182382045297257036L;

	public WebSecurityException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public WebSecurityException (String msg, Throwable cause){
		super (msg, cause);
	}

}
