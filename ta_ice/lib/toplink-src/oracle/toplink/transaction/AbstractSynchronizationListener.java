// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.toplink.exceptions.TransactionException;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.sequencing.SequencingCallback;
import oracle.toplink.internal.sequencing.SequencingCallbackFactory;
import oracle.toplink.logging.*;
import oracle.toplink.sessionbroker.SessionBroker;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.sessions.DatabaseSession;

/**
 * <p>
 * <b>Purpose</b>: Abstract Synchronization Listener class
 *
 * <b>Description</b>: This abstract class is paired with the
 * AbstractTransactionController class. It contains most of the implementation
 * logic to handle callback notifications from an external transaction
 * manager to ensure consistency between the global transaction and the
 * TopLink unit of work. It does not assume any particular specification
 * or interface, but can be called by any implementation subclass.
 *
 * @see AbstractTransactionController
 */
public abstract class AbstractSynchronizationListener {

    /**
     * The external txn controller that is intimate with the transaction manager
     * and knows how to do things like rolling back transactions, etc.
     */
    protected AbstractTransactionController controller;

    /**
     * The parent of the uow.
     */
    protected AbstractSession session;

    /**
     * The unit of work associated with the global txn that this listener is
     * bound to. 
     * Note that unitOfWork is null in case it's a purely sequencing listener.
     */
    protected UnitOfWorkImpl unitOfWork;

    /**
     * The global transaction object.
     */
    protected Object transaction;

    /**
     * The global transaction key.
     */
    protected Object transactionKey;

    /**
     * sequencingCallback used in case listener has a single callback.
     */
    protected SequencingCallback sequencingCallback;

    /**
     * sequencingCallbackMap used in case listener has more than one callback:
     * SessionBroker with at least two members requiring callbacks.
     */
    protected Map<DatabaseSession, SequencingCallback> sequencingCallbackMap;

    /**
     * INTERNAL:
     */
    public AbstractSynchronizationListener() {
        super();
    }

