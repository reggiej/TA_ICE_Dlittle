// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.io.Serializable;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.queryframework.*;

/**
 * Call defines the interface used primarily by TopLink queries
 * and query mechanisms to perform the necessary actions
 * (read, insert, update, delete) on the data store.
 * A Call can collaborate with an Accessor to perform its
 * responsibilities. The only explicit requirement of a Call is that
 * it be able to supply the appropriate query mechanism for
 * performing its duties. Otherwise, the Call is pretty much
 * unrestricted as to how it should perform its responsibilities.
 *
 * @see DatabaseQuery
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 */
public interface Call extends Cloneable, Serializable {

    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call set as necessary.
     */
    DatabaseQueryMechanism buildNewQueryMechanism(DatabaseQuery query);

    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    DatabaseQueryMechanism buildQueryMechanism(DatabaseQuery query, DatabaseQueryMechanism mechanism);

    /**
     * INTERNAL:
     * Return a clone of the call.
     */
    Object clone();

    /**
     * INTERNAL:
     * Return a string appropriate for the session log.
     */
    String getLogString(Accessor accessor);

    /**
     * INTERNAL:
     * Return whether the call is finished returning
     * all of its results (e.g. a call that returns a cursor
     * will answer false).
     */
    boolean isFinished();
}