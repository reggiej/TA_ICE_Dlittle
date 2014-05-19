// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.Vector;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.internal.queryframework.DatabaseQueryMechanism;
import oracle.toplink.internal.queryframework.ExpressionQueryMechanism;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * PUBLIC:
 * Query used to perform a bulk delete using TopLink's expression framework.
 *
 * @author Andrei Ilitchev
 * @date August 18, 2005
 */
public abstract class ModifyAllQuery extends ModifyQuery {

    /** Cache usage flags */
    public static final int NO_CACHE = 0;
    public static final int INVALIDATE_CACHE = 1;

    private int m_cacheUsage = INVALIDATE_CACHE;

    protected Class referenceClass;
    protected String referenceClassName;
    
    /** Number of modified objects */
    protected transient Integer result;

    /** Indicates whether execution should be deferred in UOW */
    private boolean shouldDeferExecutionInUOW;

    /** Provide a default builder so that it's easier to be consistent */
    protected ExpressionBuilder defaultBuilder;
    
    /** Indicates whether the query was prepared so that it will execute using temp storage */
    protected boolean isPreparedUsingTempStorage;
    
    /**
     * PUBLIC:
     */
    public ModifyAllQuery() {
        super();
        shouldDeferExecutionInUOW = true;
    }

    /**
     * PUBLIC:
     * Create a new update all query for the class specified.
     */
    public ModifyAllQuery(Class referenceClass) {
        this();
        setReferenceClass(referenceClass);
    }

    /**
     * PUBLIC:
     * Create a new update all query for the class and the selection criteria
     * specified.
     */
    public ModifyAllQuery(Class referenceClass, Expression selectionCriteria) {
        this();
        setReferenceClass(referenceClass);
        setSelectionCriteria(selectionCriteria);
    }

    /**
     * PUBLIC:
     * Return true if this is a modify query.
     */
    public boolean isModifyQuery() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public void setIsPreparedUsingTempStorage(boolean isPreparedUsingTempStorage) {
        this.isPreparedUsingTempStorage = isPreparedUsingTempStorage;
    }

    /**
     * INTERNAL:
     */
    public boolean isPreparedUsingTempStorage() {
        return isPreparedUsingTempStorage;
    }

    /**
     * INTERNAL
     * Used to give the subclasses oportunity to copy aspects of the cloned query
     * to the original query.  The clones of all the ModifyAllQueries will be added to modifyAllQueries for validation.
     */
    protected void clonedQueryExecutionComplete(DatabaseQuery query, AbstractSession session) {
        super.clonedQueryExecutionComplete(query, session);
        
        if (session.isUnitOfWork()) {
            ((UnitOfWorkImpl)session).storeModifyAllQuery(query);
        }
    }

    /**
     * INTERNAL:
     * Override query execution where Session is a UnitOfWork.
     * <p>
     * If there are objects in the cache return the results of the cache lookup.
     *
     * @param unitOfWork - the session in which the receiver will be executed.
     * @param translationRow - the arguments
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return An object, the result of executing the query.
     */
    public Object executeInUnitOfWork(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        if (unitOfWork.isNestedUnitOfWork()) {
            throw ValidationException.nestedUOWNotSupportedForModifyAllQuery();
        }

        //Bug4607551  For UpdateAllQuery, if deferred, add the original query with a translation row to the deferredUpdateAllQueries for execution.  
        //No action for non-deferred.  Later on the clones of all the UpdateAllQuery's will be added to modifyAllQueries for validation.
        if(shouldDeferExecutionInUOW()) {
            unitOfWork.storeDeferredModifyAllQuery(this, translationRow);
            result = null;
        } else {
            if(!unitOfWork.isInTransaction()) {
                unitOfWork.beginEarlyTransaction();
            }
            unitOfWork.setWasNonObjectLevelModifyQueryExecuted(true);
            result = (Integer)super.executeInUnitOfWork(unitOfWork, translationRow);
        }
        return result;
    }

    /**
     * PUBLIC:
     * Return the cache usage for this query.
     */
    public int getCacheUsage() {
        return m_cacheUsage;
    }

    /**
     * PUBLIC:
     * Get the expression builder which should be used for this query.
     * This expression builder should be used to build all expressions used by this query.
     */
    public ExpressionBuilder getExpressionBuilder() {
        if (defaultBuilder == null) {
            initializeDefaultBuilder();
        }

        return defaultBuilder;
    }
    
    /**
     * INTERNAL
     * Sets the default expression builder for this query.
     */
    public void setExpressionBuilder(ExpressionBuilder builder) {
        this.defaultBuilder = builder;
    }

