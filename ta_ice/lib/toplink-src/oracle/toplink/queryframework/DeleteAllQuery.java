// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.descriptors.DescriptorEvent;
import oracle.toplink.descriptors.DescriptorEventManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>:
 * Query used to delete a collection of objects
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Stores & retrieves the objects to delete.
 * <li> Store the where clause used for the deletion.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DeleteAllQuery extends ModifyAllQuery {

    /* Vector containing objects to be deleted, these should be removed from the identity map after deletion. */
    protected Vector objects;

    /**
     * PUBLIC:
     */
    public DeleteAllQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Create a new delete all query for the class specified.
     */
    public DeleteAllQuery(Class referenceClass) {
        super(referenceClass);
    }

    /**
     * PUBLIC:
     * Create a new delete all query for the class and the selection criteria
     * specified.
     */
    public DeleteAllQuery(Class referenceClass, Expression selectionCriteria) {
        super(referenceClass, selectionCriteria);
    }

    /**
     * PUBLIC:
     * Return if this is a delete all query.
     */
    public boolean isDeleteAllQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * This method has to be broken.  If commit manager is not active either
     * an exception should be thrown (ObjectLevelModify case), or a transaction
     * should be started early and execute on parent if remote (dataModify case).
     * A modify query is NEVER executed on the parent, unless remote session.
     * @param unitOfWork
     * @param translationRow
     * @return
     * @throws oracle.toplink.essentials.exceptions.DatabaseException
     * @throws oracle.toplink.essentials.exceptions.OptimisticLockException
     */
    public Object executeInUnitOfWork(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        if (getObjects() != null) {
            if (unitOfWork.isAfterWriteChangesButBeforeCommit()) {
                throw ValidationException.illegalOperationForUnitOfWorkLifecycle(unitOfWork.getLifecycle(), "executeQuery(DeleteAllQuery)");
            }
    
            // This must be broken, see comment.
            if (!unitOfWork.getCommitManager().isActive()) {
                return unitOfWork.getParent().executeQuery(this, translationRow);
            }
            result = (Integer)super.execute(unitOfWork, translationRow);
            return result;
        } else {
            return super.executeInUnitOfWork(unitOfWork, translationRow);
        }
    }

    /**
     * INTERNAL:
     * Perform the work to delete a collection of objects.
     * This skips the optimistic lock check and should not called for objects using locking.
     * @exception  DatabaseException - an error has occurred on the database.
     * @return Integer the number of objects (rows) deleted.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        // CR# 4286
        if (getObjects() != null) {

            if(isExpressionQuery() && getSelectionCriteria() == null) {
                // DeleteAllQuery has objects so it *must* have selectionCriteria, too
                throw QueryException.deleteAllQuerySpecifiesObjectsButNotSelectionCriteria(getDescriptor(), this, getObjects().toString());
            }
            
            // Optimistic lock check not required because objects are deleted individually in that case.
            try {
                getSession().beginTransaction();
    
                // Need to run pre-delete selector if available.
                // PERF: Avoid events if no listeners.
                if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                    for (Enumeration deletedObjectsEnum = getObjects().elements();
                             deletedObjectsEnum.hasMoreElements();) {
                        DescriptorEvent event = new DescriptorEvent(deletedObjectsEnum.nextElement());
                        event.setEventCode(DescriptorEventManager.PreDeleteEvent);
                        event.setSession(getSession());
                        event.setQuery(this);
                        getDescriptor().getEventManager().executeEvent(event);
                    }
                }
    
                result = getQueryMechanism().deleteAll();
    
                // Need to run post-delete selector if available.
                // PERF: Avoid events if no listeners.
                if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                    for (Enumeration deletedObjectsEnum = getObjects().elements();
                             deletedObjectsEnum.hasMoreElements();) {
                        DescriptorEvent event = new DescriptorEvent(deletedObjectsEnum.nextElement());
                        event.setEventCode(DescriptorEventManager.PostDeleteEvent);
                        event.setSession(getSession());
                        event.setQuery(this);
                        getDescriptor().getEventManager().executeEvent(event);
                    }
                }
    
                if (shouldMaintainCache()) {
                    // remove from the cache.
                    for (Enumeration objectsEnum = getObjects().elements();
                             objectsEnum.hasMoreElements();) {
                        Object deleted = objectsEnum.nextElement();
                        if (getSession().isUnitOfWork()) {
                            //BUG #2612169: Unwrap is needed
                            deleted = getDescriptor().getObjectBuilder().unwrapObject(deleted, getSession());
                            ((UnitOfWorkImpl)getSession()).addObjectDeletedDuringCommit(deleted, getDescriptor());
                        } else {
                            getSession().getIdentityMapAccessor().removeFromIdentityMap(deleted);
                        }
                    }
                }
    
                getSession().commitTransaction();
    
            } catch (RuntimeException exception) {
                getSession().rollbackTransaction();
                throw exception;
            }
        } else {
            result = getQueryMechanism().deleteAll();// fire the SQL to the database
            mergeChangesIntoSharedCache();
        }
        
        return result;
    }

    /**
     * INTERNAL:
     * Delete all queries are executed specially to avoid cloning and ensure preparing.
     */
    public void executeDeleteAll(AbstractSession session, AbstractRecord translationRow, Vector objects) throws DatabaseException {
        this.checkPrepare(session, translationRow);
        DeleteAllQuery queryToExecute = (DeleteAllQuery)clone();

        // Then prapared for the single execution.
        queryToExecute.setTranslationRow(translationRow);
        queryToExecute.setSession(session);
        queryToExecute.setObjects(objects);
        queryToExecute.prepareForExecution();
        queryToExecute.executeDatabaseQuery();
    }

    /**
     * PUBLIC:
     * Return the objects that are to be deleted
     */
    public Vector getObjects() {
        return objects;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() throws QueryException {
        super.prepare();

        if (getReferenceClass() == null) {
            throw QueryException.referenceClassMissing(this);
        }

        if (getDescriptor() == null) {
            ClassDescriptor referenceDescriptor = getSession().getDescriptor(getReferenceClass());
            if (referenceDescriptor == null) {
                throw QueryException.descriptorIsMissing(getReferenceClass(), this);
            }
            setDescriptor(referenceDescriptor);
        }

        if (getDescriptor().isAggregateDescriptor()) {
            throw QueryException.aggregateObjectCannotBeDeletedOrWritten(getDescriptor(), this);
        }

        getQueryMechanism().prepareDeleteAll();
    }

    /**
     * PUBLIC (REQUIRED):
     * Set the objects to be deleted.
     * Also REQUIRED is a selection criteria or SQL string that performs the deletion of the objects.
     * This does not generate the SQL call from the deleted objects.
     * #setObject() should not be called.
     * 
     * Vector objects used as an indicator of one of two possible
     * ways the query may behave:
     *   objects != null - the "old" functionality used by OneToMany mapping
     *     objects deleted from the cache, either selection expression or custom sql
     *     should be provided for deletion from db;
     *   objects == null - the "new" functionality (on par with UpdateAllQuery)
     *     the cache is either left alone or in-memory query finds the cached objects to be deleted,
     *       and these objects are invalidated in cache.
     *   
     *   Note that empty objects is still objects != case.
     *     Signal that no cache altering is required.
     *     Used by AggregationCollectionMapping and OneToManyMapping in case they use indirection
     *       and the ValueHolder has not been instantiated.
     */
    public void setObjects(Vector objectCollection) {
        objects = objectCollection;
    }
}
