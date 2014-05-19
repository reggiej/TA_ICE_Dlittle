// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.mappings;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.history.*;
import oracle.toplink.indirection.*;
import oracle.toplink.internal.descriptors.*;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.identitymaps.CacheKey;
import oracle.toplink.internal.indirection.*;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.remote.*;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.sessions.*;
//import oracle.toplink.internal.weaving.ClassWeaver;
import oracle.toplink.queryframework.*;
import oracle.toplink.remote.*;
import oracle.toplink.sessions.DatabaseRecord;

/**
 * <b>Purpose</b>: Abstract class for relationship mappings
 */
public abstract class ForeignReferenceMapping extends DatabaseMapping {

    /** This is used only in descriptor proxy in remote session */
    protected Class referenceClass;
    protected String referenceClassName;

    /** The session is temporarily used for initialization. Once used, it is set to null */
    protected transient AbstractSession tempInitSession;

    /** The descriptor of the reference class. */
    protected transient ClassDescriptor referenceDescriptor;

    /** This query is used to read referenced objects for this mapping. */
    protected transient ReadQuery selectionQuery;

    /** Indicates whether the referenced object is privately owned or not. */
    protected boolean isPrivateOwned;

    /** Indicates whether the referenced object should always be batch read on read all queries. */
    protected boolean usesBatchReading;

    /** Implements indirection behavior */
    protected IndirectionPolicy indirectionPolicy;

    /** Indicates whether the selection query is TopLink generated or defined by the user. */
    protected transient boolean hasCustomSelectionQuery;

    /** Used to reference the other half of a bi-directional relationship. */
    protected DatabaseMapping relationshipPartner;

    /** Set by users, used to retreive the backpointer for this mapping */
    protected String relationshipPartnerAttributeName;

    /** Cascading flags used by the EntityManager */
    protected boolean cascadePersist;
    protected boolean cascadeMerge;
    protected boolean cascadeRefresh;
    protected boolean cascadeRemove;
    
    /** Define if the relationship should always be join fetched. */
    protected int joinFetch = NONE;
    /** Specify any INNER join on a join fetch. */
    public static final int INNER_JOIN = 1;
    /** Specify any OUTER join on a join fetch. */
    public static final int OUTER_JOIN = 2;
    /** Specify no join fetch, this is the default. */
    public static final int NONE = 0;

    protected ForeignReferenceMapping() {
        this.isPrivateOwned = false;
        this.hasCustomSelectionQuery = false;
        this.usesBatchReading = false;
        this.useBasicIndirection();
        this.cascadePersist = false;
        this.cascadeMerge = false;
        this.cascadeRefresh = false;
        this.cascadeRemove = false;
    }

    /**
     * INTERNAL:
     * Retreive the value through using batch reading.
     * This executes a single query to read the target for all of the objects and stores the
     * result of the batch query in the original query to allow the other objects to share the results.
     */
    protected Object batchedValueFromRow(AbstractRecord row, ReadAllQuery query) {
        ReadQuery batchQuery = (ReadQuery)query.getProperty(this);
        if (batchQuery == null) {
            if (query.getBatchReadMappingQueries() != null) {
                batchQuery = (ReadQuery)query.getBatchReadMappingQueries().get(this);
            }
            if (batchQuery == null) {
                batchQuery = prepareNestedBatchQuery(query);
                batchQuery.setIsExecutionClone(true);
            } else {
                batchQuery = (ReadQuery)batchQuery.clone();
                batchQuery.setIsExecutionClone(true);
            }
            query.setProperty(this, batchQuery);
        }
        return getIndirectionPolicy().valueFromBatchQuery(batchQuery, row, query);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(clone);
        Object clonedAttributeValue = getIndirectionPolicy().backupCloneAttribute(attributeValue, clone, backup, unitOfWork);
        setAttributeValueInObject(backup, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * Used during building the backup shallow copy to copy the
     * target object without re-registering it.
     */
    public abstract Object buildBackupCloneForPartObject(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork);

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(original);
        Object clonedAttributeValue = getIndirectionPolicy().cloneAttribute(attributeValue, original, clone, unitOfWork, false); // building clone from an original not a row.
        //GFBug#404 - fix moved to ObjectBuildingQuery.registerIndividualResult 
        setAttributeValueInObject(clone, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * A combination of readFromRowIntoObject and buildClone.
     * <p>
     * buildClone assumes the attribute value exists on the original and can
     * simply be copied.
     * <p>
     * readFromRowIntoObject assumes that one is building an original.
     * <p>
     * Both of the above assumptions are false in this method, and actually
     * attempts to do both at the same time.
     * <p>
     * Extract value from the row and set the attribute to this value in the
     * working copy clone.
     * In order to bypass the shared cache when in transaction a UnitOfWork must
     * be able to populate working copies directly from the row.
     */
    public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        Object attributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);
        Object clonedAttributeValue = getIndirectionPolicy().cloneAttribute(attributeValue, null,// no original
                                                                            clone, unitOfWork, true);// building clone directly from row.
        setAttributeValueInObject(clone, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     */
    public abstract Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting);

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        ForeignReferenceMapping clone = (ForeignReferenceMapping)super.clone();

        clone.setIndirectionPolicy((IndirectionPolicy)indirectionPolicy.clone());
        clone.setSelectionQuery((ReadQuery)getSelectionQuery().clone());

        return clone;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        if (isPrivateOwned()) {
            return compareObjectsWithPrivateOwned(firstObject, secondObject, session);
        } else {
            return compareObjectsWithoutPrivateOwned(firstObject, secondObject, session);
        }
    }

    /**
     * Compare two objects if their parts are not private owned
     */
    protected abstract boolean compareObjectsWithoutPrivateOwned(Object first, Object second, AbstractSession session);

    /**
     * Compare two objects if their parts are private owned
     */
    protected abstract boolean compareObjectsWithPrivateOwned(Object first, Object second, AbstractSession session);

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);

