// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.internal.descriptors.changetracking.AggregateAttributeChangeListener;
import oracle.toplink.internal.descriptors.changetracking.AttributeChangeListener;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.descriptors.DescriptorEventManager;
import oracle.toplink.descriptors.DescriptorQueryManager;
import oracle.toplink.descriptors.changetracking.ChangeTracker;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.internal.descriptors.*;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.remote.*;
import oracle.toplink.sessions.ObjectCopyingPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;

/**
 * <b>Purpose</b>: Two objects can be considered to be related by aggregation if there is a strict
 * 1:1 relationship between the objects. This means that if the source (parent)object exists, then
 * the target (child or owned) object must exist. This class implements the behavior common to the
 * aggregate object and structure mappings.
 *
 * @author Sati
 * @since TopLink for Java 1.0
 */
public abstract class AggregateMapping extends DatabaseMapping {

    /** Stores a reference class */
    protected Class referenceClass;
    protected String referenceClassName;

    /** The descriptor of the reference class */
    protected ClassDescriptor referenceDescriptor;

    /**
     * Default constructor.
     */
    public AggregateMapping() {
        super();
    }

    /**
     * Make a copy of the sourceQuery for the attribute.
     */
    protected DeleteObjectQuery buildAggregateDeleteQuery(DeleteObjectQuery sourceQuery, Object sourceAttributeValue) {
        DeleteObjectQuery aggregateQuery = new DeleteObjectQuery();
        buildAggregateModifyQuery(sourceQuery, aggregateQuery, sourceAttributeValue);
        return aggregateQuery;
    }

    /**
     * Initialize the aggregate query with the settings from the source query.
     */
    protected void buildAggregateModifyQuery(ObjectLevelModifyQuery sourceQuery, ObjectLevelModifyQuery aggregateQuery, Object sourceAttributeValue) {
        if (sourceQuery.getSession().isUnitOfWork()) {
            Object backupAttributeValue = getAttributeValueFromBackupClone(sourceQuery.getBackupClone());
            if (backupAttributeValue == null) {
                backupAttributeValue = getObjectBuilder(sourceAttributeValue, sourceQuery.getSession()).buildNewInstance();
            }
            aggregateQuery.setBackupClone(backupAttributeValue);
        }
        aggregateQuery.setCascadePolicy(sourceQuery.getCascadePolicy());
        aggregateQuery.setObject(sourceAttributeValue);
        aggregateQuery.setTranslationRow(sourceQuery.getTranslationRow());
        aggregateQuery.setSession(sourceQuery.getSession());
        aggregateQuery.setProperties(sourceQuery.getProperties());
    }