    /**
     * INTERNAL:
     * Return the name of the reference class of the query.
     * Used by the Mappign Workbench to avoid classpath dependancies
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * PUBLIC:
     * Return the reference class for this query.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Invalid the cache, that is, those objects in the cache that were affected
     * by the query.
     */
    protected void invalidateCache() {
        oracle.toplink.sessions.IdentityMapAccessor identityMapAccessor = getSession().getIdentityMapAccessor();
        if (getSelectionCriteria() == null) {
            // Invalidate the whole class since the user did not specify a where clause
            if(getDescriptor().isChildDescriptor()) {
                Vector collectionToInvalidate = identityMapAccessor.getAllFromIdentityMap(null, getReferenceClass(), getTranslationRow(), null);
                identityMapAccessor.invalidateObjects(collectionToInvalidate);
            } else {
                // if it's either a root class or there is no inheritance just clear the identity map
                identityMapAccessor.invalidateClass(getReferenceClass());
            }
        } else {
            // Invalidate only those objects in the cache that match the selection criteria
            //Bug:4293920, expression parameters were not passed in
            boolean noObjectsModifiedInDb = result != null && result.intValue() == 0;
            try {
                int policy = InMemoryQueryIndirectionPolicy.SHOULD_IGNORE_EXCEPTION_RETURN_CONFORMED;
                if (noObjectsModifiedInDb) {
                    policy = InMemoryQueryIndirectionPolicy.SHOULD_IGNORE_EXCEPTION_RETURN_NOT_CONFORMED;
                }
                Vector collectionToInvalidate = identityMapAccessor.getAllFromIdentityMap(getSelectionCriteria(), getReferenceClass(), getTranslationRow(), policy);
                identityMapAccessor.invalidateObjects(collectionToInvalidate);
            } catch (QueryException ex) {
                if(ex.getErrorCode() == QueryException.CANNOT_CONFORM_EXPRESSION) {
                    // If no objects changed in the db - don't invalidate, ignore.
                    if(!noObjectsModifiedInDb) {
                        // Invalidate the whole class since the expression can't be selected in memory
                        identityMapAccessor.invalidateClass(getReferenceClass());
                    }
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * INTERNAL:
     * After execution we need to merge the changes into the shared cache
     */
    public void mergeChangesIntoSharedCache() {
        if (shouldInvalidateCache()) {
            invalidateCache();
        }
    }

    /**
     * PUBLIC:
     * Set the level of cache support for this query, either NONE or INVALIDATE.
     */
    public void setCacheUsage(int cacheUsage) {
        m_cacheUsage = cacheUsage;
    }

    /**
     * PUBLIC:
     * Set the reference class this query.
     */
    public void setReferenceClass(Class referenceClass) {
        if (this.referenceClass != referenceClass) {
            setIsPrepared(false);
        }
        this.referenceClass = referenceClass;
    }

    /**
     * INTERNAL:
     * Set the class name of the reference class of this query.
     * Used by the Mapping Workbench to avoid classpath dependancies.
     */
    public void setReferenceClassName(String className) {
        referenceClassName = className;
    }
    
    /**
     * PUBLIC:
     * Set a flag indicating whether execution should be deferred in UOW until commit.
     */
    public void setShouldDeferExecutionInUOW(boolean shouldDeferExecutionInUOW) {
        this.shouldDeferExecutionInUOW = shouldDeferExecutionInUOW;
    }
    
    /**
     * PUBLIC:
     * Indicates whether execution should be deferred in UOW until commit.
     */
    public boolean shouldDeferExecutionInUOW() {
        return shouldDeferExecutionInUOW;
    }
    
    /**
     * INTERNAL:
     */
    protected boolean shouldInvalidateCache() {
        return m_cacheUsage == INVALIDATE_CACHE;
    }

    /**
     * INTERNAL:
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder, otherwise
     * generate one and cache it. This helps avoid unnecessary rebuilds.
     */
    protected void initializeDefaultBuilder() {
        initializeQuerySpecificDefaultBuilder();
        if(defaultBuilder == null) {
            defaultBuilder = new ExpressionBuilder();
        }
    }
    
    /**
     * INTERNAL:
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder.
     * If after this method defaultBuilder is still null,
     * then initializeDefaultBuilder method will generate and cache it.
     */
    protected void initializeQuerySpecificDefaultBuilder() {
        DatabaseQueryMechanism mech = getQueryMechanism();
        if (mech.isExpressionQueryMechanism() && ((ExpressionQueryMechanism)mech).getExpressionBuilder() != null) {
            this.defaultBuilder = ((ExpressionQueryMechanism)mech).getExpressionBuilder();
        }
    }
}
