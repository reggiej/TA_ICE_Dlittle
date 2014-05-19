// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.sessions.MergeManager;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.indirection.IndirectContainer;
import oracle.toplink.indirection.ValueHolderInterface;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.mappings.CollectionMapping;
import oracle.toplink.mappings.ForeignReferenceMapping;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all read queries that build objects and potentially manipulate
 * the TopLink cache.
 *
 * <p><b>Description</b>:
 * Contains common behavior for all read queries building objects.
 *
 * @author Gordon Yorke
 * @since TopLink Essentials
 */
public abstract class ObjectBuildingQuery extends ReadQuery {

    /** The class of the target objects to be read from the database. */
    protected Class referenceClass;
    protected String referenceClassName;

    /** Allows for the resulting objects to be refresh with the data from the database. */
    protected boolean shouldRefreshIdentityMapResult;
    protected boolean shouldRefreshRemoteIdentityMapResult;

    /** INTERNAL: for bug 2612601 allow ability not to register results in UOW. */
    protected boolean shouldRegisterResultsInUnitOfWork = true;

    /** CMP only. Allow users to configure whether finder should be executed in a uow or not. */
    protected boolean shouldProcessResultsInUnitOfWork = true;

    /** Used for pessimistic locking. */
    protected ForUpdateClause lockingClause;
    public static final short NO_LOCK = 0;
    public static final short LOCK = 1;
    public static final short LOCK_NOWAIT = 2;
    // allow pessimistic locking policy to be used
    public static final short DEFAULT_LOCK_MODE = -1;

    /**
     * Used to set the read time on objects that use this query.
     * Should be set to the time the query returned from the database.
     */
    protected long executionTime = 0;

    /**
     * Added for Exclusive Connection (VPD) support see accessor for information
     */
    protected boolean shouldUseExclusiveConnection = false;

    /**
     * INTERNAL:  This is the key for accessing unregistered and locked result in the query's properties.
     * The uow and  QueryBaseValueHolder use this property to record amd to retreive the result respectively.
     */
    public static final String LOCK_RESULT_PROPERTY = "LOCK_RESULT";

    /** PERF: Store if the query originally used the default lock mode. */
    protected boolean wasDefaultLockMode = false;
    
    /**
     * INTERNAL:  If primary key is null ObjectBuilder.buildObject returns null
     * in case this flag is set to true (instead of throwing exception).
     */
    protected boolean shouldBuildNullForNullPk;
    