    /**
     * Make a copy of the sourceQuery for the attribute.
     */
    protected WriteObjectQuery buildAggregateWriteQuery(WriteObjectQuery sourceQuery, Object sourceAttributeValue) {
        WriteObjectQuery aggregateQuery = new WriteObjectQuery();
        buildAggregateModifyQuery(sourceQuery, aggregateQuery, sourceAttributeValue);
        return aggregateQuery;
    }

    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(clone);
        setAttributeValueInObject(backup, buildBackupClonePart(attributeValue, unitOfWork));
    }

    /**
     * INTERNAL:
     * Build and return a backup clone of the attribute.
     */
    protected Object buildBackupClonePart(Object attributeValue, UnitOfWorkImpl unitOfWork) {
        if (attributeValue == null) {
            return null;
        }
        return getObjectBuilder(attributeValue, unitOfWork).buildBackupClone(attributeValue, unitOfWork);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(original);
        setAttributeValueInObject(clone, buildClonePart(original, attributeValue, unitOfWork));
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
        // automatically returns a uow result from scratch that doesn't need cloning
        Object cloneAttributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);
        setAttributeValueInObject(clone, cloneAttributeValue);
    }

    /**
     * INTERNAL:
     * Build and return a clone of the attribute.
     */
    protected Object buildClonePart(Object original, Object attributeValue, UnitOfWorkImpl unitOfWork) {
        if (attributeValue == null) {
            return null;
        }
        if (unitOfWork.isOriginalNewObject(original)) {
            unitOfWork.addNewAggregate(attributeValue);
        }

        // Do not clone for read-only.
        if (unitOfWork.isClassReadOnly(attributeValue.getClass())) {
            return attributeValue;
        }

        ObjectBuilder aggregateObjectBuilder = getObjectBuilder(attributeValue, unitOfWork);

        // bug 2612602 as we are building the working copy make sure that we call to correct clone method.
        Object clonedAttributeValue = aggregateObjectBuilder.instantiateWorkingCopyClone(attributeValue, unitOfWork);
        aggregateObjectBuilder.populateAttributesForClone(attributeValue, clonedAttributeValue, unitOfWork);

        return clonedAttributeValue;
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        Object attributeValue = getAttributeValueFromObject(original);
        setAttributeValueInObject(copy, buildCopyOfAttributeValue(attributeValue, policy));
    }

    /**
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    protected Object buildCopyOfAttributeValue(Object attributeValue, ObjectCopyingPolicy policy) {
        if (attributeValue == null) {
            return null;
        }
        return getObjectBuilder(attributeValue, policy.getSession()).copyObject(attributeValue, policy);
    }

    /**
     * INTERNAL:
     * In case Query By Example is used, this method generates an expression from a attribute value pair.  Since
     * this is an Aggregate mapping, a recursive call is made to the buildExpressionFromExample method of
     * ObjectBuilder.
     */
    public Expression buildExpression(Object queryObject, QueryByExamplePolicy policy, Expression expressionBuilder, IdentityHashtable processedObjects, AbstractSession session) {
        String attributeName = this.getAttributeName();
        Object attributeValue = this.getRealAttributeValueFromObject(queryObject, session);

        if (!policy.shouldIncludeInQuery(queryObject.getClass(), attributeName, attributeValue)) {
            //the attribute name and value pair is not to be included in the query.
            return null;
        }

        if (attributeValue == null) {
            //even though it is null, it is to be always included in the query
            Expression expression = expressionBuilder.get(attributeName);
            return policy.completeExpressionForNull(expression);
        }

        ObjectBuilder objectBuilder = getReferenceDescriptor().getObjectBuilder();
        return objectBuilder.buildExpressionFromExample(attributeValue, policy, expressionBuilder.get(attributeName), processedObjects, session);
    }

    /**
     * INTERNAL:
     * Build and return a new instance of the specified attribute.
     * This will be populated by a merge.
     */
    protected Object buildNewMergeInstanceOf(Object sourceAttributeValue, AbstractSession session) {
        return getObjectBuilder(sourceAttributeValue, session).buildNewInstance();
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
//    public void cascadePerformDeleteIfRequired(Object object, UnitOfWork uow, IdentityHashtable visitedObjects){
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
//    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
//    public void cascadeRegisterNewIfRequired(Object object, UnitOfWork uow, IdentityHashtable visitedObjects){
        //aggregate objects are not registeres as they have no identity, this is a no-op.
//    }

    /**
     * INTERNAL:
     * Compare the attributes. Return true if they are alike.
     */
    protected boolean compareAttributeValues(Object attributeValue1, Object attributeValue2, AbstractSession session) {
        if ((attributeValue1 == null) && (attributeValue2 == null)) {
            return true;
        }
        if ((attributeValue1 == null) || (attributeValue2 == null)) {
            return false;
        }
        if (attributeValue1.getClass() != attributeValue2.getClass()) {
            return false;
        }
        return getObjectBuilder(attributeValue1, session).compareObjects(attributeValue1, attributeValue2, session);
    }

    /**
     * INTERNAL:
     * Compare the changes between two aggregates.
     * Return a change record holding the changes.
     */
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        Object cloneAttribute = getAttributeValueFromObject(clone);
        Object backupAttribute = null;

        if (!owner.isNew()) {
            backupAttribute = getAttributeValueFromObject(backup);
            if ((cloneAttribute == null) && (backupAttribute == null)) {
                return null;// no change
            }
            if ((cloneAttribute != null) && (backupAttribute != null) && (!cloneAttribute.getClass().equals(backupAttribute.getClass()))) {
                backupAttribute = null;
            }
        }

        AggregateChangeRecord changeRecord = new AggregateChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);

        if (cloneAttribute == null) {// the attribute was set to null
            changeRecord.setChangedObject(null);
            return changeRecord;
        }

        ObjectBuilder builder = getObjectBuilder(cloneAttribute, session);

        //if the owner is new then the backup will be null, if the owner is new then the aggregate is new
        //if the backup is null but the owner is not new then this aggregate is new
        ObjectChangeSet initialChanges = builder.createObjectChangeSet(cloneAttribute, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), (backupAttribute == null), session);
        ObjectChangeSet changeSet = builder.compareForChange(cloneAttribute, backupAttribute, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
        if (changeSet == null) {
            if (initialChanges.isNew()) {
                // This happens if original aggregate is of class A, the new aggregate
                // is of class B (B inherits from A) - and neither A nor B has any mapped attributes.
                // CR3664
                changeSet = initialChanges;
            } else {
                return null;// no change
            }
        }
        changeRecord.setChangedObject(changeSet);
        return changeRecord;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        return compareAttributeValues(getAttributeValueFromObject(firstObject), getAttributeValueFromObject(secondObject), session);
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
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
     * Execute a descriptor event for the specified event code.
     */
    protected void executeEvent(int eventCode, ObjectLevelModifyQuery query) {
        ClassDescriptor referenceDescriptor = getReferenceDescriptor(query.getObject(), query.getSession());

        // PERF: Avoid events if no listeners.
        if (referenceDescriptor.getEventManager().hasAnyEventListeners()) {
            referenceDescriptor.getEventManager().executeEvent(new oracle.toplink.descriptors.DescriptorEvent(eventCode, query));
        }
    }

    /**
     * INTERNAL:
     * An object has been serialized from the server to the remote client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    protected void fixAttributeValue(Object attributeValue, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
        if (attributeValue == null) {
            return;
        }
        getObjectBuilder(attributeValue, query.getSession()).fixObjectReferences(attributeValue, objectDescriptors, processedObjects, query, session);
    }

    /**
     * INTERNAL:
     * An object has been serialized from the server to the remote client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    public void fixObjectReferences(Object object, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
        Object attributeValue = getAttributeValueFromObject(object);
        fixAttributeValue(attributeValue, objectDescriptors, processedObjects, query, session);
    }

    /**
     * Return the appropriate attribute value.
     * This method is a hack to allow the aggregate collection
     * subclass to override....
     */
    protected Object getAttributeValueFromBackupClone(Object backupClone) {
        return getAttributeValueFromObject(backupClone);
    }

    /**
     * Convenience method
     */
    protected ObjectBuilder getObjectBuilderForClass(Class javaClass, AbstractSession session) {
        return getReferenceDescriptor(javaClass, session).getObjectBuilder();
    }

    /**
     * Convenience method
     */
    protected ObjectBuilder getObjectBuilder(Object attributeValue, AbstractSession session) {
        return getReferenceDescriptor(attributeValue, session).getObjectBuilder();
    }

    /**
     * Convenience method
     */
    protected DescriptorQueryManager getQueryManager(Object attributeValue, AbstractSession session) {
        return getReferenceDescriptor(attributeValue, session).getQueryManager();
    }

    /**
     * PUBLIC:
     * Returns the reference class
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Used by MW.
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * Return the referenceDescriptor. This is a descriptor which is associated with the reference class.
     * NOTE: If you are looking for the descriptor for a specific aggregate object, use
     * #getReferenceDescriptor(Object). This will ensure you get the right descriptor if the object's
     * descriptor is part of an inheritance tree.
     */
    public ClassDescriptor getReferenceDescriptor() {
        return referenceDescriptor;
    }

    /**
     * INTERNAL:
     * For inheritance purposes.
     */
    protected ClassDescriptor getReferenceDescriptor(Class theClass, AbstractSession session) {
        if (getReferenceDescriptor().getJavaClass().equals(theClass)) {
            return getReferenceDescriptor();
        }

        ClassDescriptor subDescriptor = session.getDescriptor(theClass);
        if (subDescriptor == null) {
            throw DescriptorException.noSubClassMatch(theClass, this);
        } else {
            return subDescriptor;
        }
    }

    /**
     * Convenience method
     */
    protected ClassDescriptor getReferenceDescriptor(Object attributeValue, AbstractSession session) {
        if (attributeValue == null) {
            return getReferenceDescriptor();
        } else {
            return getReferenceDescriptor(attributeValue.getClass(), session);
        }
    }

    /**
     * INTERNAL:
     * Initialize the reference descriptor.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        if (getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }

        setReferenceDescriptor(session.getDescriptor(getReferenceClass()));

        ClassDescriptor refDescriptor = this.getReferenceDescriptor();
        if (refDescriptor == null) {
            session.getIntegrityChecker().handleError(DescriptorException.descriptorIsMissing(getReferenceClass().getName(), this));
            return;
        }
        if (refDescriptor.isAggregateDescriptor()) {
            refDescriptor.checkInheritanceTreeAggregateSettings(session, this);
        } else {
            session.getIntegrityChecker().handleError(DescriptorException.referenceDescriptorIsNotAggregate(getReferenceClass().getName(), this));
        }
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAggregateMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the appropriate attribute value.
     */
    public void iterate(DescriptorIterator iterator) {
        iterateOnAttributeValue(iterator, getAttributeValueFromObject(iterator.getVisitedParent()));
    }

    /**
     * Iterate on the specified attribute value.
     */
    protected void iterateOnAttributeValue(DescriptorIterator iterator, Object attributeValue) {
        iterator.iterateForAggregateMapping(attributeValue, this, getReferenceDescriptor(attributeValue, iterator.getSession()));
    }

    /**
     * Merge the attribute values.
     */
    protected void mergeAttributeValue(Object targetAttributeValue, boolean isTargetUnInitialized, Object sourceAttributeValue, MergeManager mergeManager) {
        // don't merge read-only attributes
        if (mergeManager.getSession().isClassReadOnly(sourceAttributeValue.getClass())) {
            return;
        }
        if (mergeManager.getSession().isClassReadOnly(targetAttributeValue.getClass())) {
            return;
        }

        // Toggle change tracking during the merge.
        ClassDescriptor descriptor = getReferenceDescriptor(sourceAttributeValue, mergeManager.getSession());
        descriptor.getObjectChangePolicy().dissableEventProcessing(targetAttributeValue);
        try {
            descriptor.getObjectBuilder().mergeIntoObject(targetAttributeValue, isTargetUnInitialized, sourceAttributeValue, mergeManager);
        } finally {            
            descriptor.getObjectChangePolicy().enableEventProcessing(targetAttributeValue);
        }
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     * With aggregates the merge must cascade to the object changes for the aggregate object
     * because aggregate objects have no identity outside of themselves.
     * The actual aggregate object does not need to be replaced, because even if the clone references
     * another aggregate it appears the same to TopLink
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        ObjectChangeSet aggregateChangeSet = (ObjectChangeSet)((AggregateChangeRecord)changeRecord).getChangedObject();
        if (aggregateChangeSet == null) {// the change was to set the value to null
            setAttributeValueInObject(target, null);
            return;
        }

        Object sourceAggregate = null;
        if (source != null) {
            sourceAggregate = getAttributeValueFromObject(source);
        }
        ObjectBuilder objectBuilder = getObjectBuilderForClass(aggregateChangeSet.getClassType(mergeManager.getSession()), mergeManager.getSession());
        //Bug#4719341  Always obtain aggregate attribute value from the target object regardless of new or not
        Object targetAggregate = getAttributeValueFromObject(target);
        if (targetAggregate == null) {
            targetAggregate = objectBuilder.buildNewInstance();
        } else {
            //update for bug 6451053 - check the aggregate change set for java type as this will always be set
            //even when merging from distributed server where sourceAggregate will be nulll
            if (aggregateChangeSet.getClassType(mergeManager.getSession()) != targetAggregate.getClass()) {
                targetAggregate = objectBuilder.buildNewInstance();
            }
        }
        objectBuilder.mergeChangesIntoObject(targetAggregate, aggregateChangeSet, sourceAggregate, mergeManager);
        setAttributeValueInObject(target, targetAggregate);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. This merge is only called when a changeSet for the target
     * does not exist or the target is uninitialized
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        Object sourceAttributeValue = getAttributeValueFromObject(source);
        if (sourceAttributeValue == null) {
            setAttributeValueInObject(target, null);
            return;
        }

        Object targetAttributeValue = getAttributeValueFromObject(target);
        if (targetAttributeValue == null) {
            // avoid null-pointer/nothing to merge to - create a new instance
            // (a new clone cannot be used as all changes must be merged)
            targetAttributeValue = buildNewMergeInstanceOf(sourceAttributeValue, mergeManager.getSession());
            mergeAttributeValue(targetAttributeValue, true, sourceAttributeValue, mergeManager);
            // setting new instance so fire event as if set was called by user.
            // this call will eventually get passed to updateChangeRecord which will 
            //ensure this new aggregates is fully initilized with listeners.
            this.getDescriptor().getObjectChangePolicy().raiseInternalPropertyChangeEvent(target, getAttributeName(), getAttributeValueFromObject(target), targetAttributeValue);
            
        } else {
            mergeAttributeValue(targetAttributeValue, isTargetUnInitialized, sourceAttributeValue, mergeManager);
        }

        // allow setter to re-morph any changes...
        setAttributeValueInObject(target, targetAttributeValue);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void postDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            postDeleteAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void postDeleteAttributeValue(DeleteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        DeleteObjectQuery aggregateQuery = buildAggregateDeleteQuery(query, attributeValue);
        getQueryManager(attributeValue, query.getSession()).postDelete(aggregateQuery);
        executeEvent(DescriptorEventManager.PostDeleteEvent, aggregateQuery);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            postInsertAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void postInsertAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        WriteObjectQuery aggregateQuery = buildAggregateWriteQuery(query, attributeValue);
        getQueryManager(attributeValue, query.getSession()).postInsert(aggregateQuery);
        executeEvent(DescriptorEventManager.PostInsertEvent, aggregateQuery);
        // aggregates do not actually use a query to write to the database so the post write must be called here
        executeEvent(DescriptorEventManager.PostWriteEvent, aggregateQuery);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            postUpdateAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void postUpdateAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ObjectChangeSet changeSet = null;
        UnitOfWorkChangeSet uowChangeSet = null;
        if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
            uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
            changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(attributeValue);
        }
        WriteObjectQuery aggregateQuery = buildAggregateWriteQuery(query, attributeValue);
        aggregateQuery.setObjectChangeSet(changeSet);
        getQueryManager(attributeValue, query.getSession()).postUpdate(aggregateQuery);
        executeEvent(DescriptorEventManager.PostUpdateEvent, aggregateQuery);
        // aggregates do not actually use a query to write to the database so the post write must be called here
        executeEvent(DescriptorEventManager.PostWriteEvent, aggregateQuery);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void preDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            preDeleteAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void preDeleteAttributeValue(DeleteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        DeleteObjectQuery aggregateQuery = buildAggregateDeleteQuery(query, attributeValue);
        executeEvent(DescriptorEventManager.PreDeleteEvent, aggregateQuery);
        getQueryManager(attributeValue, query.getSession()).preDelete(aggregateQuery);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void preInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            preInsertAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void preInsertAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        WriteObjectQuery aggregateQuery = buildAggregateWriteQuery(query, attributeValue);

        // aggregates do not actually use a query to write to the database so the pre-write must be called here
        if (query.getSession().usesOldCommit()) {
            // in new commit process these events will be thrown at changeset calc time
            executeEvent(DescriptorEventManager.PreWriteEvent, aggregateQuery);
            executeEvent(DescriptorEventManager.PreInsertEvent, aggregateQuery);
        }
        getQueryManager(attributeValue, query.getSession()).preInsert(aggregateQuery);
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    public void preUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isReadOnly()) {
            preUpdateAttributeValue(query, getAttributeValueFromObject(query.getObject()));
        }
    }

    /**
     * INTERNAL:
     * The message is passed to its reference class descriptor.
     */
    protected void preUpdateAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        WriteObjectQuery aggregateQuery = buildAggregateWriteQuery(query, attributeValue);
        ObjectChangeSet changeSet = null;
        UnitOfWorkChangeSet uowChangeSet = null;
        if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
            uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
            changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(aggregateQuery.getObject());
        }

        aggregateQuery.setObjectChangeSet(changeSet);
        // aggregates do not actually use a query to write to the database so the pre-write must be called here
        if (changeSet == null) {// then we didn't fire events at calculations
            executeEvent(DescriptorEventManager.PreWriteEvent, aggregateQuery);
            executeEvent(DescriptorEventManager.PreUpdateEvent, aggregateQuery);
        }
        getQueryManager(attributeValue, query.getSession()).preUpdate(aggregateQuery);
    }

    /**
     * INTERNAL:
     * Once a descriptor is serialized to the remote session, all its mappings and reference descriptors are traversed.
     * Usually the mappings are initialized and the serialized reference descriptors are replaced with local descriptors
     * if they already exist in the remote session.
     */
    public void remoteInitialization(DistributedSession session) {
        super.remoteInitialization(session);
        ClassDescriptor refDescriptor = getReferenceDescriptor();

        if (session.hasCorrespondingDescriptor(refDescriptor)) {
            ClassDescriptor correspondingDescriptor = session.getDescriptorCorrespondingTo(refDescriptor);
            setReferenceDescriptor(correspondingDescriptor);
        } else {
            session.privilegedAddDescriptor(refDescriptor);
            refDescriptor.remoteInitialization(session);
        }
    }

    /**
     * PUBLIC:
     * This is a reference class whose instances this mapping will store in the domain objects.
     */
    public void setReferenceClass(Class aClass) {
        referenceClass = aClass;
    }

    /**
     * INTERNAL:
     * Used by MW.
     */
    public void setReferenceClassName(String aClassName) {
        referenceClassName = aClassName;
    }

    /**
     * INTERNAL:
     * Set the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    protected void setReferenceDescriptor(ClassDescriptor aDescriptor) {
        referenceDescriptor = aDescriptor;
    }

    /**
     * INTERNAL:
     * Either create a new change record or update the change record with the new value.
     * This is used by attribute change tracking.
     */
    public void updateChangeRecord(Object sourceClone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        //This method will be called when either the referenced aggregate has 
        //been changed or a component of the referenced aggregate has been changed
        //this case is determined by the value of the sourceClone 
        
        AggregateChangeRecord changeRecord = (AggregateChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null){
            changeRecord = new AggregateChangeRecord(objectChangeSet);
            changeRecord.setAttribute(this.getAttributeName());
            changeRecord.setMapping(this);
            objectChangeSet.addChange(changeRecord);
        }
        
        if ( sourceClone.getClass().equals(objectChangeSet.getClassType(uow)) ) {
            // event was fired on the parent to the aggregate, the attribute value changed.
            ClassDescriptor referenceDescriptor = getReferenceDescriptor(newValue, uow);
            if ( newValue == null ) { // attribute set to null
                changeRecord.setChangedObject(null);
                if (referenceDescriptor.getObjectChangePolicy().isAttributeChangeTrackingPolicy()){
                    //need to detach listener
                    ((AggregateAttributeChangeListener)((ChangeTracker)oldValue)._persistence_getPropertyChangeListener()).setParentListener(null);
                }
                return;
            }else{ // attribute set to new aggregate
                UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)objectChangeSet.getUOWChangeSet();
                //force comparison change detection to build changeset.
                ObjectChangeSet aggregateChangeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(newValue);
                if (aggregateChangeSet != null) {
                    aggregateChangeSet.clear(); // old differences must be thrown away because difference is between old value and new value
                }
                //make sure the listener is initialized
                if (referenceDescriptor.getObjectChangePolicy().isAttributeChangeTrackingPolicy()){
                    //need to detach listener
                    ((AggregateAttributeChangeListener)((ChangeTracker)oldValue)._persistence_getPropertyChangeListener()).setParentListener(null);
                    //need to attach new listener.
                    AggregateAttributeChangeListener newListener = (AggregateAttributeChangeListener)((ChangeTracker)newValue)._persistence_getPropertyChangeListener();
                    if (newListener == null){
                        newListener = new AggregateAttributeChangeListener(referenceDescriptor, uow, ((AttributeChangeListener)((ChangeTracker)sourceClone)._persistence_getPropertyChangeListener()), this.getAttributeName(), newValue);
                        ((ChangeTracker)newValue)._persistence_setPropertyChangeListener(newListener);
                    }
                    newListener.setParentListener((AttributeChangeListener)((ChangeTracker)sourceClone)._persistence_getPropertyChangeListener());
                }
                //force comparison change detection to build changeset.
                changeRecord.setChangedObject(referenceDescriptor.getObjectChangePolicy().createObjectChangeSetThroughComparison(newValue,oldValue, uowChangeSet, (oldValue == null), uow, referenceDescriptor));
                referenceDescriptor.getObjectChangePolicy().setChangeSetOnListener((ObjectChangeSet)changeRecord.getChangedObject(), newValue);
            }
        } else {
            //a value was set on the aggregate but the aggregate was not changed.
            if (referenceDescriptor.getObjectChangePolicy().isAttributeChangeTrackingPolicy()){
                //The aggregate that is referenced is Attribute Change Tracked as well.
                changeRecord.setChangedObject(((AggregateAttributeChangeListener)((ChangeTracker)sourceClone)._persistence_getPropertyChangeListener()).getObjectChangeSet());
            } else {
                // not tracked at attribute level, lets force build a changeset then.
                changeRecord.setChangedObject(referenceDescriptor.getObjectChangePolicy().createObjectChangeSetThroughComparison(sourceClone, null, (UnitOfWorkChangeSet)objectChangeSet.getUOWChangeSet(), true, uow, referenceDescriptor));
            }
        }
    }
    
    /**
     * INTERNAL:
     * Return whether the specified object and all its components have been deleted.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        return verifyDeleteOfAttributeValue(getAttributeValueFromObject(object), session);
    }

    /**
     * INTERNAL:
     * Return whether the specified object and all its components have been deleted.
     */
    protected boolean verifyDeleteOfAttributeValue(Object attributeValue, AbstractSession session) throws DatabaseException {
        if (attributeValue == null) {
            return true;
        }
        for (Enumeration mappings = getReferenceDescriptor(attributeValue, session).getMappings().elements();
                 mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();
            if (!mapping.verifyDelete(attributeValue, session)) {
                return false;
            }
        }
        return true;
    }
}
