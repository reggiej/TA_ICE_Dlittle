/**
 *
 */
package com.demo.app.web;

/**
 * @author Kunta L.
 *
 */
public class WebException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -785329826737140620L;
	
	/**
	 *
	 * @param msg
	 */
	public WebException (String msg){
		super (msg);
	}
	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public WebException (String msg, Throwable cause){
		super (msg, cause);
	}

}
