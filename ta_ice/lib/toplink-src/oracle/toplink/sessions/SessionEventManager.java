// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.helper.NonSynchronizedVector;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.threetier.ClientSession;
import oracle.toplink.sessions.SessionProfiler;

/**
 * <p><b>Purpose</b>: Used to support session events.
 * To register for events notification an event listener must be registered with the session.
 *
 * @see Session#getEventManager()
 * @see SessionEvent
 */
public class SessionEventManager implements Cloneable, Serializable {
    protected Vector listeners;
    protected Session session;

    /**
     * INTERNAL:
     * Default constructor.
     */
    public SessionEventManager() {
        this.listeners = NonSynchronizedVector.newInstance();
    }

    /**
     * PUBLIC:
     * Create a new session event manager for a session
     */
    public SessionEventManager(Session session) {
        this.listeners = NonSynchronizedVector.newInstance();
        this.session = session;
    }

    /**
     * PUBLIC:
     * Add the event listener to the session.
     * The listner will receive all events raised by this session.
     * Also unit of works acquire from this session will inherit the listenrs.
     */
    public void addListener(SessionEventListener listener) {
        getListeners().addElement(listener);
    }

    /**
     * INTERNAL:
     * Shallow clone the event manager.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            return new InternalError(exception.toString());
        }
    }

    /**
     * INTERNAL:
     * Clone the event manager for the new session.
     */
    public SessionEventManager clone(Session newSession) {
        SessionEventManager newManager = (SessionEventManager)clone();
        newManager.setSession(newSession);
        if (this.listeners != null) {
            newManager.setListeners(new NonSynchronizedVector(this.listeners));
        }
        return newManager;
    }

    /**
     * PUBLIC:
     * The event listners will receive all events raised by this session.
     * Also unit of works acquire from this session will inherit the listenrs.
     */
    public Vector getListeners() {
        if (listeners == null) {
            listeners = NonSynchronizedVector.newInstance();
        }
        return listeners;
    }

    /**
     * INTERNAL:
     * Get the session for this session event manager
     */
    public Session getSession() {
        return session;
    }

    /**
     * PUBLIC:
     * Check if there are any event listeners.
     */
    public boolean hasListeners() {
        return (listeners != null) && (!listeners.isEmpty());
    }

