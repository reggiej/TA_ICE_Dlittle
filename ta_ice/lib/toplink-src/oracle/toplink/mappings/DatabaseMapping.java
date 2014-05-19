// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;

import java.beans.PropertyChangeListener;

import java.io.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.indirection.*;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.internal.descriptors.*;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.indirection.*;
import oracle.toplink.internal.queryframework.*;
import oracle.toplink.internal.remote.*;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.remote.*;
import oracle.toplink.sessions.ObjectCopyingPolicy;
import oracle.toplink.sessions.Project;

/**
 * <p><b>Purpose</b>: Defines how an attribute of an object maps to and from the database
 *
 * <p><b>Responsibilities</b>:<ul>
 * <li> Define type of relationship (1:1/1:M/M:M/etc.)
 * <li> Define instance variable name and fields names required
 * <li> Define any additional properties (ownership, indirection, read only, etc.)
 * <li> Control building the value for the instance variable from the database row
 * <li> Control building the database fields from the object
 * <li> Control any pre/post updating/inserting/deleting required to maintain the relationship
 * <li> Merges object changes for unit of work.
 * <li> Clones objects for unit of work.
 * <li> cache computed information to optimize performance
 * </ul>
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public abstract class DatabaseMapping implements Cloneable, Serializable {

    /** Used to reduce memory for mappings with no fields. */
    protected static final Vector NO_FIELDS = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(0);

    /** Used to share integer instance to reduce memory. */
    protected static final Integer NO_WEIGHT = new Integer(Integer.MAX_VALUE);
    protected static final Integer WEIGHT_1 = new Integer(1);

    /** ClassDescriptor to which this mapping belongs to */
    protected ClassDescriptor descriptor;

    /** Wrapper to store the reference objects. */
    protected AttributeAccessor attributeAccessor;

    /** Makes this mapping read only. No write are performed on it. Default is false */
    protected boolean isReadOnly;
    
    /** Specifies whether this mapping is optional (i.e. field may be null). Used for DDL generation. */
    protected boolean isOptional;
    
    /** Specifies whether this mapping is lazy, this means not included in the default fetch group. */
    protected boolean isLazy;

    /** Fields associated with the mappings are cached */
    protected Vector<DatabaseField> fields;

    /** It is needed only in remote initialization and mapping is in parent descriptor */
    protected boolean isRemotelyInitialized;

    /** This is a TopLink defined attribute that allows us to sort the mappings */
    protected Integer weight = NO_WEIGHT;

    /** Allow user defined properties. */
    protected Map properties;

    /**
     * PERF: Used as a quick check to see if this mapping is a primary key mapping,
     * set by the object builder during initialization.
     */
    protected boolean primaryKeyMapping = false;
    
    /**
     * PERF: Cache the mappings attribute name.
     */
    protected String attributeName;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public DatabaseMapping() {
        this.isOptional = true;
        this.isReadOnly = false;
        this.attributeAccessor = new InstanceVariableAttributeAccessor();
    }

    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public abstract void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork);

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     */
    public Object buildBackupCloneForPartObject(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        throw DescriptorException.invalidMappingOperation(this, "buildBackupCloneForPartObject");
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public abstract void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork);

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
    public abstract void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession);

    /**
     * INTERNAL:
     * Builds a shallow original object.  Only direct attributes and primary
     * keys are populated.  In this way the minimum original required for
     * instantiating a working copy clone can be built without placing it in
     * the shared cache (no concern over cycles).
     */
    public void buildShallowOriginalFromRow(AbstractRecord databaseRow, Object original, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
        return;
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     */
    public Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        throw DescriptorException.invalidMappingOperation(this, "buildCloneForPartObject");
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
    }

    /**
     * INTERNAL:
     * In case Query By Example is used, this method builds and returns an expression that
     * corresponds to a single attribue and it's value.
     */
    public Expression buildExpression(Object queryObject, QueryByExamplePolicy policy, Expression expressionBuilder, IdentityHashtable processedObjects, AbstractSession session) {
        return null;
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     */
    public Expression buildObjectJoinExpression(Expression base, Object value, AbstractSession session) {
        throw QueryException.unsupportedMappingForObjectComparison(this, base);
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     */
    public Expression buildObjectJoinExpression(Expression base, Expression argument, AbstractSession session) {
        throw QueryException.unsupportedMappingForObjectComparison(this, base);
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    abstract public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects);

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    abstract public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects);

    /**
     * INTERNAL:
     * Cascade discover and persist new objects during commit.
     */
    public void cascadeDiscoverAndPersistUnregisteredNewObjects(Object object, IdentityHashtable newObjects, IdentityHashtable unregisteredExistingObjects, IdentityHashtable visitedObjects, UnitOfWorkImpl uow) {
        // Do nothing by default, (direct and xml mappings do not require anything).
    }
    
    /**
     * INTERNAL:
     * Used by AttributeLevelChangeTracking to update a changeRecord with calculated changes
     * as apposed to detected changes.  If an attribute can not be change tracked it's
     * changes can be detected through this process.
     */
    public void calculateDeferredChanges(ChangeRecord changeRecord, AbstractSession session){
        throw DescriptorException.invalidMappingOperation(this, "calculatedDeferredChanges");
    }
    
    /**
     * INTERNAL:
     * Cascade the merge to the component object, if appropriate.
     */
    public void cascadeMerge(Object sourceElement, MergeManager mergeManager) {
        throw DescriptorException.invalidMappingOperation(this, "cascadeMerge");
    }

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        // Bug 3037701 - clone the AttributeAccessor
        DatabaseMapping mapping = null;
        try {
            mapping = (DatabaseMapping)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        mapping.setAttributeAccessor((AttributeAccessor)attributeAccessor.clone());
        return mapping;
    }

    /**
     * INTERNAL:
     * Helper method to clone vector of fields (used in aggregate initialization cloning).
     */
    protected Vector cloneFields(Vector fields) {
        Vector clonedFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        for (Enumeration fieldsEnum = fields.elements(); fieldsEnum.hasMoreElements();) {
            clonedFields.addElement(((DatabaseField)fieldsEnum.nextElement()).clone());
        }

        return clonedFields;
    }

    /**
     * This method must be overwritten in the subclasses to return a vector of all the
     * fields this mapping represents.
     */
    protected Vector<DatabaseField> collectFields() {
        return NO_FIELDS;
    }

    /**
     * INTERNAL:
     * This method was created in VisualAge.
     * @return prototype.changeset.ChangeRecord
     */
    abstract public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session);

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public abstract boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session);

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){};

    /**
     * INTERNAL:
     * Builder the unit of work value holder.
     * @param buildDirectlyFromRow indicates that we are building the clone directly
     * from a row as opposed to building the original from the row, putting it in
     * the shared cache, and then cloning the original.
     */
    public UnitOfWorkValueHolder createUnitOfWorkValueHolder(ValueHolderInterface attributeValue, Object original, Object clone, AbstractRecord row, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        throw DescriptorException.invalidMappingOperation(this, "createUnitOfWorkValueHolder");
    }

    /**
     * INTERNAL:
     * Extract the nested attribute expressions that apply to this mapping.
     * This is used for partial objects and joining.
     * @param rootExpressionsAllowed true if newRoot itself can be one of the
     * expressions returned
     */
    protected Vector extractNestedExpressions(List expressions, ExpressionBuilder newRoot, boolean rootExpressionsAllowed) {
        Vector nestedExpressions = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(expressions.size());

        for (Iterator expressionsEnum = expressions.iterator();
                 expressionsEnum.hasNext();) {
            Expression next = (Expression)expressionsEnum.next();

            // The expressionBuilder can be one of the locked expressions in
            // the ForUpdateOfClause.
            if (!next.isQueryKeyExpression()) {
                continue;
            }
            QueryKeyExpression expression = (QueryKeyExpression)next;
            QueryKeyExpression base = expression;
            boolean afterBase = false;
            while (!base.getBaseExpression().isExpressionBuilder()) {
                afterBase = true;
                base = (QueryKeyExpression)base.getBaseExpression();
            }
            if (afterBase && base.getName().equals(getAttributeName())) {
                nestedExpressions.addElement(expression.rebuildOn(base, newRoot));
            } else if (rootExpressionsAllowed && expression.getBaseExpression().isExpressionBuilder() && expression.getName().equals(getAttributeName())) {
                nestedExpressions.addElement(newRoot);
            }
        }
        return nestedExpressions;
    }

    /**
     * INTERNAL:
     * If there is root expression in the list then indicates whether it shouldUseOuterJoin,
     * otherwise return false.
     */
    protected boolean hasRootExpressionThatShouldUseOuterJoin(List expressions) {
        for (Iterator expressionsEnum = expressions.iterator();
                 expressionsEnum.hasNext();) {
            Expression next = (Expression)expressionsEnum.next();

            // The expressionBuilder can be one of the locked expressions in
            // the ForUpdateOfClause.
            if (!next.isQueryKeyExpression()) {
                continue;
            }
            QueryKeyExpression expression = (QueryKeyExpression)next;
            if (expression.getBaseExpression().isExpressionBuilder() && expression.getName().equals(getAttributeName())) {
                return expression.shouldUseOuterJoin();
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * An object has been serialized from the server to the client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    public abstract void fixObjectReferences(Object object, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session);

    /**
     * INTERNAL:
     * At this point, we realize we don't have indirection;
     * so we need to replace the reference object(s) with
     * the corresponding object(s) from the remote session.
     * The default is to do nothing.
     */
    public void fixRealObjectReferences(Object object, IdentityHashtable objectInformation, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
        // do nothing
    }

    /**
     * ADVANCED:
     * Return the attributeAccessor.
     * The attribute accessor is responsible for setting and retrieving the attribute value
     * from the object for this mapping.
     */
    public AttributeAccessor getAttributeAccessor() {
        return attributeAccessor;
    }

    /**
     * PUBLIC:
     * The classification type for the attribute this mapping represents
     */
    public Class getAttributeClassification() {
        return null;
    }

    /**
     * PUBLIC:
     * Return the name of the attribute set in the mapping.
     * The attribute name on the attributeAccessor will always override any attribute already set
     */
    public String getAttributeName() {
    	// Use the attributeAccessor attribute over the current attribute name
        if (attributeName == null) {
            attributeName = getAttributeAccessor().getAttributeName();
        }
        return attributeName;
    }

    /**
     * INTERNAL:
     * Return the value of an attribute which this mapping represents for an object.
     */
    public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        try {
            // PERF: direct-access.
            return this.attributeAccessor.getAttributeValueFromObject(object);
        } catch (DescriptorException exception) {
            exception.setMapping(this);
            throw exception;
        }
    }

    /**
     * INTERNAL:
     * Return the mapping's containerPolicy.
     */
    public ContainerPolicy getContainerPolicy() {
        throw DescriptorException.invalidMappingOperation(this, "getContainerPolicy");
    }

    /**
     * INTERNAL:
     * Return the descriptor to which this mapping belongs
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * Return the field associated with this mapping if there is exactly one.
     * This is required for object relational mapping to print them, but because
     * they are defined in Enterprise they cannot be cast to.
     * Mappings that have a field include direct mappings and object relational mappings.
     */
    public DatabaseField getField() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent java value.
     * By default this is unknown.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) {
        return null;
    }

    /**
     * INTERNAL:
     * Returns the set of fields that should be selected to build this mapping's value(s).
     * This is used by expressions to determine which fields to include in the select clause for non-object expressions.
     */
    public Vector getSelectFields() {
        return getFields();
    }
    
    /**
     * INTERNAL:
     * Returns the table(s) that should be selected to build this mapping's value(s).
     * This is used by expressions to determine which tables to include in the from clause for non-object expressions.
     */
    public Vector getSelectTables() {
        return new NonSynchronizedVector(0);
    }
    
    /**
     * INTERNAL:
     * Returns a vector of all the fields this mapping represents.
     */
    public Vector<DatabaseField> getFields() {
        return this.fields;
    }

    /**
     * PUBLIC:
     * This method is invoked reflectively on the reference object to return the value of the
     * attribute in the object. This method returns the name of the getMethodName or null if not using method access.
     */
    public String getGetMethodName() {
        if (!(getAttributeAccessor() instanceof MethodAttributeAccessor)) {
            return null;
        }
        return ((MethodAttributeAccessor)getAttributeAccessor()).getGetMethodName();
    }

    /**
     * INTERNAL:
     * return the object on the client corresponding to the specified object.
     * The default is to simply return the object itself, without worrying about
     * maintaining object identity.
     */
    public Object getObjectCorrespondingTo(Object object, RemoteSession session, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query) {
        return object;
    }

    /**
     * INTERNAL:
     * used as a temporary store for custom SDK usage
     */
    public Map getProperties() {
        if (properties == null) {//Lazy initialize to conserve space and allocation time.
            properties = new HashMap(5);
        }
        return properties;
    }

    /**
     * ADVANCED:
     * Allow user defined properties.
     */
    public Object getProperty(Object property) {
        if (properties == null) {
            return null;
        }

        return getProperties().get(property);
    }

    /**
     * INTERNAL:
     * Return the value of an attribute unwrapping value holders if required.
     */
    public Object getRealAttributeValueFromObject(Object object, AbstractSession session) throws DescriptorException {
        return getAttributeValueFromObject(object);
    }

    /**
     * INTERNAL:
     * Return the value of an attribute, unwrapping value holders if necessary.
     * If the value is null, build a new container.
     */
    public Object getRealCollectionAttributeValueFromObject(Object object, AbstractSession session) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "getRealCollectionAttributeValueFromObject");
    }

    /**
     * PUBLIC:
     * Return the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    public ClassDescriptor getReferenceDescriptor() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     */
    public DatabaseMapping getRelationshipPartner() {
        return null;
    }

    /**
     * PUBLIC:
     * This method is invoked reflectively on the reference object to set the value of the
     * attribute in the object. This method returns the name of the setMethodName or null if not using method access.
     */
    public String getSetMethodName() {
        if (!(getAttributeAccessor() instanceof MethodAttributeAccessor)) {
            return null;
        }
        return ((MethodAttributeAccessor)getAttributeAccessor()).getSetMethodName();
    }

    /**
     * INTERNAL:
     * extract and return the appropriate value from the
     * specified remote value holder
     */
    public Object getValueFromRemoteValueHolder(RemoteValueHolder remoteValueHolder) {
        throw DescriptorException.invalidMappingOperation(this, "getValueFromRemoteValueHolder");
    }

    /**
     * INTERNAL:
     * Return the weight of the mapping, used to sort mappings to ensure that
     * DirectToField Mappings get merged first
     */
    public Integer getWeight() {
        return this.weight;
    }

    /**
     * INTERNAL:
     * The returns if the mapping has any constraint dependencies, such as foreign keys and join tables.
     */
    public boolean hasConstraintDependency() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if method access is used.
     */
    public boolean isUsingMethodAccess() {
        return getAttributeAccessor() instanceof MethodAttributeAccessor;
    }

    /**
     * INTERNAL:
     * Return if the mapping has any ownership or other dependency over its target object(s).
     */
    public boolean hasDependency() {
        return isPrivateOwned();
    }

    /**
     * INTERNAL:
     * The returns if the mapping has any inverse constraint dependencies, such as foreign keys and join tables.
     */
    public boolean hasInverseConstraintDependency() {
        return false;
    }

    /**
     * INTERNAL:
     * Allow for initialization of properties and validation.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        ;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAggregateCollectionMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAggregateMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAggregateObjectMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isCollectionMapping() {
        return false;
    }

    /**
     * INTERNAL:
     */
    public boolean isDatabaseMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isDirectCollectionMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isDirectMapMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isDirectToFieldMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isForeignReferenceMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isManyToManyMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isNestedTableMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isObjectReferenceMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isObjectTypeMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isOneToManyMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isOneToOneMapping() {
        return false;
    }

    /**
     * Return whether the value of this mapping is optional (that is, can be 
     * null). This is a hint and is used when generating DDL.
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Return if this mapping is lazy.
     * This can be used for any mapping type to exclude it from the descriptor's
     * default fetch group.  This means that queries will not include the field(s) required
     * by this mapping by default.
     * This can only be used if the descriptor has a FetchGroupManager and class implements
     * the FetchGroupTracker interface (or is weaved).
     * This is not the same as indirection on relationships (lazy relationships),
     * as it defers the loading of the source object fields, not the relationship.
     */
    public boolean isLazy() {
        return isLazy;
    }
    
    /**
     * Set if this mapping is lazy.
     * This can be used for any mapping type to exclude it from the descriptor's
     * default fetch group.  This means that queries will not include the field(s) required
     * by this mapping by default.
     * This can only be used if the descriptor has a FetchGroupManager and class implements
     * the FetchGroupTracker interface (or is weaved).
     * This is not the same as indirection on relationships (lazy relationships),
     * as it defers the loading of the source object fields, not the relationship.
     */
    public void setIsLazy(boolean isLazy) {
        this.isLazy = isLazy;
    }
    
    /**
     * INTERNAL:
     * All EIS mappings should implement this method to return true.
     */
    public boolean isEISMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * All relational mappings should implement this method to return true.
     */
    public boolean isRelationalMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * All relational mappings should implement this method to return true.
     */
    public boolean isXMLMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAbstractDirectMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAbstractCompositeDirectCollectionMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAbstractCompositeObjectMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAbstractCompositeCollectionMapping() {
        return false;
    }
    /**
     * INTERNAL:
     * Return if this mapping support joining.
     */
    public boolean isJoiningSupported() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return if this mapping requires its attribute value to be cloned.
     */
    public boolean isCloningRequired() {
        return true;
    }

    /**
     * INTERNAL:
     * Set by the Object builder during initialization returns true if this mapping
     * is used as a primary key mapping.
     */
    public boolean isPrimaryKeyMapping() {
        return this.primaryKeyMapping;
    }
    /**
     * INTERNAL:
     * Used when determining if a mapping supports cascaded version optimistic
     * locking.
     */
    public boolean isCascadedLockingSupported() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported(Project project) {
        return false;
    }
    /**
     * INTERNAL:
     * Return if the mapping has ownership over its target object(s).
     */
    public boolean isPrivateOwned() {
        return false;
    }

    /**
     * INTERNAL:
     * Returns true if mapping is read only else false.
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isReferenceMapping() {
        return false;
    }

    protected boolean isRemotelyInitialized() {
        return isRemotelyInitialized;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isSerializedObjectMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isStructureMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isTransformationMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isTypeConversionMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isVariableOneToOneMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isDirectToXMLTypeMapping() {
        return false;
    }

    /**
     * INTERNAL:
     * Some mappings support no attribute (transformation).
     */
    public boolean isWriteOnly() {
        return false;
    }

    /**
     * INTERNAL:
     * Iterate on the appropriate attribute value.
     */
    public abstract void iterate(DescriptorIterator iterator);

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     */
    public void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue) {
        throw DescriptorException.invalidMappingOperation(this, "iterateOnRealAttributeValue");
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public abstract void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager);

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public abstract void mergeIntoObject(Object target, boolean isTargetUninitialized, Object source, MergeManager mergeManager);

    /**
     * INTERNAL:
     * Perform the commit event.
     * This is used in the uow to delay data modifications.
     */
    public void performDataModificationEvent(Object[] event, AbstractSession session) throws DatabaseException, DescriptorException {
        throw DescriptorException.invalidDataModificationEvent(this);
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to delete the reference objects after the actual object is deleted.
     */
    public void postDelete(DeleteObjectQuery query) throws DatabaseException {
        return;
    }

    /**
     * INTERNAL:
     * Allow for initialization of properties and validation that have dependecies no the descriptor
     * being initialized.
     */
    public void postInitialize(AbstractSession session) throws DescriptorException {
        // Nothing by default.
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to insert the reference objects after the actual object is inserted.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException {
        return;
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to update the reference objects after the actual object is updated.
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException {
        return;
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to delete the reference objects before the actual object is deleted.
     */
    public void preDelete(DeleteObjectQuery query) throws DatabaseException {
        return;
    }

    /**
     * INTERNAL:
     * Allow for initialization of properties and validation.
     */
    public void preInitialize(AbstractSession session) throws DescriptorException {
        try {
            getAttributeAccessor().initializeAttributes(getDescriptor().getJavaClass());
        } catch (DescriptorException exception) {
            exception.setMapping(this);
            session.getIntegrityChecker().handleError(exception);
        }
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to insert the reference objects before the actual object is inserted.
     */
    public void preInsert(WriteObjectQuery query) throws DatabaseException {
        return;
    }
    
    /**
     * INTERNAL:
     * A subclass that supports cascade version optimistic locking should 
     * implement this method to properly prepare the locking policy for their 
     * mapping type.
     * Guy
     */
    public void prepareCascadeLockingPolicy() {
        return;
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Recurse thru the parts to update the reference objects before the actual object is updated.
     */
    public void preUpdate(WriteObjectQuery query) throws DatabaseException {
        return;
    }

    /**
     * INTERNAL:
     * Extract value from the row and set the attribute to this value in the object.
     * return value as this value will have been converted to the appropriate type for
     * the object.
     */
    public Object readFromRowIntoObject(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object targetObject, ObjectBuildingQuery sourceQuery) throws DatabaseException {
        // This version can be called directly for reading a sequence number
        // field, a write lock value, or a return row into an object, and hence
        // the query is just a placeholder.  Getting the correct execution
        // session will generate an exception, so just pass in any session.
        // In general call this version only if no field conversion needed.
        return readFromRowIntoObject(databaseRow, joinManager, targetObject, sourceQuery, sourceQuery.getSession());
    }

    /**
     * INTERNAL:
     * Extract value from the row and set the attribute to this value in the object.
     * return value as this value will have been converted to the appropriate type for
     * the object.
     */
    public Object readFromRowIntoObject(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object targetObject, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        Object attributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);
        setAttributeValueInObject(targetObject, attributeValue);
        return attributeValue;
    }    

    /**
     * INTERNAL:
     * Extract values directly from the result-set.
     * PERF: This is used for optimized object building directly from the result-set.
     */
    public Object readFromResultSetIntoObject(ResultSet resultSet, Object targetObject, ObjectBuildingQuery query, AbstractSession session, DatabaseAccessor accessor, ResultSetMetaData metaData, int columnNumber, DatabasePlatform platform) throws DatabaseException {
        Object attributeValue = valueFromResultSet(resultSet, query, session, accessor, metaData, columnNumber, platform);
        setAttributeValueInObject(targetObject, attributeValue);
        return attributeValue;
    }
    
    /**
     * PUBLIC:
     * To make mapping read only.
     * Read-only mappings can be used if two attributes map to the same field.
     * Read-only mappings cannot be used for the primary key or other required fields.
     */
    public void readOnly() {
        setIsReadOnly(true);
    }

    /**
     * PUBLIC:
     * The mapping can be dynamically made either readOnly or readWriteOnly. This makes mapping go back to
     * default mode.
     */
    public void readWrite() {
        setIsReadOnly(false);
    }

    /**
     * INTERNAL:
     * Rehash any hashtables based on fields.
     * This is used to clone descriptors for aggregates, which hammer field names,
     * it is probably better not to hammer the field name and this should be refactored.
     */
    public void rehashFieldDependancies(AbstractSession session) {
        // Should be overwritten by any mapping with fields.
    }

    /**
     * INTERNAL:
     * Once descriptors are serialized to the remote session. All its mappings and reference descriptors are traversed. Usually
     * mappings are initilaized and serialized reference descriptors are replaced with local descriptors if they already exist on the
     * remote session.
     */
    public void remoteInitialization(DistributedSession session) {
        // Remote mappings is initilaized here again because while serializing only the uninitialized data is passed
        // as the initialized data is not serializable.
        if (!isRemotelyInitialized()) {
            getAttributeAccessor().initializeAttributes(getDescriptor().getJavaClass());
            remotelyInitialized();
        }
    }

    /**
     * Set the mapping to be initialized for the remote session.
     */
    protected void remotelyInitialized() {
        isRemotelyInitialized = true;
    }

    /**
     * INTERNAL:
     * replace the value holders in the specified reference object(s)
     */
    public IdentityHashtable replaceValueHoldersIn(Object object, RemoteSessionController controller) {
        // by default, do nothing
        return null;
    }

    /**
     * ADVANCED:
     * Set the attributeAccessor.
     * The attribute accessor is responsible for setting and retrieving the attribute value
     * from the object for this mapping.
     * This can be set to an implementor of AttributeAccessor if the attribute
     * requires advanced conversion of the mapping value, or a real attribute does not exist.
     */
    public void setAttributeAccessor(AttributeAccessor anAttributeAccessor) {
        String anAttributeName = getAttributeName();
        attributeAccessor = anAttributeAccessor;
        if (anAttributeAccessor.getAttributeName() == null) {
            anAttributeAccessor.setAttributeName(anAttributeName);
        }
        // Clear the mapping attribute name until a getAttributeName() call copies the accessor attributeName
        this.attributeName = null;
    }

    /**
     * PUBLIC:
     * Sets the name of the attribute in the mapping.
     */
    public void setAttributeName(String attributeName) {
        getAttributeAccessor().setAttributeName(attributeName);
        // Clear the mapping attribute name until a getAttributeName() call copies the accessor attributeName        
        this.attributeName = null;
    }

    /**
     * INTERNAL:
     * Set the value of the attribute mapped by this mapping.
     */
    public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        // PERF: Direct variable access.
        try {
            this.attributeAccessor.setAttributeValueInObject(object, value);
        } catch (DescriptorException exception) {
            exception.setMapping(this);
            throw exception;
        }
    }

    /**
     * INTERNAL:
     * Set the value of the attribute mapped by this mapping,
     * placing it inside a value holder if necessary.
     */
    public void setRealAttributeValueInObject(Object object, Object value) throws DescriptorException {
        try {
            this.setAttributeValueInObject(object, value);
        } catch (DescriptorException exception) {
            exception.setMapping(this);
            throw exception;
        }
    }

    /**
     * INTERNAL:
     * Set the descriptor to which this mapping belongs
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * INTERNAL:
     * Set the mapping's field collection.
     */
    protected void setFields(Vector<DatabaseField> fields) {
        this.fields = fields;
    }

    /**
     * PUBLIC:
     * This method is invoked reflectively on the reference object to return the value of the
     * attribute in the object. This method sets the name of the getMethodName.
     */
    public void setGetMethodName(String methodName) {
        if (methodName == null) {
            return;
        }

        // This is done because setting attribute name by defaults create InstanceVariableAttributeAccessor	
        if (getAttributeAccessor() instanceof InstanceVariableAttributeAccessor) {
            String attributeName = this.attributeAccessor.getAttributeName();
            setAttributeAccessor(new MethodAttributeAccessor());
            getAttributeAccessor().setAttributeName(attributeName);
        }

        ((MethodAttributeAccessor)getAttributeAccessor()).setGetMethodName(methodName);
    }

    /**
     * Used to specify whether the value of this mapping may be null.
     * This is used when generating DDL.
     */
    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }
    
    /**
     * INTERNAL:
     * Set by the Object builder during initialization returns true if this mapping
     * is used as a primary key mapping.
     */
    public void setIsPrimaryKeyMapping(boolean pkMapping) {
        this.primaryKeyMapping = pkMapping;
    }

    /**
     * PUBLIC:
     * Set this mapping to be read only.
     * Read-only mappings can be used if two attributes map to the same field.
     * Read-only mappings cannot be used for the primary key or other required fields.
     */
    public void setIsReadOnly(boolean aBoolean) {
        isReadOnly = aBoolean;
    }

    /**
     * INTERNAL:
     * Allow user defined properties.
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * ADVANCED:
     * Allow user defined properties.
     */
    public void setProperty(Object property, Object value) {
        getProperties().put(property, value);
    }

    /**
     * PUBLIC:
     * Set the methodName used to set the value for the mapping's attribute into the object.
     */
    public void setSetMethodName(String methodName) {
        if (methodName == null) {
            return;
        }

        // This is done because setting attribute name by defaults create InstanceVariableAttributeAccessor		
        if (!(getAttributeAccessor() instanceof MethodAttributeAccessor)) {
            String attributeName = this.attributeAccessor.getAttributeName();
            setAttributeAccessor(new MethodAttributeAccessor());
            getAttributeAccessor().setAttributeName(attributeName);
        }

        ((MethodAttributeAccessor)getAttributeAccessor()).setSetMethodName(methodName);
    }

    /**
     * ADVANCED:
     * Set the weight of the mapping, used to sort mappings
     * DirectToField Mappings have a default weight of 1 while all other Mappings have a
     * default weight of MAXINT.  Ordering of Mappings can be achieved by setting the weight of
     * a particular mapping to a value within the above mentioned limits.  By ordering mappings
     * the user can control what order relationships are processed by TopLink.
     */

    // CR 4097
    public void setWeight(Integer newWeight) {
        this.weight = newWeight;
    }

    /**
     * ADVANCED:
     * This method is used to add an object to a collection once the changeSet is applied.
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "simpleAddToCollectionChangeRecord");
    }

    /**
     * ADVANCED:
     * This method is used to remove an object from a collection once the changeSet is applied.
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "simpleRemoveFromCollectionChangeRecord");
    }

    /**
     * INTERNAL:
     * Print the mapping attribute name, this is used in error messages.
     */
    public String toString() {
        return getClass().getName() + "[" + getAttributeName() + "]";
    }

    /**
     * INTERNAL:
     * Allow for subclasses to perform validation.
     */
    public void validateAfterInitialization(AbstractSession session) throws DescriptorException {
    }

    /**
     * INTERNAL:
     * Allow for subclasses to perform validation.
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
    }

    /**
     * INTERNAL:
     * A subclass should extract the value from the object for the field, if it does not map the field then
     * it should return null.
     * Return the Value from the object.
     */
    public Object valueFromObject(Object anObject, DatabaseField field, AbstractSession session) {
        return null;
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Returns the value for the mapping from the database row.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query) throws DatabaseException {
        return valueFromRow(row, joinManager, query, query.getExecutionSession());
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Returns the value for the mapping from the database row.
     * The execution session is the session the query was executed on,
     * and its platform should be used for data conversion.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession session) throws DatabaseException {
        return null;
    }
    
    /**
     * INTERNAL:
     * Returns the value for the mapping directly from the result-set.
     * PERF: Used for optimized object building.
     */
    public Object valueFromResultSet(ResultSet resultSet, ObjectBuildingQuery query, AbstractSession session, DatabaseAccessor accessor, ResultSetMetaData metaData, int columnNumber, DatabasePlatform platform) throws DatabaseException {
        throw DescriptorException.invalidMappingOperation(this, "valueFromResultSet");
    }

    /**
     * INTERNAL:
     * To verify if the specified object has been deleted or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        return true;
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the foreign key values from the attribute to the row.
     */
     
    public void writeFromAttributeIntoRow(Object attribute, AbstractRecord row, AbstractSession session) 
    {
        // Do nothing by default.
    }
    
    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the attribute value from the object to the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        // Do nothing by default.
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     */
    public void writeFromObjectIntoRowForShallowInsert(Object object, AbstractRecord row, AbstractSession session) {
        writeFromObjectIntoRow(object, row, session);
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the attribute value from the object to the row.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        // Do nothing by default.
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     */
    public void writeFromObjectIntoRowForShallowInsertWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        writeFromObjectIntoRowWithChangeRecord(changeRecord, row, session);
    }

    /**
     * INTERNAL:
     * Write the attribute value from the object to the row for update.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery query, AbstractRecord row) {
        writeFromObjectIntoRow(query.getObject(), row, query.getSession());
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the attribute value from the object to the row.
     */
    public void writeFromObjectIntoRowForWhereClause(ObjectLevelModifyQuery query, AbstractRecord row) {
        Object object;
        if (query.isDeleteObjectQuery()) {
            object = query.getObject();
        } else {
            object = query.getBackupClone();
        }
        writeFromObjectIntoRow(object, row, query.getSession());
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        // Do nothing by default.
    }

    /**
     * INTERNAL:
     * Write fields needed for update into the template for with null values.
     * By default inserted fields are used.
     */
    public void writeUpdateFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        writeInsertFieldsIntoRow(databaseRow, session);
    }

    /**
     * INTERNAL:
     * Either create a new change record or update the change record with the new value.
     * This is used by attribute change tracking.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "updateChangeRecord");
    }
    
    /**
     * INTERNAL:
     * Set the change listener if required.
     * This is required for collections and aggregates or other change tracked mutable objects.
     * This is used for resuming or flushing units of work.
     */
    public void setChangeListener(Object clone, PropertyChangeListener listener, UnitOfWorkImpl uow) {
        // Nothing by default.
    }

    /**
     * INTERNAL:
     * Add a new value and its change set to the collection change record.  This is used by
     * attribute change tracking.
     */
    public void addToCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "addToCollectionChangeRecord");
    }

    /**
     * INTERNAL:
     * Remove a value and its change set from the collection change record.  This is used by
     * attribute change tracking.
     */
    public void removeFromCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "removeFromCollectionChangeRecord");
    }

    /**
     * INTERNAL:
     * Directly build a change record without comparison
     */
    public ChangeRecord buildChangeRecord(Object newValue, ObjectChangeSet owner, AbstractSession session) throws DescriptorException {
        throw DescriptorException.invalidMappingOperation(this, "buildChangeRecord");
    }
}