    /**
     * INTERNAL:
     */
    protected AbstractSynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller) {
        this.session = session;
        this.unitOfWork = unitOfWork;
        this.transaction = transaction;
        this.controller = controller;
        this.transactionKey = controller.getTransactionKey(transaction);
    }

    /**
     * INTERNAL:
     * This method performs the logic that occurs at transaction
     * completion time. This includes issuing the SQL, etc.
     * This method executes within the transaction context of the caller of
     * transaction.commit(), or in the case of container-managed transactions,
     * in the context of the method for which the Container started the transaction.
     */
    public void beforeCompletion() {
        UnitOfWorkImpl uow = getUnitOfWork();
        // it's a purely sequencing listener - nothing to do in beforeCompletion.
        if(unitOfWork == null) {
            return;
        }
        try {
            Object status = getTransactionController().getTransactionStatus();
            getTransactionController().logTxStateTrace(uow, "TX_beforeCompletion", status);
            //CR# 3452053 
            session.startOperationProfile(SessionProfiler.JtsBeforeCompletion);

            // In case jts transaction was internally started but completed
            // directly by TransactionManager this flag is still set to true.
            getSession().setWasJTSTransactionInternallyStarted(false);
            
            // If the uow is not active then somebody somewhere messed up 
            if (!uow.isActive()) {
                throw TransactionException.inactiveUnitOfWork(uow);
            }

            // Bail out if we don't think we should actually issue the SQL
            if (!getTransactionController().canIssueSQLToDatabase_impl(status)) {
                // Must force concurrency mgrs active thread if in nested transaction
                if (getSession().isInTransaction()) {
                    getSession().getTransactionMutex().setActiveThread(Thread.currentThread());
                    if(getUnitOfWork().wasTransactionBegunPrematurely()) {
                        getUnitOfWork().setWasTransactionBegunPrematurely(false);
                    }
                    getSession().rollbackTransaction();
                }
                getSession().releaseJTSConnection();
                return;
            }

            // Must force concurrency mgrs active thread if in nested transaction
            if (getSession().isInTransaction()) {
                getSession().getTransactionMutex().setActiveThread(Thread.currentThread());
            }

            // If sequencing callback for this transaction will be required
            // in case it doesn't already exist it will be created on this very listener
            // avoiding adding more listeners while processing a listener.
            if(getTransactionController().isSequencingCallbackRequired()) {
                getTransactionController().currentlyProcessedListeners.put(getTransactionKey(), this);
            }
            
            // Send the SQL to the DB
            uow.issueSQLbeforeCompletion();

            // Fix up our merge state in the unit of work and the session
            uow.setPendingMerge();

        } catch (RuntimeException exception) {
            // Something went wrong (probably while sending SQL to the database).
            uow.log(new SessionLogEntry(uow, SessionLog.WARNING, SessionLog.TRANSACTION, exception));
            // Handle the exception according to transaction manager requirements
            handleException(exception);
        } finally {
            if(getTransactionController().isSequencingCallbackRequired()) {
                getTransactionController().currentlyProcessedListeners.remove(getTransactionKey());
            }
            getSession().releaseJTSConnection();
            session.endOperationProfile(SessionProfiler.JtsBeforeCompletion);
        }
    }

    /**
     * INTERNAL:
     * The method performs the logic that should be executed after the transaction
     * has been completed. The status passed in indicates whether the transaction
     * was committed or rolled back. This status flag may be different for different
     * implementations.
     * This method executes without a transaction context.
     *
     * @param status The status code of the transaction completion.
     */
    public void afterCompletion(Object status) {
        UnitOfWorkImpl uow = getUnitOfWork();
        // it's a purely sequencing listener - call sequencing callback if the transaction has committed.
        if(uow == null) {
            if(getTransactionController().isSequencingCallbackRequired()) {
                if(getTransactionController().canMergeUnitOfWork_impl(status)) {
                    callSequencingCallback();
                }
            }
        } else {
            try {
                // Log the fact that we got invoked
                getTransactionController().logTxStateTrace(uow, "TX_afterCompletion", status);
                //Cr#3452053
                session.startOperationProfile(SessionProfiler.JtsAfterCompletion);
                // The uow should still be active even in rollback case
                if (!uow.isActive()) {
                    throw TransactionException.inactiveUnitOfWork(uow);
                }
    
                // Only do merge if txn was committed
                if (getTransactionController().canMergeUnitOfWork_impl(status)) {
                    if(getTransactionController().isSequencingCallbackRequired()) {
                        callSequencingCallback();
                    }
                    if (uow.isMergePending()) {
                        // uow in PENDING_MERGE state, merge clones
                        uow.mergeClonesAfterCompletion();
                    }
                } else {
                    // call this method again because there may have been no beforeCompletion call
                    // if case transaction is to be rolled back.
                    getSession().releaseJTSConnection();
                    uow.afterExternalTransactionRollback();
                }
            } catch (RuntimeException rtEx) {
                // First log the exception so it gets seen
                uow.log(new SessionLogEntry(uow, SessionLog.WARNING, SessionLog.TRANSACTION, rtEx));
                // Rethrow it just for fun (app servers tend to ignore them at this stage)
                throw rtEx;
            } finally {
                session.endOperationProfile(SessionProfiler.JtsAfterCompletion);
            }
    
            // Clean up by releasing the uow and client session
            if (uow.shouldResumeUnitOfWorkOnTransactionCompletion() && getTransactionController().canMergeUnitOfWork_impl(status)){
                uow.synchronizeAndResume();
                uow.setSynchronized(false);
            }else{
                uow.release();
                // Release the session explicitly
                if (getSession().isClientSession() || (getSession().isSessionBroker() && ((SessionBroker)getSession()).isClientSessionBroker())) {
                    getSession().release();
                }
            }
            getTransactionController().removeUnitOfWork(getTransactionKey());
            setUnitOfWork(null);
            setSession(null);
        }
        if(getTransactionController().isSequencingCallbackRequired()) {
            getTransactionController().removeSequencingListener(getTransactionKey());
            sequencingCallback = null;
            sequencingCallbackMap = null;
        }
        setTransaction(null);
        setTransactionKey(null);
    }

    /**
     * INTERNAL:
     * Do the appropriate thing for when an exception occurs during SQL issuance.
     * The default thing to do is to simply mark the transaction to be rolled back,
     * for those transaction managers that support this, and rethrow the exception.
     * We hope that the exception will do the trick for those that do not allow
     * marking rollback.
     *
     * This method may optionally be overridden by concrete subclass implementations.
     * Different transaction manager vendors may have different reactions to exceptions
     * that get signalled during the commit phase of synchronization.
     */
    public void handleException(RuntimeException exception) {
        // Don't do this just yet, since some may not be able to handle it
        //	getTransactionController().markTransactionForRollback();
        throw exception;
    }

    protected AbstractTransactionController getTransactionController() {
        return controller;
    }

    protected void setTransactionController(AbstractTransactionController newController) {
        controller = newController;
    }

    protected Object getTransaction() {
        return transaction;
    }

    protected void setTransaction(Object transaction) {
        this.transaction = transaction;
    }

    protected Object getTransactionKey() {
        return transactionKey;
    }

    protected void setTransactionKey(Object transactionKey) {
        this.transactionKey = transactionKey;
    }

    protected AbstractSession getSession() {
        return session;
    }

    protected void setSession(AbstractSession session) {
        this.session = session;
    }

    protected UnitOfWorkImpl getUnitOfWork() {
        return unitOfWork;
    }

    protected void setUnitOfWork(UnitOfWorkImpl unitOfWork) {
        this.unitOfWork = unitOfWork;
    }
    
    protected void callSequencingCallback() {
        if(sequencingCallback != null) {
            sequencingCallback.afterCommit(null);
        } else if (sequencingCallbackMap != null) {
            Iterator<SequencingCallback> itCallback = sequencingCallbackMap.values().iterator();
            while(itCallback.hasNext()) {
                itCallback.next().afterCommit(null);
            }
        }
    }

    /**
     * Return sequencingCallback corresponding to the passed session.
     */
    public SequencingCallback getSequencingCallback(DatabaseSession dbSession, SequencingCallbackFactory sequencingCallbackFactory) {
        if(getTransactionController().numSessionsRequiringSequencingCallback() == 1) {
            if(sequencingCallback == null) {
                sequencingCallback = sequencingCallbackFactory.createSequencingCallback();
            }
            return sequencingCallback;
        } else if(getTransactionController().numSessionsRequiringSequencingCallback() > 1) {
            SequencingCallback callback = null;
            if(sequencingCallbackMap == null) {
                sequencingCallbackMap = new HashMap(getTransactionController().numSessionsRequiringSequencingCallback());
            } else {
                callback = sequencingCallbackMap.get(dbSession);
            }
            if(callback == null) {
                callback = sequencingCallbackFactory.createSequencingCallback();
                sequencingCallbackMap.put(dbSession, callback);
            }
            return callback;
        } else {
            // should never happen.
            return null;
        }
    }
}