    /**
     * INTERNAL:
     * Raised for missing descriptors for lazy registration.
     */
    public void missingDescriptor(Class missingClass) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.MissingDescriptor, getSession());
        event.setResult(missingClass);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).missingDescriptor(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised for stored proc output parameters.
     */
    public void moreRowsDetected(DatabaseCall call) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.MoreRowsDetected, getSession());
        event.setResult(call);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).moreRowsDetected(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised for stored proc output parameters.
     */
    public void noRowsModified(ModifyQuery query, Object object) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.NoRowsModified, getSession());
        event.setQuery(query);
        event.setResult(object);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).noRowsModified(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised for stored proc output parameters.
     */
    public void outputParametersDetected(Record outputRow, DatasourceCall call) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.OutputParametersDetected, getSession());
        event.setResult(outputRow);
        event.setProperty("call", call);
        event.setQuery(call.getQuery());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).outputParametersDetected(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post acquire client session.
     */
    public void postAcquireClientSession() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostAcquireClientSession, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postAcquireClientSession(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised after acquire a connection from a connection pool.
     */
    public void postAcquireConnection(Accessor accessor) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostAcquireConnection, getSession());
        event.setResult(accessor);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postAcquireConnection(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised after acquire a connection from a connection pool.
     */
    public void postAcquireExclusiveConnection(ClientSession clientSession, Accessor accessor) {
        if (!hasListeners()) {
            return;
        }

        SessionEvent event = new SessionEvent(SessionEvent.PostAcquireExclusiveConnection, clientSession);
        event.setResult(accessor);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postAcquireExclusiveConnection(event);
        }
    }

    /**
     * INTERNAL:
     * Post acquire unit of work.
     */
    public void postAcquireUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostAcquireUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postAcquireUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post begin transaction.
     */
    public void postBeginTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostBeginTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postBeginTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post commit transaction.
     */
    public void postCommitTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostCommitTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postCommitTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post commit unit of work.
     */
    public void postCommitUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostCommitUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postCommitUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised after connecting.
     */
    public void postConnect(Accessor accessor) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostConnect, getSession());
        event.setResult(accessor);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postConnect(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post execute query.
     */
    public void postExecuteQuery(DatabaseQuery query, Object result) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostExecuteQuery, getSession());
        event.setQuery(query);
        event.setResult(result);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postExecuteQuery(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post release client session.
     */
    public void postReleaseClientSession() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostReleaseClientSession, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postReleaseClientSession(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post release unit of work.
     */
    public void postReleaseUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostReleaseUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postReleaseUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post resume unit of work.
     */
    public void postResumeUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostResumeUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postResumeUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post rollback transaction.
     */
    public void postRollbackTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostRollbackTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postRollbackTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre execute query.
     */
    public void postDistributedMergeUnitOfWorkChangeSet(UnitOfWorkChangeSet changeSet) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostDistributedMergeUnitOfWorkChangeSet, getSession());
        event.setProperty("UnitOfWorkChangeSet", changeSet);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postDistributedMergeUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre execute query.
     */
    public void postMergeUnitOfWorkChangeSet(UnitOfWorkChangeSet changeSet) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostMergeUnitOfWorkChangeSet, getSession());
        event.setProperty("UnitOfWorkChangeSet", changeSet);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postMergeUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre begin transaction.
     */
    public void preBeginTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreBeginTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preBeginTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre calculate UnitOfWork Change Set.
     */
    public void preCalculateUnitOfWorkChangeSet() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreCalculateUnitOfWorkChangeSet, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preCalculateUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Post calculate UnitOfWork Change Set.
     */
    public void postCalculateUnitOfWorkChangeSet(UnitOfWorkChangeSet changeSet) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostCalculateUnitOfWorkChangeSet, getSession());
        event.setProperty("UnitOfWorkChangeSet", changeSet);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postCalculateUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
       * INTERNAL:
       * Pre commit transaction.
       */
    public void preCommitTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreCommitTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preCommitTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre commit unit of work.
     */
    public void preCommitUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreCommitUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preCommitUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre execute query.
     */
    public void preExecuteQuery(DatabaseQuery query) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreExecuteQuery, getSession());
        event.setQuery(query);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preExecuteQuery(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre login to the session.
     */
    public void preLogin(Session session) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreLogin, session);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preLogin(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * post login to the session.
     */
    public void postLogin(Session session) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PostLogin, session);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).postLogin(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Prepare unit of work.
     */
    public void prepareUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PrepareUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).prepareUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre release client session.
     */
    public void preReleaseClientSession() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreReleaseClientSession, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preReleaseClientSession(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Raised before release a connection to a connection pool.
     */
    public void preReleaseConnection(Accessor accessor) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreReleaseConnection, getSession());
        event.setResult(accessor);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preReleaseConnection(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * This event is fired just before a Client Session, with isolated data,
     * releases its Exclusive Connection
     */
    public void preReleaseExclusiveConnection(ClientSession clientSession, Accessor accessor) {
        if (!hasListeners()) {
            return;
        }

        SessionEvent event = new SessionEvent(SessionEvent.PreReleaseExclusiveConnection, clientSession);
        event.setResult(accessor);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preReleaseExclusiveConnection(event);
        }
    }

    /**
     * INTERNAL:
     * Pre release unit of work.
     */
    public void preReleaseUnitOfWork() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreReleaseUnitOfWork, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preReleaseUnitOfWork(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre rollback transaction.
     */
    public void preRollbackTransaction() {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreRollbackTransaction, getSession());
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preRollbackTransaction(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre merge Distributed UnitOfWorkChangeSet
     */
    public void preDistributedMergeUnitOfWorkChangeSet(UnitOfWorkChangeSet changeSet) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreDistributedMergeUnitOfWorkChangeSet, getSession());
        event.setProperty("UnitOfWorkChangeSet", changeSet);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preDistributedMergeUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
     * INTERNAL:
     * Pre merge UnitOfWorkChangeSet
     */
    public void preMergeUnitOfWorkChangeSet(UnitOfWorkChangeSet changeSet) {
        if (!hasListeners()) {
            return;
        }
        startOperationProfile();
        SessionEvent event = new SessionEvent(SessionEvent.PreMergeUnitOfWorkChangeSet, getSession());
        event.setProperty("UnitOfWorkChangeSet", changeSet);
        for (Enumeration listenerEnum = getListeners().elements(); listenerEnum.hasMoreElements();) {
            ((SessionEventListener)listenerEnum.nextElement()).preMergeUnitOfWorkChangeSet(event);
        }
        endOperationProfile();
    }

    /**
     * PUBLIC:
     * Remove the event listener from the session.
     */
    public void removeListener(SessionEventListener listener) {
        getListeners().removeElement(listener);
    }

    /**
     * The event listners will receive all events raised by this session.
     * Also unit of works acquire from this session will inherit the listenrs.
     */
    protected void setListeners(Vector listeners) {
        this.listeners = listeners;
    }

    /**
     * INTERNAL:
     * Set the session for this session event manager
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
       * INTERNAL:
       * Start call
       */
    protected void startOperationProfile() {
        if (getSession().isInProfile()) {
            getSession().getProfiler().startOperationProfile(SessionProfiler.SessionEvent);
        }
    }

    /**
       * INTERNAL:
       * End call
       */
    protected void endOperationProfile() {
        if (getSession().isInProfile()) {
            getSession().getProfiler().endOperationProfile(SessionProfiler.SessionEvent);
        }
    }
}