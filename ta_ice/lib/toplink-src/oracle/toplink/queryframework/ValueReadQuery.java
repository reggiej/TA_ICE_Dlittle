// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.internal.queryframework.*;
import oracle.toplink.exceptions.*;

/**
 * <p><b>Purpose</b>:
 * Concrete class to perform a read of a single data value.
 * <p>
 * <p><b>Responsibilities</b>:
 * Used in conjunction with CursoredStream size and Platform getSequence.
 * This can be used to read a single data value (i.e. one field).
 * A single data value is returned, or null if no rows are returned.
 *
 * @author James Sutherland
 * @since TOPLink/Java 1.2
 */
public class ValueReadQuery extends DirectReadQuery {

    /**
     * PUBLIC:
     * Initialize the state of the query.
     */
    public ValueReadQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified SQL string.
     * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public ValueReadQuery(String sqlString) {
        super(sqlString);
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call.
     */
    public ValueReadQuery(Call call) {
        super(call);
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Perform the work to execute the SQL string.
     * @exception  DatabaseException an error has occurred on the database
     * @return Object the data value or null.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        Object values = super.executeDatabaseQuery();
        ContainerPolicy cp = getContainerPolicy();
        if (cp.sizeFor(values) == 0) {
            return null;
        } else {
            return cp.next(cp.iteratorFor(values), getSession());
        }
    }
    
    /**
     * PUBLIC:
     * Return if this is a value read query.
     */
    public boolean isValueReadQuery() {
        return true;
    }
}