    /**
     * INTERNAL:
     * Initialize the state of the query
     */
    public ObjectBuildingQuery() {
        this.shouldRefreshIdentityMapResult = false;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this query to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        Class referenceClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    referenceClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getReferenceClassName(), true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(getReferenceClassName(), exception.getException());
                }
            } else {
                referenceClass = oracle.toplink.internal.security.PrivilegedAccessHelper.getClassForName(getReferenceClassName(), true, classLoader);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(getReferenceClassName(), exc);
        }
        setReferenceClass(referenceClass);
    };

    /**
     * INTERNAL:
     * Return if this query originally used the default lock mode.
     */
    protected boolean wasDefaultLockMode() {
        return wasDefaultLockMode;
    }
    
    /**
     * INTERNAL:
     * Set if this query originally used the default lock mode.
     */
    protected void setWasDefaultLockMode(boolean wasDefaultLockMode) {
        this.wasDefaultLockMode = wasDefaultLockMode;
    }
    
    /**
     * INTERNAL:
     * Clone the query, including its selection criteria.
     * <p>
     * Normally selection criteria are not cloned here as they are cloned
     * later on during prepare.
     */
    public Object deepClone() {
    	return clone();
    }
        
    /**
     * INTERNAL:
     * Copy all setting from the query.
     * This is used to morph queries from one type to the other.
     * By default this calls prepareFromQuery, but additional properties may be required
     * to be copied as prepareFromQuery only copies properties that affect the SQL.
     */
    public void copyFromQuery(DatabaseQuery query) {
        super.copyFromQuery(query);
        if (query.isObjectBuildingQuery()) {
            ObjectBuildingQuery readQuery = (ObjectBuildingQuery)query;
            this.shouldBuildNullForNullPk = readQuery.shouldBuildNullForNullPk;
            this.shouldProcessResultsInUnitOfWork = readQuery.shouldProcessResultsInUnitOfWork;
            this.shouldRefreshIdentityMapResult = readQuery.shouldRefreshIdentityMapResult;
            this.shouldRefreshRemoteIdentityMapResult = readQuery.shouldRefreshRemoteIdentityMapResult;
            this.shouldRegisterResultsInUnitOfWork = readQuery.shouldRegisterResultsInUnitOfWork;
            this.shouldUseExclusiveConnection = readQuery.shouldUseExclusiveConnection;
        }
    }
    
    /**
     * INTERNAL:
     * Prepare the query from the prepared query.
     * This allows a dynamic query to prepare itself directly from a prepared query instance.
     * This is used in the EJBQL parse cache to allow preparsed queries to be used to prepare
     * dyanmic queries.
     * This only copies over properties that are configured through EJBQL.
     */
    public void prepareFromQuery(DatabaseQuery query) {
        super.prepareFromQuery(query);
        if (query.isObjectBuildingQuery()) {
            ObjectBuildingQuery objectQuery = (ObjectBuildingQuery)query;
            this.referenceClass = objectQuery.referenceClass;
            this.referenceClassName = objectQuery.referenceClassName;
            this.lockingClause = objectQuery.lockingClause;
            this.wasDefaultLockMode = objectQuery.wasDefaultLockMode;
        }
    }
    
    /**
     * PUBLIC:
     * When unset means perform read normally and dont do refresh.
     */
    public void dontRefreshIdentityMapResult() {
        setShouldRefreshIdentityMapResult(false);
    }

    /**
     * PUBLIC:
     * When unset means perform read normally and dont do refresh.
     */
    public void dontRefreshRemoteIdentityMapResult() {
        setShouldRefreshRemoteIdentityMapResult(false);
    }

    /**
     * Return the fetch group set in the query.
     * If a fetch group is not explicitly set in the query, default fetch group optionally defined in the decsiptor
     * would be used, unless the user explicitly calls query.setShouldUseDefaultFetchGroup(false).
     */
    public FetchGroup getFetchGroup() {
        return null;
    }

    /**
     * PUBLIC:
     * Return the current locking mode.
     */
    public short getLockMode() {
        if (lockingClause == null) {
            return DEFAULT_LOCK_MODE;
        } else {
            return lockingClause.getLockMode();
        }
    }

    /**
     * INTERNAL:
     * Return  all of the rows fetched by the query, used for 1-m joining.
     */
    public List getDataResults() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the time this query actually went to the database
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * PUBLIC:
     * Return the reference class of the query.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Return the reference class of the query.
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * Return if partial attributes.
     */
    public boolean hasPartialAttributeExpressions() {
        return false;
    }

    /**
     * PUBLIC:
     * Answers if the query lock mode is known to be LOCK or LOCK_NOWAIT.
     *
     * In the case of DEFAULT_LOCK_MODE and the query reference class being a CMP entity bean,
     * at execution time LOCK, LOCK_NOWAIT, or NO_LOCK will be decided.
     * <p>
     * If a single joined attribute was configured for pessimistic locking then
     * this will return true (after first execution) as the SQL contained a
     * FOR UPDATE OF clause.
     */
    public boolean isLockQuery() {
        return getLockMode() > NO_LOCK;
    }

    /**
     * PUBLIC:
     * Return if this is an object building query.
     */
    public boolean isObjectBuildingQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Answers if we are executing through a UnitOfWork and registering results.
     * This is only ever false if using the conforming without registering
     * feature.
     */
    protected boolean isRegisteringResults() {
        return ((shouldRegisterResultsInUnitOfWork() && getDescriptor().shouldRegisterResultsInUnitOfWork()) || isLockQuery());
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object(s) resulting from the query.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void refreshIdentityMapResult() {
        setShouldRefreshIdentityMapResult(true);
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object(s) resulting from the query.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void refreshRemoteIdentityMapResult() {
        setShouldRefreshRemoteIdentityMapResult(true);
    }

    /**
     * INTERNAL:
     * Constructs the final (registered) object for every individual object
     * queried via a UnitOfWork.
     * <p>
     * Called for every object in a read all, the object in a read object, and
     * every time the next or previous row is retrieved from a cursor.
     * <p>
     * The (conform) without registering feature is implemented here, and may
     * return an original non UnitOfWork registered result.
     * <p>
     * Pessimistically locked objects are tracked here.
     *
     * @return a refreshed UnitOfWork queried object, unwrapped.
     */
    public Object registerIndividualResult(Object result, UnitOfWorkImpl unitOfWork, JoinedAttributeManager joinManager) {
        // PERF: Do not register nor process read-only.
        if (unitOfWork.isClassReadOnly(result.getClass())) {
            // There is an obscure case where they object could be read-only and pessimistic.
            // Record clone if referenced class has pessimistic locking policy.
            recordCloneForPessimisticLocking(result, unitOfWork);
            return result;
        }
        Object clone = null;
        // For bug 2612601 Conforming without registering in Unit Of Work.
        if (!isRegisteringResults()) {
            clone = unitOfWork.getIdentityMapAccessorInstance().getIdentityMapManager().getFromIdentityMap(result);

            // If object not registered do not register it here!  Simply return 
            // the original to the user.
            // Avoid setting clone = original, in case revert(clone) is called.
            if (clone == null) {
                clone = result;
            }
        } else {
            // bug # 3183379 either the object comes from the shared cache and is existing, or
            //it is from a parent unit of work and this unit of work does not need to know if it is new
            //or not.  It will query the parent unit of work to determine newness.
            clone = unitOfWork.registerExistingObject(result);
        }

        // Check for refreshing, require to revert in the unit of work to accomplish a refresh.
        if (shouldRefreshIdentityMapResult()) {
            // Revert only works in the object is in the parent cache, if it is not merge must be used.
            if (unitOfWork.getParent().getIdentityMapAccessor().containsObjectInIdentityMap(clone)) {
                if (shouldCascadeAllParts()) {
                    unitOfWork.deepRevertObject(clone);
                } else if (shouldCascadePrivateParts()) {
                    unitOfWork.revertObject(clone);
                } else if (shouldCascadeByMapping()) {
                    unitOfWork.revertObject(clone, MergeManager.CASCADE_BY_MAPPING);
                } else if (!shouldCascadeParts()) {
                    unitOfWork.shallowRevertObject(clone);
                }
            } else {
                if (shouldCascadeAllParts()) {
                    unitOfWork.deepMergeClone(result);
                } else if (shouldCascadePrivateParts()) {
                    unitOfWork.mergeClone(result);
                } else if (shouldCascadeByMapping()) {
                    unitOfWork.mergeClone(result, MergeManager.CASCADE_BY_MAPPING);
                } else if (!shouldCascadeParts()) {
                    unitOfWork.shallowMergeClone(result);
                }
            }
        }
        //bug 6130550: trigger indirection on the clone where required due to fetch joins on the query
        if (joinManager != null && joinManager.hasJoinedAttributeExpressions()) { 
            triggerJoinExpressions(unitOfWork, joinManager, clone);
        }
        return clone;
    }
    
    /**
     * INTERNAL:
     * Fetch/trigger indirection on the clone passed in, based on join expressions in the joinManager.
     */
    private void triggerJoinExpressions(UnitOfWorkImpl unitOfWork, JoinedAttributeManager joinManager, Object clone){
        List joinExpressions = joinManager.getJoinedAttributeExpressions();
        int size = joinExpressions.size();
        if ( (size==0) || (clone==null) ){
            return;
        }
        ClassDescriptor descriptor = unitOfWork.getDescriptor(clone);
        for (int index = 0; index < size; index++) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(joinManager.getJoinedAttributes().get(index));
            if (mapping !=null){
                Object attributeValue = mapping.getRealAttributeValueFromObject(clone, unitOfWork);
                if (attributeValue != null){
                    if ( mapping.isForeignReferenceMapping() && (((ForeignReferenceMapping)mapping).getIndirectionPolicy().usesTransparentIndirection()) ) {
                        ((IndirectContainer)attributeValue).getValueHolder().getValue();
                    }
                    //recurse through the mapping if the expression's base isn't the base expressionBuilder
                    QueryKeyExpression queryKeyExpression = (QueryKeyExpression)joinExpressions.get(index);
                    if (!queryKeyExpression.getBaseExpression().isExpressionBuilder()){
                        ObjectLevelReadQuery nestedQuery =null;
                        if (joinManager.getJoinedMappingQueryClones()==null){
                            if (joinManager.getJoinedMappingQueries_()!=null){
                                nestedQuery = joinManager.getJoinedMappingQueries_().get(mapping);
                            }
                        }else{
                            nestedQuery = joinManager.getJoinedMappingQueryClones().get(mapping);
                        }

                        if ( (nestedQuery!=null) && (nestedQuery.getJoinedAttributeManager()!=null)){
                            if (!mapping.isCollectionMapping()){
                                triggerJoinExpressions(unitOfWork, nestedQuery.getJoinedAttributeManager(), attributeValue);
                            }else {
                                ContainerPolicy cp = ((CollectionMapping)mapping).getContainerPolicy();
                                Object iterator = cp.iteratorFor(attributeValue);
                                while (cp.hasNext(iterator)){
                                    triggerJoinExpressions(unitOfWork, nestedQuery.getJoinedAttributeManager(), cp.next(iterator, unitOfWork));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Set the the time this query went to the database.
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * PUBLIC:
     * Sets whether this is a pessimistically locking query.
     * <ul>
     * <li>ObjectBuildingQuery.LOCK: SELECT .... FOR UPDATE issued.
     * <li>ObjectBuildingQuery.LOCK_NOWAIT: SELECT .... FOR UPDATE NO WAIT issued.
     * <li>ObjectBuildingQuery.NO_LOCK: no pessimistic locking.
     * <li>ObjectBuildingQuery.DEFAULT_LOCK_MODE (default) and you have a CMP descriptor:
     * fine grained locking will occur.
     * </ul>
     * <p>Fine Grained Locking: On execution the reference class
     * and those of all joined attributes will be checked.  If any of these have a
     * PessimisticLockingPolicy set on their descriptor, they will be locked in a
     * SELECT ... FOR UPDATE OF ... {NO WAIT}.  Issues fewer locks
     * and avoids setting the lock mode on each query.
     * <p>Example:<code>readAllQuery.setSelectionCriteria(employee.get("address").equal("Ottawa"));</code>
     * <ul><li>LOCK: all employees in Ottawa and all referenced Ottawa addresses will be locked.
     * <li>DEFAULT_LOCK_MODE: if address is a joined attribute, and only address has a pessimistic
     * locking policy, only referenced Ottawa addresses will be locked.
     * </ul>
     * @see oracle.toplink.descriptors.PessimisticLockingPolicy
     */
    public void setLockMode(short lockMode) {
        lockingClause = ForUpdateClause.newInstance(lockMode);
    }

    /**
     * REQUIRED:
     * Set the reference class for the query.
     */
    public void setReferenceClass(Class aClass) {
        referenceClass = aClass;
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Set the reference class for the query.
     */
    public void setReferenceClassName(String aClass) {
        referenceClassName = aClass;
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Set if the attributes of the object(s) resulting from the query should be refreshed.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void setShouldRefreshIdentityMapResult(boolean shouldRefreshIdentityMapResult) {
        this.shouldRefreshIdentityMapResult = shouldRefreshIdentityMapResult;
        if (shouldRefreshIdentityMapResult) {
            setShouldRefreshRemoteIdentityMapResult(true);
        }
    }

    /**
     * PUBLIC:
     * Set if the attributes of the object(s) resulting from the query should be refreshed.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void setShouldRefreshRemoteIdentityMapResult(boolean shouldRefreshIdentityMapResult) {
        this.shouldRefreshRemoteIdentityMapResult = shouldRefreshIdentityMapResult;
    }

    /**
     * INTERNAL:
     * Set to false to have queries conform to a UnitOfWork without registering
     * any additional objects not already in that UnitOfWork.
     * @see #shouldRegisterResultsInUnitOfWork
     * @bug 2612601
     */
    public void setShouldRegisterResultsInUnitOfWork(boolean shouldRegisterResultsInUnitOfWork) {
        this.shouldRegisterResultsInUnitOfWork = shouldRegisterResultsInUnitOfWork;
    }

    /**
     * ADVANCED:
     * If the user has isolated data and specified that the client session should
     * use an exclusive connection then by setting this condition to true
     * TopLink will ensure that the query is executed through the exclusive
     * connection.  This may be required in certain cases.  An example being
     * where database security will prevent a query joining to a secure table
     * from returning the correct results when executed through the shared
     * connection.
     */
    public void setShouldUseExclusiveConnection(boolean shouldUseExclusiveConnection) {
        this.shouldUseExclusiveConnection = shouldUseExclusiveConnection;
    }

    /**
     * INTERNAL:
     * Allows one to do conforming in a UnitOfWork without registering.
     * Queries executed on a UnitOfWork will only return working copies for objects
     * that have already been registered.
     * <p>Extreme care should be taken in using this feature, for a user will
     * get back a mix of registered and original (unregistered) objects.
     * <p>Best used with a WrapperPolicy where invoking on an object will trigger
     * its registration (CMP).  Without a WrapperPolicy {@link oracle.toplink.sessions.UnitOfWork#registerExistingObject registerExistingObject}
     * should be called on any object that you intend to change.
     * @return true by default.
     * @see #setShouldRegisterResultsInUnitOfWork
     * @bug 2612601
     */
    public boolean shouldRegisterResultsInUnitOfWork() {
        return shouldRegisterResultsInUnitOfWork;
    }

    /**
     * ADVANCED:
     * If the user has isolated data and specified that the client session should
     * use an exclusive connection then by setting this condition to true
     * TopLink will ensure that the query is executed through the exclusive
     * connection.  This may be required in certain cases.  An example being
     * where database security will prevent a query joining to a secure table
     * from returning the correct results when executed through the shared
     * connection.
     */
    public boolean shouldUseExclusiveConnection() {
        return this.shouldUseExclusiveConnection;
    }

    /**
     * INTERNAL:
     * Return if this is a full object query, not partial nor fetch group.
     */
    public boolean shouldReadAllMappings() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Check if the mapping is part of the partial attributes.
     */
    public boolean shouldReadMapping(DatabaseMapping mapping) {
        return true;
    }

    /**
     * PUBLIC:
     * Set to a boolean. When set means refresh the instance
     * variables of referenceObject from the database.
     */
    public boolean shouldRefreshIdentityMapResult() {
        return shouldRefreshIdentityMapResult;
    }

    /**
     * PUBLIC:
     * Set to a boolean. When set means refresh the instance
     * variables of referenceObject from the database.
     */
    public boolean shouldRefreshRemoteIdentityMapResult() {
        return shouldRefreshRemoteIdentityMapResult;
    }

    public String toString() {
        if (getReferenceClass() == null) {
            return super.toString();
        }
        return Helper.getShortClassName(getClass()) + "(" + getReferenceClass().getName() + ")";
    }

    /**
     * ADVANCED:
     * Used for CMP only.  This allows users to indicate whether cmp finders executed
     * at the beginning of a transaction should always be run against a UnitOfWork.
     * Defaults to true.
     * <p>
     * If set to false, then UnitOfWork allocation will be deferred until a business
     * method (including creates/removes) or finder with shouldProcessResultsInUnitOfWork == true
     * is invoked.  Any finder executed before such a time, will do so against the
     * underlying ServerSession.  Forcing finder execution to always go through a
     * UnitOfWork means the results will be cloned and cached in the UnitOfWork up
     * front.  This is desired when the results will be accessed in the same transaction.
     * <p>
     * Note that finders executed with an unspecified transaction context will never
     * be executed against a UnitOfWork, even if this setting is true.  This case may happen
     * with the NotSupported, Never, and Supports attributes.
     */
    public void setShouldProcessResultsInUnitOfWork(boolean processResultsInUnitOfWork) {
        this.shouldProcessResultsInUnitOfWork = processResultsInUnitOfWork;
    }

    /**
     * ADVANCED:
     * Used for CMP only.  Indicates whether cmp finders executed at the beginning
     * of a transaction should always be run against a UnitOfWork.
     * Defaults to true.
     * <p>
     * If set to false, then UnitOfWork allocation will be deferred until a business
     * method (including creates/removes) or finder with shouldProcessResultsInUnitOfWork == true
     * is invoked.  Any finder executed before such a time, will do so against the
     * underlying ServerSession.  Forcing finder execution to always go through a
     * UnitOfWork means the results will be cloned and cached in the UnitOfWork up
     * front.  This is desired when the results will be accessed in the same transaction.
     * <p>
     * Note that finders executed with an unspecified transaction context will never
     * be executed against a UnitOfWork, even if this setting is true.  This case may happen
     * with the NotSupported, Never, and Supports attributes.
     */
    public boolean shouldProcessResultsInUnitOfWork() {
        return this.shouldProcessResultsInUnitOfWork;
    }

    /**
     * INTERNAL:
     * Return if the attribute is specified for joining.
     */
    public boolean isAttributeJoined(ClassDescriptor mappingDescriptor, String attributeName) {
        return false;
    }

    /**
    * INTERNAL:
    * Helper method that checks if clone has been locked with uow.
    */
    public boolean isClonePessimisticLocked(Object clone, UnitOfWorkImpl uow) {
        return false;
    }

    /**
     * INTERNAL:
     * Helper method that records clone with uow if query is pessimistic locking.
     */
    public void recordCloneForPessimisticLocking(Object clone, UnitOfWorkImpl uow) {
        if ((isLockQuery()) && lockingClause.isReferenceClassLocked()) {
            uow.addPessimisticLockedClone(clone);
        }
    }

    /**
    * INTERNAL: Helper method to determine the default mode. If true and quey has a pessimistic locking policy,
    * locking will be configured according to the pessimistic locking policy.
    */
    public boolean isDefaultLock() {
        return (lockingClause == null);
    }

    /**
     * INTERNAL:  
     * If primary key is null ObjectBuilder.buildObject returns null
     * in case this flag is set to true (instead of throwing exception).
     */
    public boolean shouldBuildNullForNullPk() {
        return shouldBuildNullForNullPk;
    }

    /**
     * INTERNAL:  
     * If primary key is null ObjectBuilder.buildObject returns null
     * in case this flag is set to true (instead of throwing exception).
     */
    public void setShouldBuildNullForNullPk(boolean shouldBuildNullForNullPk) {
        this.shouldBuildNullForNullPk = shouldBuildNullForNullPk;
    }
}
