// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transaction;

import javax.transaction.Synchronization;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;

/**
 * <p>
 * <b>Purpose</b>: Synchronization object implementation for JTA 1.0
 * <p>
 * <b>Description</b>:  Instances of this class are registered against JTA 1.0
 * transactions. This class may be subclassed to provide specialized behavior
 * for specific transaction implementations. Subclasses must implement the
 * newListener() method to return an instances of the listener subclass.
 *
 * @see JTATransactionController
 */
public class JTASynchronizationListener extends AbstractSynchronizationListener implements Synchronization, SynchronizationListenerFactory {

    /**
     * PUBLIC:
     * Used to create factory instances only. Use the "full-bodied" constructor
     * for creating proper listener instances.
     */
    public JTASynchronizationListener() {
        super();
    }

    /**
     * INTERNAL:
     * Constructor for creating listener instances (expects all required state info)
     */
    public JTASynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller) {
        super(unitOfWork, session, transaction, controller);
    }

    /**
     * INTERNAL:
     * Create and return the Synchronization listener object that can be registered
     * to receive JTA transaction notification callbacks.
     */
    public AbstractSynchronizationListener newSynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller) {
        return new JTASynchronizationListener(unitOfWork, session, transaction, controller);
    }

    /**
     * INTERNAL:
     * Called by the JTA transaction manager prior to the start of the
     * transaction completion process.
     * This call is executed in the same transaction context of the caller
     * who initiates the TransactionManager.commit, or the call is executed
     * with no transaction context if Transaction.commit is used.
     */
    public void beforeCompletion() {
        super.beforeCompletion();
    }

    /**
     * INTERNAL:
     * Called by the JTA transaction manager after the transaction is committed
     * or rolled back. This method executes without a transaction context.
     *
     * @param stat The status of the transaction completion.
     */
    public void afterCompletion(int stat) {
        super.afterCompletion(new Integer(stat));
    }
}