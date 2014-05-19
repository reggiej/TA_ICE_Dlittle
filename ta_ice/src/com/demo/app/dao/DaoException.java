package com.demo.app.dao;
/**
* @author Kunta L.
*/


public class DaoException extends RuntimeException {

	
	/**
	 *
	 */
	private static final long serialVersionUID = 8277722205979251865L;
	/**
	 *
	 * @param msg
	 */
	public DaoException (String msg){
		super (msg);
	}
	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public DaoException (String msg, Throwable cause){
		super (msg, cause);
	}

}
