// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.io.Serializable;
import oracle.toplink.sessions.*;

/**
 * <p><b>Purpose</b>:
 * Query redirection allows for a named or parameterized query (or finder) to dynamically determine how the query is to be executed based on the arguments.
 *
 * <p><b>Description</b>:
 * An implementor of this interface can be given to a query to allow the user to
 * have full control over the execution of the query.
 * Redirection can be used to:
 * <ul>
 * <li> Dynamically configure the query options based on the arguments (i.e. ordering, query optimization... etc.).
 * <li> Dynamically define the selection criteria based on the arguments.
 * <li> Pass Query By Example objects or Expressions as the arguments.
 * <li> Post prosses the query results.
 * <li> Perform multiple queries or non-TopLink operations.
 * </ul>
 * <p>
 * Note: If you execute the query on a UnitOfWork, the results register with that
 * UnitOfWork, so any objects you attempt to retrieve with the <code>invoke</code>
 * method must come from the Session Cache.
 * @see MethodBaseQueryRedirector
 * @see DatabaseQuery#setRedirector
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public interface QueryRedirector extends Serializable {

    /**
     * REQUIRED:
     * Perform the query.
     * This must execute the query base on the arguments and return a valid result for the query.
     */
    Object invokeQuery(DatabaseQuery query, Record arguments, Session session);
}