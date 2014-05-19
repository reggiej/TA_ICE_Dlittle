/**
 *
 */
package com.demo.app.dao.db.jdbc;

import com.demo.app.dao.DaoException;

/**
 * @author Kunta L.
 *
 */

public class JDBCException extends DaoException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4398832512660445969L;

	public JDBCException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public JDBCException (String msg, Throwable cause){
		super (msg, cause);
	}

}
