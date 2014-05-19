// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transaction;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;

/**
 * <p>
 * <b>Purpose</b>: Generate synchronization listener objects of the appropriate type.
 * <p>
 * <b>Description</b>: This interface will be used by the AbstractTransactionController
 * to obtain a listener that it will register against the external transaction in order
 * to synchronize the unit of work.
 * All new listener classes should implement this interface.
 *
 * @see AbstractSynchronizationListener
 */
public interface SynchronizationListenerFactory {

    /**
     * INTERNAL:
     * Create and return the synchronization listener object that can be registered
     * to receive transaction notification callbacks. The type of synchronization object
     * that gets returned will be dependent upon the transaction system
     */
    public AbstractSynchronizationListener newSynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller);
}