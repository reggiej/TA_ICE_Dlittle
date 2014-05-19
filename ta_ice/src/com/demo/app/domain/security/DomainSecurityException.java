/**
 *
 */
package com.demo.app.domain.security;

import com.demo.app.dao.DaoException;


/**
 * @author kalittl
 *
 */
public class DomainSecurityException extends DaoException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8926997257982143539L;

	public DomainSecurityException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public DomainSecurityException (String msg, Throwable cause){
		super (msg, cause);
	}

}