        // DirectCollection mappings don't require a reference class.
        if (getReferenceClassName() != null) {
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
        }
    }

    /**
     * INTERNAL:
     * Builder the unit of work value holder.
     * Ignore the original object.
     * @param buildDirectlyFromRow indicates that we are building the clone directly
     * from a row as opposed to building the original from the row, putting it in
     * the shared cache, and then cloning the original.
     */
    public UnitOfWorkValueHolder createUnitOfWorkValueHolder(ValueHolderInterface attributeValue, Object original, Object clone, AbstractRecord row, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        return new UnitOfWorkQueryValueHolder(attributeValue, clone, this, row, unitOfWork);
    }

    /**
     * INTERNAL:
     * Return true if the merge should be bypassed. This would be the case for several reasons, depending on
     * the kind of merge taking place.
     */
    protected boolean dontDoMerge(Object target, Object source, MergeManager mergeManager) {
        if (!shouldMergeCascadeReference(mergeManager)) {
            return true;
        }
        if (mergeManager.shouldMergeOriginalIntoWorkingCopy()) {
            // For reverts we are more concerned about the target than the source.
            if (!isAttributeValueInstantiated(target)) {
                return true;
            }
        } else {
            if (mergeManager.shouldRefreshRemoteObject() && shouldMergeCascadeParts(mergeManager) && usesIndirection()) {
                return true;
            } else {
                if (!isAttributeValueInstantiated(source)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be batch read on read all queries.
     * Batch reading will read all of the related objects in a single query when accessed from an originating read all.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void dontUseBatchReading() {
        setUsesBatchReading(false);
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public void dontUseIndirection() {
        setIndirectionPolicy(new NoIndirectionPolicy());
    }

    /**
     * INTERNAL:
     * Extract the value from the batch optimized query, this should be supported by most query types.
     */
    public Object extractResultFromBatchQuery(DatabaseQuery query, AbstractRecord Record, AbstractSession session, AbstractRecord argumentRow) throws QueryException {
        throw QueryException.batchReadingNotSupported(this, query);
    }

    /**
     * INTERNAL:
     * Clone and prepare the JoinedAttributeManager nested JoinedAttributeManager.
     * This is used for nested joining as the JoinedAttributeManager passed to the joined build object.
     */
    public ObjectLevelReadQuery prepareNestedJoins(JoinedAttributeManager joinManager, ObjectBuildingQuery baseQuery, AbstractSession session) {
        // A nested query must be built to pass to the descriptor that looks like the real query execution would.
        ObjectLevelReadQuery nestedQuery = (ObjectLevelReadQuery)((ObjectLevelReadQuery)getSelectionQuery()).deepClone();
        nestedQuery.setSession(session); 
        // Must cascade for nested partial/join attributes, the expressions must be filter to only the nested ones.
        if (baseQuery.hasPartialAttributeExpressions()) {
            nestedQuery.setPartialAttributeExpressions(extractNestedExpressions(((ObjectLevelReadQuery)baseQuery).getPartialAttributeExpressions(), nestedQuery.getExpressionBuilder(), false));
            // bug 5501751: USING GETALLOWINGNULL() WITH ADDPARTIALATTRIBUTE() BROKEN IN 10.1.3
            // The query against Employee with 
            //   query.addPartialAttribute(builder.getAllowingNull("address"));
            // in case there's no address returns null instead of Address object.
            // Note that in case 
            //   query.addPartialAttribute(builder.getAllowingNull("address").get("city"));
            // in case there's no address an empty Address object (all atributes are nulls) is returned.
            if(nestedQuery.getPartialAttributeExpressions().isEmpty()) {
                if(hasRootExpressionThatShouldUseOuterJoin(((ObjectLevelReadQuery)baseQuery).getPartialAttributeExpressions())) {
                    nestedQuery.setShouldBuildNullForNullPk(true);
                }
            }
        } else {
            List nestedJoins = extractNestedExpressions(joinManager.getJoinedAttributeExpressions(), nestedQuery.getExpressionBuilder(), false);
            if (nestedJoins.size() > 0) {
                // Recompute the joined indexes based on the nested join expressions.
                nestedQuery.getJoinedAttributeManager().clear();
                nestedQuery.getJoinedAttributeManager().setJoinedAttributeExpressions_(nestedJoins);
                // the next line sets isToManyJoinQuery flag
                nestedQuery.getJoinedAttributeManager().prepareJoinExpressions(session);
                nestedQuery.getJoinedAttributeManager().computeJoiningMappingIndexes(true, session, 0);
            } else if (nestedQuery.hasJoining()) {
                // Clear any mapping level joins.
                nestedQuery.setJoinedAttributeManager(null);
            }
            // Configure nested locking clause.
            if (baseQuery.isLockQuery()) {
                if (((ObjectLevelReadQuery)baseQuery).getLockingClause().isForUpdateOfClause()) {
                    ForUpdateOfClause clause = (ForUpdateOfClause)((ObjectLevelReadQuery)baseQuery).getLockingClause().clone();
                    clause.setLockedExpressions(extractNestedExpressions(clause.getLockedExpressions(), nestedQuery.getExpressionBuilder(), true));
                    nestedQuery.setLockingClause(clause);
                } else {
                    nestedQuery.setLockingClause(((ObjectLevelReadQuery)baseQuery).getLockingClause());
                }
            }
        }
        nestedQuery.setShouldMaintainCache(baseQuery.shouldMaintainCache());
        nestedQuery.setShouldRefreshIdentityMapResult(baseQuery.shouldRefreshIdentityMapResult());

        // For flashback: Must still propogate all properties, as the
        // attributes of this joined attribute may be read later too.
        if (baseQuery.isObjectLevelReadQuery() && ((ObjectLevelReadQuery)baseQuery).hasAsOfClause()) {
            nestedQuery.setAsOfClause(((ObjectLevelReadQuery)baseQuery).getAsOfClause());
        }
        nestedQuery.setCascadePolicy(baseQuery.getCascadePolicy());
        if (nestedQuery.hasJoining()) {
            nestedQuery.getJoinedAttributeManager().computeJoiningMappingQueries(session);
        }
        nestedQuery.setSession(null);

        return nestedQuery;
    }

    /**
     * INTERNAL:
     * Allow the mapping the do any further batch preparation.
     */
    protected void postPrepareNestedBatchQuery(ReadQuery batchQuery, ReadAllQuery query) {
        // Do nothing.
    }
    
    /**
     * INTERNAL:
     * Clone and prepare the selection query as a nested batch read query.
     * This is used for nested batch reading.
     */
    public ReadQuery prepareNestedBatchQuery(ReadAllQuery query) {
        ReadAllQuery batchQuery = new ReadAllQuery();
        batchQuery.setReferenceClass(getReferenceClass());
        batchQuery.setSession(query.getSession());

        //bug 3965568 
        // we should not wrap the results as this is an internal query
        batchQuery.setShouldUseWrapperPolicy(false);
        if (query.shouldCascadeAllParts() || (query.shouldCascadePrivateParts() && isPrivateOwned()) || (query.shouldCascadeByMapping() && this.cascadeRefresh)) {
            batchQuery.setShouldRefreshIdentityMapResult(query.shouldRefreshIdentityMapResult());
            batchQuery.setCascadePolicy(query.getCascadePolicy());
            batchQuery.setShouldMaintainCache(query.shouldMaintainCache());
            if (query.hasAsOfClause()) {
                batchQuery.setAsOfClause(query.getAsOfClause());
            }

            //bug 3802197 - cascade binding and prepare settings
            batchQuery.setShouldBindAllParameters(query.getShouldBindAllParameters());
            batchQuery.setShouldPrepare(query.shouldPrepare());
        }
        //CR #4365
        batchQuery.setQueryId(query.getQueryId());

        // For CR#2646-S.M.  In case of inheritance the descriptor to use may not be that
        // of the source query (the base class descriptor), but that of the subclass, if the
        // attribute is only of the subclass.  Thus in this case use the descriptor from the mapping.
        // Also: for Bug 5478648 - Do not switch the descriptor if the query's descriptor is an aggregate
        ClassDescriptor descriptorToUse = query.getDescriptor();
        if ((descriptorToUse != getDescriptor()) && (!descriptorToUse.getMappings().contains(this)) && (!getDescriptor().isAggregateDescriptor())) { 
            descriptorToUse = getDescriptor();
        }

        // Join the query where clause with the mapping's,
        // this will cause a join that should bring in all of the target objects.
        ExpressionBuilder builder = batchQuery.getExpressionBuilder();
        Expression backRef = builder.getManualQueryKey(getAttributeName() + "-back-ref", descriptorToUse);
        Expression twisted = backRef.twist(getSelectionCriteria(), builder);
        if (query.getSelectionCriteria() != null) {
            // For bug 2612567, any query can have batch attributes, so the
            // original selection criteria can be quite complex, with multiple
            // builders (i.e. for parallel selects).
            // Now uses cloneUsing(newBase) instead of rebuildOn(newBase).
            twisted = twisted.and(query.getSelectionCriteria().cloneUsing(backRef));
        }
        // Since a manual query key expression does not really get normalized,
        // it must get its additional expressions added in here.  Probably best
        // to somehow keep all this code inside QueryKeyExpression.normalize.
        if (descriptorToUse.getQueryManager().getAdditionalJoinExpression() != null) {
            twisted = twisted.and(descriptorToUse.getQueryManager().getAdditionalJoinExpression().rebuildOn(backRef));
        }
        // Check for history and add history expression. 
        if (descriptorToUse.getHistoryPolicy() != null) {
            if (query.getSession().getAsOfClause() != null) {
                backRef.asOf(query.getSession().getAsOfClause());
            } else if (batchQuery.getAsOfClause() == null) {
                backRef.asOf(AsOfClause.NO_CLAUSE);
            } else {
                backRef.asOf(batchQuery.getAsOfClause());
            }
            twisted = twisted.and(descriptorToUse.getHistoryPolicy().additionalHistoryExpression((ObjectExpression)backRef));
        }
        batchQuery.setSelectionCriteria(twisted);
        if (query.isDistinctComputed()) {
            // Only recompute if it has not already been set by the user
            batchQuery.setDistinctState(query.getDistinctState());
        }

        // Add batch reading attributes contained in the mapping's query.
        ReadQuery mappingQuery = this.getSelectionQuery();
        if (mappingQuery.isReadAllQuery()) {
            // CR#3238 clone these vectors so they will not grow with each call to the query. -TW
            batchQuery.setOrderByExpressions(((Vector)((ReadAllQuery)mappingQuery).getOrderByExpressions().clone()));
            for (Enumeration enumtr = ((ReadAllQuery)mappingQuery).getBatchReadAttributeExpressions().elements(); enumtr.hasMoreElements();) {
                Expression expression = ((Expression)enumtr.nextElement()).rebuildOn(batchQuery.getExpressionBuilder());
                batchQuery.addBatchReadAttribute(expression);
            }
        }

        // Computed nested batch attribute expressions.
        Vector nestedExpressions = extractNestedExpressions(query.getBatchReadAttributeExpressions(), batchQuery.getExpressionBuilder(), false);
        Helper.addAllToVector(batchQuery.getBatchReadAttributeExpressions(), nestedExpressions);
        // Allow subclasses to further prepare.
        postPrepareNestedBatchQuery(batchQuery, query);
        
        if (batchQuery.shouldPrepare()) {
            batchQuery.checkPrepare(query.getSession(), query.getTranslationRow());
        }
        batchQuery.setSession(null);

        return batchQuery;
    }
    
    /**
     * INTERNAL:
     * The hashtable of batched objects resides in one of two places.
     * In the normal case it is stored in a query property.
     * When a UnitOfWork is in transaction, it executes the batch query
     * on the UnitOfWork and stores the clones on the UnitOfWork.
     */
    protected Hashtable getBatchReadObjects(DatabaseQuery query, AbstractSession session) {
        if (session.isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)session;
            return (Hashtable)unitOfWork.getBatchReadObjects().get(query);
        } else {
            return (Hashtable)query.getProperty("batched objects");
        }
    }

    /**
     * INTERNAL:
     * The hashtable of batched objects resides in one of two places.
     * In the normal case it is stored in a query property.
     * When a UnitOfWork is in transaction, it executes the batch query
     * on the UnitOfWork and stores the clones on the UnitOfWork.
     */
    protected void setBatchReadObjects(Hashtable referenceObjectsByKey, DatabaseQuery query, AbstractSession session) {
        if (session.isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)session;
            unitOfWork.getBatchReadObjects().put(query, referenceObjectsByKey);
        } else {
            query.setProperty("batched objects", referenceObjectsByKey);
        }
    }

    /**
     * INTERNAL:
     * An object has been serialized from the server to the client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    public void fixObjectReferences(Object object, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
        getIndirectionPolicy().fixObjectReferences(object, objectDescriptors, processedObjects, query, session);
    }

    /**
     * INTERNAL:
     * Return the value of an attribute which this mapping represents for an object.
     */
    public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        Object attributeValue = super.getAttributeValueFromObject(object);
        Object indirectionValue = getIndirectionPolicy().validateAttributeOfInstantiatedObject(attributeValue);

        // PERF: Allow the indirection policy to initialize null attribute values,
        // this allows the indirection objects to not be initialized in the constructor.
        if (indirectionValue != attributeValue) {
            setAttributeValueInObject(object, indirectionValue);
            attributeValue = indirectionValue;
        }
        return attributeValue;
    }
    
    /**
     * INTERNAL:
     * Returns the attribute value from the reference object.
     * If the attribute is using indirection the value of the value-holder is returned.
     * If the value holder is not instantiated then it is instantiated.
     */
    public Object getAttributeValueWithClonedValueHolders(Object object) {
        Object attributeValue = getAttributeValueFromObject(object);
        if (attributeValue instanceof DatabaseValueHolder){
            return ((DatabaseValueHolder)attributeValue).clone();
        } else if (attributeValue instanceof ValueHolder){
            return ((ValueHolder)attributeValue).clone(); 
        }
        return attributeValue;
    }

    /**
     * INTERNAL:
     * Return the mapping's indirection policy.
     */
    public IndirectionPolicy getIndirectionPolicy() {
        return indirectionPolicy;
    }

    /**
     * INTERNAL:
     * Returns the join criteria stored in the mapping selection query. This criteria
     * is used to read reference objects across the tables from the database.
     */
    public Expression getJoinCriteria(QueryKeyExpression exp) {
        Expression selectionCriteria = getSelectionCriteria();
        return exp.getBaseExpression().twist(selectionCriteria, exp);
    }

    /**
     * INTERNAL:
     * return the object on the client corresponding to the specified object.
     * ForeignReferenceMappings have to worry about
     * maintaining object identity.
     */
    public Object getObjectCorrespondingTo(Object object, RemoteSession session, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query) {
        return session.getObjectCorrespondingTo(object, objectDescriptors, processedObjects, query);
    }

    /**
     * INTERNAL:
     * Returns the attribute value from the reference object.
     * If the attribute is using indirection the value of the value-holder is returned.
     * If the value holder is not instantiated then it is instantiated.
     */
    public Object getRealAttributeValueFromObject(Object object, AbstractSession session) {
        return getIndirectionPolicy().getRealAttributeValueFromObject(object, getAttributeValueFromObject(object));
    }

    /**
     * PUBLIC:
     * Returns the reference class.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Returns the reference class name.
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * Return the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    public ClassDescriptor getReferenceDescriptor() {
        if (referenceDescriptor == null) {
            if (getTempSession() == null) {
                return null;
            } else {
                referenceDescriptor = getTempSession().getDescriptor(getReferenceClass());
            }
        }

        return referenceDescriptor;
    }

    /**
     * INTERNAL:
     * Return the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     */
    public DatabaseMapping getRelationshipPartner() {
        if ((this.relationshipPartner == null) && (this.relationshipPartnerAttributeName != null)) {
            setRelationshipPartner(getReferenceDescriptor().getObjectBuilder().getMappingForAttributeName(getRelationshipPartnerAttributeName()));
        }
        return this.relationshipPartner;
    }

    /**
     * PUBLIC:
     *  Use this method retreive the relationship partner attribute name of this bidirectional Mapping.
     */
    public String getRelationshipPartnerAttributeName() {
        return this.relationshipPartnerAttributeName;
    }

    /**
     * INTERNAL:
     * Returns the selection criteria stored in the mapping selection query. This criteria
     * is used to read reference objects from the database.
     */
    public Expression getSelectionCriteria() {
        return getSelectionQuery().getSelectionCriteria();
    }

    /**
     * INTERNAL:
     * Returns the read query assoicated with the mapping.
     */
    public ReadQuery getSelectionQuery() {
        return selectionQuery;
    }

    protected AbstractSession getTempSession() {
        return tempInitSession;
    }

    /**
     * INTERNAL:
     * Extract and return the appropriate value from the
     * specified remote value holder.
     */
    public Object getValueFromRemoteValueHolder(RemoteValueHolder remoteValueHolder) {
        return getIndirectionPolicy().getValueFromRemoteValueHolder(remoteValueHolder);
    }

    /**
     * INTERNAL:
     * Indicates whether the selection query is TopLink generated or defined by 
     * the user.
     */
    public boolean hasCustomSelectionQuery() {
        return hasCustomSelectionQuery;
    }

    /**
     * INTERNAL:
     * Initialize the state of mapping.
     */
    public void preInitialize(AbstractSession session) throws DescriptorException {
        super.preInitialize(session);
/*        // If weaving was used the mapping must be configured to use the weaved get/set methods.
        if ((getIndirectionPolicy() instanceof BasicIndirectionPolicy) && ClassConstants.PersistenceWeaved_Class.isAssignableFrom(getDescriptor().getJavaClass())) {
            Class attributeType = getAttributeAccessor().getAttributeClass();
            // Check that not already weaved or coded.
            if (!(ClassConstants.ValueHolderInterface_Class.isAssignableFrom(attributeType))) {
                boolean usesMethodAccess = getAttributeAccessor() instanceof MethodAttributeAccessor;
                String originalSetMethod = null;
                if (usesMethodAccess) {
                    originalSetMethod = getSetMethodName();
                }
                setGetMethodName(ClassWeaver.getWeavedValueHolderGetMethodName(getAttributeName()));
                setSetMethodName(ClassWeaver.getWeavedValueHolderSetMethodName(getAttributeName()));
                if (usesMethodAccess) {
                    useWeavedIndirection(originalSetMethod);
                }
                // Must re-initialize the attribute accessor.
                super.preInitialize(session);
            }
        }*/
    }
    
    /**
     * INTERNAL:
     * Initialize the state of mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        initializeReferenceDescriptor(session);
        initializeSelectionQuery(session);
        getIndirectionPolicy().initialize();
    }

    /**
     * Initialize and set the descriptor for the referenced class in this mapping.
     */
    protected void initializeReferenceDescriptor(AbstractSession session) throws DescriptorException {
        if (getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }

        ClassDescriptor refDescriptor = session.getDescriptor(getReferenceClass());

        if (refDescriptor == null) {
            throw DescriptorException.descriptorIsMissing(getReferenceClass().getName(), this);
        }

        if (refDescriptor.isAggregateDescriptor() && (!isAggregateCollectionMapping())) {
            throw DescriptorException.referenceDescriptorCannotBeAggregate(this);
        }

        // can not be isolated if it is null.  Seems that only aggregates do not set
        // the owning descriptor on the mapping.
        if ((!((this.getDescriptor() != null) && this.getDescriptor().isIsolated())) && refDescriptor.isIsolated()) {
            throw DescriptorException.isolateDescriptorReferencedBySharedDescriptor(refDescriptor.getJavaClassName(), this.getDescriptor().getJavaClassName(), this);
        }

        setReferenceDescriptor(refDescriptor);
    }

    /**
     * A subclass should implement this method if it wants non default behaviour.
     */
    protected void initializeSelectionQuery(AbstractSession session) throws DescriptorException {
        if (((ObjectLevelReadQuery)getSelectionQuery()).getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }

        getSelectionQuery().setDescriptor(getReferenceDescriptor());
    }

    /**
     * INTERNAL:
     * The referenced object is checked if it is instantiated or not
     */
    public boolean isAttributeValueInstantiated(Object object) {
        return getIndirectionPolicy().objectIsInstantiated(getAttributeValueFromObject(object));
    }

    /**
     * PUBLIC:
     * Check cascading value for the CREATE operation.
     */
    public boolean isCascadePersist() {
        return this.cascadePersist;
    }

    /**
     * PUBLIC:
     * Check cascading value for the MERGE operation.
     */
    public boolean isCascadeMerge() {
        return this.cascadeMerge;
    }

    /**
     * PUBLIC:
     * Check cascading value for the REFRESH operation.
     */
    public boolean isCascadeRefresh() {
        return this.cascadeRefresh;
    }

    /**
     * PUBLIC:
     * Check cascading value for the REMOVE operation.
     */
    public boolean isCascadeRemove() {
        return this.cascadeRemove;
    }

    /**
     * INTERNAL:
     */
    public boolean isForeignReferenceMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Return if this mapping supports joining.
     */
    public boolean isJoiningSupported() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if referenced objects are privately owned else false.
     */
    public boolean isPrivateOwned() {
        return isPrivateOwned;
    }

    /**
     * INTERNAL:
     * Iterate on the iterator's current object's attribute defined by this mapping.
     * The iterator's settings for cascading and value holders determine how the
     * iteration continues from here.
     */
    public void iterate(DescriptorIterator iterator) {
        Object attributeValue = this.getAttributeValueFromObject(iterator.getVisitedParent());
        this.getIndirectionPolicy().iterateOnAttributeValue(iterator, attributeValue);
    }

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     */
    public abstract void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue);

    /**
     * INTERNAL:
     * Replace the client value holder with the server value holder,
     * after copying some of the settings from the client value holder.
     */
    public void mergeRemoteValueHolder(Object clientSideDomainObject, Object serverSideDomainObject, MergeManager mergeManager) {
        getIndirectionPolicy().mergeRemoteValueHolder(clientSideDomainObject, serverSideDomainObject, mergeManager);
    }

    /**
     * PUBLIC:
     * Sets the reference object to be a private owned.
     * The default behaviour is non private owned, or independent.
     * @see #setIsPrivateOwned(boolean)
     */
    public void privateOwnedRelationship() {
        setIsPrivateOwned(true);
    }

    /**
     * INTERNAL:
     * Once descriptors are serialized to the remote session. All its mappings and reference descriptors are traversed. Usually
     * mappings are initilaized and serialized reference descriptors are replaced with local descriptors if they already exist on the
     * remote session.
     */
    public void remoteInitialization(DistributedSession session) {
        super.remoteInitialization(session);
        setTempSession(session);
    }

    /**
     * INTERNAL:
     * replace the value holders in the specified reference object(s)
     */
    public IdentityHashtable replaceValueHoldersIn(Object object, RemoteSessionController controller) {
        return controller.replaceValueHoldersIn(object);
    }

    /**
     * PUBLIC:
     * Sets the cascading for all JPA operations.
     */
    public void setCascadeAll(boolean value) {
        setCascadePersist(value);
        setCascadeMerge(value);
        setCascadeRefresh(value);
        setCascadeRemove(value);
    }

    /**
     * PUBLIC:
     * Sets the cascading for the JPA CREATE operation.
     */
    public void setCascadePersist(boolean value) {
        this.cascadePersist = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the JPA MERGE operation.
     */
    public void setCascadeMerge(boolean value) {
        this.cascadeMerge = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the JPA REFRESH operation.
     */
    public void setCascadeRefresh(boolean value) {
        this.cascadeRefresh = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the JPA REMOVE operation.
     */
    public void setCascadeRemove(boolean value) {
        this.cascadeRemove = value;
    }

    /**
     * PUBLIC:
     * Relationship mappings creates a read query to read reference objects. If this default
     * query needs to be customize then user can specify its own read query to do the reading
     * of reference objects. One must instance of ReadQuery or subclasses of the ReadQuery.
     */
    public void setCustomSelectionQuery(ReadQuery query) {
        setSelectionQuery(query);
        setHasCustomSelectionQuery(true);
    }

    protected void setHasCustomSelectionQuery(boolean bool) {
        hasCustomSelectionQuery = bool;
    }

    /**
     * ADVANCED:
     * Set the indirection policy.
     */
    public void setIndirectionPolicy(IndirectionPolicy indirectionPolicy) {
        this.indirectionPolicy = indirectionPolicy;
        indirectionPolicy.setMapping(this);
    }

    /**
     * PUBLIC:
     * Set if the relationship is privately owned.
     * A privately owned relationship means the target object is a dependent part of the source
     * object and is not referenced by any other object and cannot exist on its own.
     * Private ownership causes many operations to be cascaded across the relationship,
     * including, deletion, insertion, refreshing, locking (when cascaded).
     * It also ensures that private objects removed from collections are deleted and object added are inserted.
     */
    public void setIsPrivateOwned(boolean isPrivateOwned) {
        this.isPrivateOwned = isPrivateOwned;
    }

    /**
     * INTERNAL:
     * Set the value of the attribute mapped by this mapping,
     * placing it inside a value holder if necessary.
     * If the value holder is not instantiated then it is instantiated.
     */
    public void setRealAttributeValueInObject(Object object, Object value) throws DescriptorException {
        this.getIndirectionPolicy().setRealAttributeValueInObject(object, value);
    }

    /**
     * PUBLIC:
     * Set the referenced class.
     */
    public void setReferenceClass(Class referenceClass) {
        this.referenceClass = referenceClass;
        if (referenceClass != null) {
            setReferenceClassName(referenceClass.getName());
            // Make sure the reference class of the selectionQuery is set.
            setSelectionQuery(getSelectionQuery());
        }
    }

    /**
     * INTERNAL:
     * Used by MW.
     */
    public void setReferenceClassName(String referenceClassName) {
        this.referenceClassName = referenceClassName;
    }

    /**
     * Set the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    protected void setReferenceDescriptor(ClassDescriptor aDescriptor) {
        referenceDescriptor = aDescriptor;
    }

    /**
     * INTERNAL:
     * Sets the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     */
    public void setRelationshipPartner(DatabaseMapping mapping) {
        this.relationshipPartner = mapping;
    }

    /**
    * PUBLIC:
    * Use this method to specify the relationship partner attribute name of a bidirectional Mapping.
    * TopLink will use the attribute name to find the back pointer mapping to maintain referential integrity of
    * the bi-directional mappings.
    */
    public void setRelationshipPartnerAttributeName(String attributeName) {
        this.relationshipPartnerAttributeName = attributeName;
    }

    /**
     * PUBLIC:
     * Sets the selection criteria to be used as a where clause to read
     * reference objects. This criteria is automatically generated by the
     * TopLink if not explicitly specified by the user.
     */
    public void setSelectionCriteria(Expression anExpression) {
        getSelectionQuery().setSelectionCriteria(anExpression);
    }

    /**
     * Sets the query
     */
    protected void setSelectionQuery(ReadQuery aQuery) {
        selectionQuery = aQuery;
        // Make sure the reference class of the selectionQuery is set.        
        if ((selectionQuery != null) && selectionQuery.isObjectLevelReadQuery() && (selectionQuery.getReferenceClassName() == null)) {
            ((ObjectLevelReadQuery)selectionQuery).setReferenceClass(getReferenceClass());
        }
    }

    /**
     * PUBLIC:
     * This is a property on the mapping which will allow custom SQL to be
     * substituted for reading a reference object.
     */
    public void setSelectionSQLString(String sqlString) {
        getSelectionQuery().setSQLString(sqlString);
        setCustomSelectionQuery(getSelectionQuery());
    }

    /**
     * PUBLIC:
     * This is a property on the mapping which will allow custom call to be
     * substituted for reading a reference object.
     */
    public void setSelectionCall(Call call) {
        getSelectionQuery().setCall(call);
        setCustomSelectionQuery(getSelectionQuery());
    }

    protected void setTempSession(AbstractSession session) {
        this.tempInitSession = session;
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be batch read on read all queries.
     * Batch reading will read all of the related objects in a single query when accessed from an originating read all.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void setUsesBatchReading(boolean usesBatchReading) {
        this.usesBatchReading = usesBatchReading;
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     * @see #useBasicIndirection()
     * @see #dontUseIndirection()
     */
    public void setUsesIndirection(boolean usesIndirection) {
        if (usesIndirection) {
            useBasicIndirection();
        } else {
            dontUseIndirection();
        }
    }

    protected boolean shouldInitializeSelectionCriteria() {
        if (hasCustomSelectionQuery()) {
            return false;
        }

        if (getSelectionCriteria() == null) {
            return true;
        }

        return false;
    }

    /**
     * INTERNAL:
     * Returns true if the merge should cascade to the mappings reference's parts.
     */
    public boolean shouldMergeCascadeParts(MergeManager mergeManager) {
        return ((mergeManager.shouldCascadeByMapping() && this.isCascadeMerge()) || (mergeManager.shouldCascadeAllParts()) || (mergeManager.shouldCascadePrivateParts() && isPrivateOwned()));
    }

    /**
     * Returns true if the merge should cascade to the mappings reference.
     */
    protected boolean shouldMergeCascadeReference(MergeManager mergeManager) {
        if (mergeManager.shouldCascadeReferences()) {
            return true;
        }

        // P2.0.1.3: Was merging references on non-privately owned parts
        // Same logic in:
        return shouldMergeCascadeParts(mergeManager);
    }

    /**
     * Returns true if any process leading to object modification should also affect its parts
     * Usually used by write, insert, update and delete.
     */
    protected boolean shouldObjectModifyCascadeToParts(ObjectLevelModifyQuery query) {
        if (isReadOnly()) {
            return false;
        }

        // Only cascade dependents writes in uow.
        if (query.shouldCascadeOnlyDependentParts()) {
            return hasConstraintDependency();
        }

        if (isPrivateOwned()) {
            return true;
        }

        return query.shouldCascadeAllParts();
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be batch read on read all queries.
     * Batch reading will read all of the related objects in a single query when accessed from an originating read all.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public boolean shouldUseBatchReading() {
        return usesBatchReading;
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public void useBasicIndirection() {
        setIndirectionPolicy(new BasicIndirectionPolicy());
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be batch read on read all queries.
     * Batch reading will read all of the related objects in a single query when accessed from an originating read all.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void useBatchReading() {
        setUsesBatchReading(true);
    }

    /**
     * INTERNAL:
     * Configures the mapping to used weaved indirection.
     * This requires that the toplink-agent be used to weave indirection into the class.
     * This policy is only require for method access.
     * @param setMethodName is the name of the original set method for the mapping.
     */
    public void useWeavedIndirection(String setMethodName){
        setIndirectionPolicy(new WeavedObjectBasicIndirectionPolicy(setMethodName));
    }

    /**
     * PUBLIC:
     * Indirection means that a IndirectContainer (wrapping a ValueHolder) will be put in-between the attribute and the real object.
     * This allows for an application specific class to be used which wraps the value holder.
     * The purpose of this is that the domain objects will not require to import the ValueHolderInterface class.
     * Refer also to transparent indirection for a transparent solution to indirection.
     */
    public void useContainerIndirection(Class containerClass) {
        ContainerIndirectionPolicy policy = new ContainerIndirectionPolicy();
        policy.setContainerClass(containerClass);
        setIndirectionPolicy(policy);
    }

    /**
     * PUBLIC:
     * Indirection means that some sort of indirection object will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public boolean usesIndirection() {
        return getIndirectionPolicy().usesIndirection();
    }
    
    /**
     * PUBLIC:
     * Indicates whether the referenced object(s) should always be joined on read queries.
     * Joining will join the two classes tables to read all of the data in a single query.
     * This should only be used if it is know that the related objects are always required with the source object,
     * or indirection is not used.
     * A join-fetch can either use an INNER_JOIN or OUTER_JOIN,
     * if the relationship may reference null or an empty collection an outer join should be used to avoid filtering the source objects from the queries.
     * Join fetch can also be specified on the query, and it is normally more efficient to do so as some queries may not require the related objects.
     * Typically batch reading is more efficient than join fetching and should be considered, especially for collection relationships.
     * @see oracle.toplink.queryframework.ObjectLevelReadQuery#addJoinedAttribute(String)
     * @see oracle.toplink.queryframework.ReadAllQuery#addBatchReadAttribute(String)
     */
    public void setJoinFetch(int joinFetch) {
        this.joinFetch = joinFetch;
    }
    
    /**
     * PUBLIC:
     * Return if this relationship should always be join fetched.
     */
    public int getJoinFetch() {
        return joinFetch;
    }
        
    /**
     * PUBLIC:
     * Return if this relationship should always be join fetched.
     */
    public boolean isJoinFetched() {
        return getJoinFetch() != NONE;
    }
            
    /**
     * PUBLIC:
     * Return if this relationship should always be INNER join fetched.
     */
    public boolean isInnerJoinFetched() {
        return getJoinFetch() == INNER_JOIN;
    }
    
    /**
     * PUBLIC:
     * Return if this relationship should always be OUTER join fetched.
     */
    public boolean isOuterJoinFetched() {
        return getJoinFetch() == OUTER_JOIN;
    }
        
    /**
     * PUBLIC:
     * Specify this relationship to always be join fetched using an INNER join.
     */
    public void useInnerJoinFetch() {
        setJoinFetch(INNER_JOIN);
    }
    
    /**
     * PUBLIC:
     * Specify this relationship to always be join fetched using an OUTER join.
     */
    public void useOuterJoinFetch() {
        setJoinFetch(OUTER_JOIN);
    }

    /**
     * INTERNAL:
     * To validate mappings decleration
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
        super.validateBeforeInitialization(session);

        if (getAttributeAccessor() instanceof InstanceVariableAttributeAccessor) {
            Class attributeType = ((InstanceVariableAttributeAccessor)getAttributeAccessor()).getAttributeType();
            getIndirectionPolicy().validateDeclaredAttributeType(attributeType, session.getIntegrityChecker());
        } else if (getAttributeAccessor() instanceof MethodAttributeAccessor) {
            Class returnType = ((MethodAttributeAccessor)getAttributeAccessor()).getGetMethodReturnType();
            getIndirectionPolicy().validateGetMethodReturnType(returnType, session.getIntegrityChecker());

            Class parameterType = ((MethodAttributeAccessor)getAttributeAccessor()).getSetMethodParameterType();
            getIndirectionPolicy().validateSetMethodParameterType(parameterType, session.getIntegrityChecker());
        }
    }

    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        // PERF: Direct variable access.
        // If the query uses batch reading, return a special value holder
        // or retrieve the object from the query property.
        if (sourceQuery.isReadAllQuery() && (((ReadAllQuery)sourceQuery).isAttributeBatchRead(this.descriptor, getAttributeName()) || this.usesBatchReading)) {
            return batchedValueFromRow(row, (ReadAllQuery)sourceQuery);
        }
        if (shouldUseValueFromRowWithJoin(joinManager, sourceQuery)) {
            return valueFromRowInternalWithJoin(row, joinManager, sourceQuery, executionSession);
        } else {
            return valueFromRowInternal(row, joinManager, sourceQuery, executionSession);
        }
    }
    
    /**
     * INTERNAL:
     * Indicates whether valueFromRow should call valueFromRowInternalWithJoin (true)
     * or valueFromRowInternal (false)
     */
    protected boolean shouldUseValueFromRowWithJoin(JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery) {
        return ((joinManager != null) && (joinManager.isAttributeJoined(this.descriptor, getAttributeName()))) || sourceQuery.hasPartialAttributeExpressions();
    }
    
    /**
     * INTERNAL:
     * If the query used joining or partial attributes, build the target object directly.
     * If isJoiningSupported()==true then this method must be overridden.
     */
    protected Object valueFromRowInternalWithJoin(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        throw ValidationException.mappingDoesNotOverrideValueFromRowInternalWithJoin(Helper.getShortClassName(this.getClass()));
    }
    
    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     */
    protected Object valueFromRowInternal(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        // PERF: Direct variable access.
        ReadQuery targetQuery = this.selectionQuery;
        // CR #4365, 3610825 - moved up from the block below, needs to be set with 
        // indirection off. Clone the query and set its id.
        if (!this.indirectionPolicy.usesIndirection()) {
            // perf: bug#4751950, first prepare the query before cloning.
            if (targetQuery.shouldPrepare()) {
                targetQuery.checkPrepare(executionSession, row);
            }
            targetQuery = (ReadQuery)targetQuery.clone();
            targetQuery.setIsExecutionClone(true);
            targetQuery.setQueryId(sourceQuery.getQueryId());
        }

        // If the source query is cascading then the target query must use the same settings.
        if (targetQuery.isObjectLevelReadQuery() && (sourceQuery.shouldCascadeAllParts() || (this.isPrivateOwned && sourceQuery.shouldCascadePrivateParts()) || (this.cascadeRefresh && sourceQuery.shouldCascadeByMapping()))) {
            // If the target query has already been cloned (we're refreshing) avoid 
            // re-cloning the query again.
            if (targetQuery == this.selectionQuery) {
                // perf: bug#4751950, first prepare the query before cloning.
                if (targetQuery.shouldPrepare()) {
                    targetQuery.checkPrepare(executionSession, row);
                }
                targetQuery = (ObjectLevelReadQuery)targetQuery.clone();
                targetQuery.setIsExecutionClone(true);
            }

            ((ObjectLevelReadQuery)targetQuery).setShouldRefreshIdentityMapResult(sourceQuery.shouldRefreshIdentityMapResult());
            targetQuery.setCascadePolicy(sourceQuery.getCascadePolicy());

            // For queries that have turned caching off, such as aggregate collection, leave it off.
            if (targetQuery.shouldMaintainCache()) {
                targetQuery.setShouldMaintainCache(sourceQuery.shouldMaintainCache());
            }

            // For flashback: Read attributes as of the same time if required.
            if (((ObjectLevelReadQuery)sourceQuery).hasAsOfClause()) {
                targetQuery.setSelectionCriteria((Expression)targetQuery.getSelectionCriteria().clone());
                ((ObjectLevelReadQuery)targetQuery).setAsOfClause(((ObjectLevelReadQuery)sourceQuery).getAsOfClause());
            }
        }
        targetQuery = prepareHistoricalQuery(targetQuery, sourceQuery, executionSession);

        return this.indirectionPolicy.valueFromQuery(targetQuery, row, sourceQuery.getSession());
    }
    
    /**
     * INTERNAL:
     * Allow for the mapping to perform any historical query additions.
     * Return the new target query.
     */
    protected ReadQuery prepareHistoricalQuery(ReadQuery targetQuery, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) {
        return targetQuery;
    }

    /**
     * INTERNAL:
     * Return a sub-partition of the row starting at the index for the mapping.
     */
    public AbstractRecord trimRowForJoin(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) {
        // The field for many objects may be in the row,
        // so build the subpartion of the row through the computed values in the query,
        // this also helps the field indexing match.
        if ((joinManager != null) && (joinManager.getJoinedMappingIndexes_() != null)) {
            Object value = joinManager.getJoinedMappingIndexes_().get(this);
            if (value != null) {
               return trimRowForJoin(row, value, executionSession);
            }
        }
        return row;
    }

    /**
     * INTERNAL:
     * Return a sub-partition of the row starting at the index.
     */
    public AbstractRecord trimRowForJoin(AbstractRecord row, Object value, AbstractSession executionSession) {
        // CR #... the field for many objects may be in the row,
        // so build the subpartion of the row through the computed values in the query,
        // this also helps the field indexing match.
        int fieldStartIndex;
        if (value instanceof Integer) {
            fieldStartIndex = ((Integer)value).intValue();
        } else {
            // must be Map of classes to Integers
            Map map = (Map)value;
            Class cls;
            if (getDescriptor().hasInheritance() && getDescriptor().getInheritancePolicy().shouldReadSubclasses()) {
                cls = getDescriptor().getInheritancePolicy().classFromRow(row, executionSession);
            } else {
                cls = getDescriptor().getJavaClass();
            }
            fieldStartIndex = ((Integer)map.get(cls)).intValue();
        }
        Vector trimedFields = new NonSynchronizedSubVector(row.getFields(), fieldStartIndex, row.size());
        Vector trimedValues = new NonSynchronizedSubVector(row.getValues(), fieldStartIndex, row.size());
        return new DatabaseRecord(trimedFields, trimedValues);
    }
    
    /**
     * INTERNAL:
     * Prepare the clone of the nested query for joining.
     * The nested query clones are stored on the execution (clone) joinManager to avoid cloning per row.
     */
    protected ObjectLevelReadQuery prepareNestedJoinQueryClone(AbstractRecord row, List dataResults, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) {
        // A nested query must be built to pass to the descriptor that looks like the real query execution would,
        // these should be cached on the query during prepare.
        ObjectLevelReadQuery nestedQuery = null;
        // This is also call for partial object reading.
        if (joinManager == null) {
            nestedQuery = prepareNestedJoins(joinManager, sourceQuery, executionSession);
            nestedQuery.setSession(executionSession);
            return nestedQuery;
        }
        // PERF: Also store the clone of the nested query on the execution query to avoid
        // cloning per row.
        if (joinManager.getJoinedMappingQueryClones() == null) {
            joinManager.setJoinedMappingQueryClones(new HashMap(5));
        }
        nestedQuery = joinManager.getJoinedMappingQueryClones().get(this);
        if (nestedQuery == null) {
            if (joinManager.getJoinedMappingQueries_() != null) {
                nestedQuery = joinManager.getJoinedMappingQueries_().get(this);
                nestedQuery = (ObjectLevelReadQuery)nestedQuery.clone();
            } else {
                nestedQuery = prepareNestedJoins(joinManager, sourceQuery, executionSession);
            }
            nestedQuery.setSession(executionSession);
            //CR #4365 - used to prevent infinite recursion on refresh object cascade all
            nestedQuery.setQueryId(joinManager.getBaseQuery().getQueryId());
            // set execution time on the nested query
            nestedQuery.setExecutionTime(joinManager.getBaseQuery().getExecutionTime());
            joinManager.getJoinedMappingQueryClones().put(this, nestedQuery);
        }
        // Must also set data results to the nested query if it uses to-many joining.
        if (nestedQuery.hasJoining() && nestedQuery.getJoinedAttributeManager().isToManyJoin()) {
            // The data results only of the child object are required, they must also be trimmed.
            List nestedDataResults = dataResults;
            if (nestedDataResults == null) {
                // Extract the primary key of the source object, to filter only the joined rows for that object.
                Vector sourceKey = getDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(row, executionSession);
                CacheKey sourceCacheKey = new CacheKey(sourceKey);
                nestedDataResults = joinManager.getDataResultsByPrimaryKey().get(sourceCacheKey);
            }
            nestedDataResults = new ArrayList(nestedDataResults);
            Object indexObject = joinManager.getJoinedMappingIndexes_().get(this);
            // Trim results to start at nested row index.
            for (int index = 0; index < nestedDataResults.size(); index++) {
                AbstractRecord sourceRow = (AbstractRecord)nestedDataResults.get(index);                        
                nestedDataResults.set(index, trimRowForJoin(sourceRow, indexObject, executionSession));
            }
            nestedQuery.getJoinedAttributeManager().setDataResults(nestedDataResults, executionSession);
        }
        return nestedQuery;
    }
}
