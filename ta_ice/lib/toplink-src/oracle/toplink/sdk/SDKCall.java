// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.queryframework.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * <code>SDKCall</code> augments the <code>Call</code> interface
 * to define a bit more
 * behavior, as required by the <code>SDKQueryMechanism</code>:<ul>
 * <li> When a query is executed, it is cloned; the call must be
 * able to provide a clone of itself that corresponds to this cloned query.
 * <li> The query mechanism will invoke the call at execution time
 * and pass it the parameters via a database row. The query
 * mechanism will also pass the accessor to the call.
 * </ul>
 *
 * @see SDKQueryMechanism
 * @see SDKAccessor
 *
 * @author Big Country
 *    @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public interface SDKCall extends Call {

    /**
     * Execute the call and return the results.
     */
    Object execute(AbstractRecord translationRow, Accessor accessor) throws SDKDataStoreException;
}