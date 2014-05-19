// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.descriptors;

import java.io.Serializable;
import java.util.Vector;

import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.descriptors.CMPLifeCycleListener;
import oracle.toplink.internal.descriptors.ObjectBuilder;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.FalseUndefinedTrue;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.queryframework.UpdateObjectQuery;

/**
 * <p>
 * <b>Description</b>: Place holder for CMP specific information.  This class can be set on the ClassDescriptor.
 *
 * @see oracle.toplink.descriptors.PessimisticLockingPolicy
 *
 * @since TopLink 10.1.3
 */
public class CMPPolicy implements java.io.Serializable {
    // SPECJ: Temporary global optimization flag, use to disable uow merge.
    public static boolean OPTIMIZE_PESSIMISTIC_CMP = false;
    
    protected int forceUpdate;
    protected int updateAllFields;

    /** Allow the bean to always be locked as it enters a new transaction. */
    protected PessimisticLockingPolicy pessimisticLockingPolicy;

    /** Allows for the CMP life-cycle events to be intercepted from core by the CMP integration. */
    protected CMPLifeCycleListener lifeCycleListener;

    /** Class originally mapped, before anything was generated. */
    protected Class mappedClass;
    protected ClassDescriptor descriptor;

    /** The object deferral level.  This controls when objects changes will be sent to the Database. */
    protected int modificationDeferralLevel = ALL_MODIFICATIONS;

    /** defer no changes */
    public static final int NONE = 0;

    /** defer updates */
    public static final int UPDATE_MODIFICATIONS = 1;

    /** defer all modifications, inserts and deletes included (default) */
    public static final int ALL_MODIFICATIONS = 2;

    /** This setting will allow customers to control when Toplink will issue the insert SQL for CMP beans. */
    protected int nonDeferredCreateTime = UNDEFINED;

    /** undefined if it is non-deferred issue sql at create */
    public static final int UNDEFINED = 0;

    /** issue SQL after ejbCreate but before ejbPostCreate */
    public static final int AFTER_EJBCREATE = 1;

    /** issue SQL after ejbPostCreate */
    public static final int AFTER_EJBPOSTCREATE = 2;

    public CMPPolicy() {
        this.forceUpdate = FalseUndefinedTrue.Undefined;
        this.updateAllFields = FalseUndefinedTrue.Undefined;
    }

    /**
     * INTERNAL:
     * Notify that the insert operation has occurred, allow a sequence primary key to be reset.
     */
    public void postInsert(Object bean, AbstractSession session) {
        if (getLifeCycleListener() != null) {
            getLifeCycleListener().postInsert(bean, session);
        }
    }

    /**
     * INTERNAL:
     * Allow the ejbLoad life-cycle callback to be called.
     */
    public void invokeEJBLoad(Object bean, AbstractSession session) {
        if (getLifeCycleListener() != null) {
            getLifeCycleListener().invokeEJBLoad(bean, session);
        }
    }

    /**
     * INTERNAL:
     * Allow the ejbStore life-cycle callback to be called.
     */
    public void invokeEJBStore(Object bean, AbstractSession session) {
        if (getLifeCycleListener() != null) {
            getLifeCycleListener().invokeEJBStore(bean, session);
        }
    }

    /**
     * INTERNAL:
     * Return the CMP life-cycle listener, used to intercept events from core by CMP integration.
     */
    public CMPLifeCycleListener getLifeCycleListener() {
        return this.lifeCycleListener;
    }

    /**
     * INTERNAL:
     * Set the CMP life-cycle listener, used to intercept events from core by CMP integration.
     * This should be set by the CMP integration during deployment.
     */
    public void setLifeCycleListener(CMPLifeCycleListener lifeCycleListener) {
        this.lifeCycleListener = lifeCycleListener;
    }

    /**
     * ADVANCED:
     * This setting is only available for CMP beans that are not being deferred.
     * Using it will allow TopLink to  determine if the INSERT SQL should be sent to
     * the database before or after the postCreate call.
     */
    public int getNonDeferredCreateTime() {
        return this.nonDeferredCreateTime;
    }

    /**
     * PUBLIC:
     * Return the policy for bean pessimistic locking
     * @see #oracle.toplink.descriptors.PessimisticLockingPolicy
     */
    public PessimisticLockingPolicy getPessimisticLockingPolicy() {
        return pessimisticLockingPolicy;
    }

    /**
     * ADVANCED:
     * This can be set to control when changes to objects are submitted to the database
     * This is only applicable to TopLink's CMP implementation and not available within
     * the core.
     */
    public void setDeferModificationsUntilCommit(int deferralLevel) {
        this.modificationDeferralLevel = deferralLevel;
    }

    /**
     * PUBLIC:
     * Define the mapped class. This is the class which was originally mapped in the MW
     *
     * @param Class newMappedClass
     */
    public void setMappedClass(Class newMappedClass) {
        mappedClass = newMappedClass;
    }

    /**
     * PUBLIC:
     * Answer the mapped class. This is the class which was originally mapped in the MW
     *
     */
    public Class getMappedClass() {
        return mappedClass;
    }

    /**
     * ADVANCED:
     * This setting is only available for CMP beans that are not being deferred.
     * Using it will allow TopLink to  determine if the INSERT SQL should be sent to
     * the database before or after the postCreate call.
     */
    public void setNonDeferredCreateTime(int createTime) {
        this.nonDeferredCreateTime = createTime;
    }

    /**
     * PUBLIC:
     * Configure bean pessimistic locking
     *
     * @param PessimisticLockingPolicy policy
     * @see #oracle.toplink.descriptors.PessimisticLockingPolicy
     */
    public void setPessimisticLockingPolicy(PessimisticLockingPolicy policy) {
        pessimisticLockingPolicy = policy;
    }

    /**
     * PUBLIC:
     * Return true if bean pessimistic locking is configured
     */
    public boolean hasPessimisticLockingPolicy() {
        return pessimisticLockingPolicy != null;
    }

    /**
     * ADVANCED:
     * This can be used to control when changes to objects are submitted to the database
     * This is only applicable to TopLink's CMP implementation and not available within
     * the core.
     */
    public int getDeferModificationsUntilCommit() {
        return this.modificationDeferralLevel;
    }

    /**
     * ADVANCED:
     * Return true if descriptor is set to always update all registered objects of this type
     */
    public boolean getForceUpdate() {
        // default to false
        return (this.forceUpdate == FalseUndefinedTrue.True);
    }

    /**
     * ADVANCED:
     * Configure whether TopLink should always update all registered objects of
     * this type.  NOTE: if set to true, then updateAllFields must also be set
     * to true
     *
     * @param boolean shouldForceUpdate
     */
    public void setForceUpdate(boolean shouldForceUpdate) {
        if (shouldForceUpdate) {
            this.forceUpdate = FalseUndefinedTrue.True;
        } else {
            this.forceUpdate = FalseUndefinedTrue.False;
        }
    }

    /**
     * ADVANCED:
     * Return true if descriptor is set to update all fields for an object of this
     * type when an update occurs.
     */
    public boolean getUpdateAllFields() {
        // default to false
        return (this.updateAllFields == FalseUndefinedTrue.True);
    }

    /**
     * ADVANCED:
     * Configure whether TopLink should update all fields for an object of this
     * type when an update occurs.
     *
     * @param boolean shouldUpdatAllFields
     */
    public void setUpdateAllFields(boolean shouldUpdatAllFields) {
        if (shouldUpdatAllFields) {
            this.updateAllFields = FalseUndefinedTrue.True;
        } else {
            this.updateAllFields = FalseUndefinedTrue.False;
        }
    }

    /**
     * INTERNAL:
     * return internal tri-state value so we can decide whether to inherit or not at init time.
     */
    public int internalGetForceUpdate() {
        return this.forceUpdate;
    }

    /**
     * INTERNAL:
     * return internal tri-state value so we can decide whether to inherit or not at init time.
     */
    public int internalGetUpdateAllFields() {
        return this.updateAllFields;
    }

    /**
     * INTERNAL:
     * internal method to set the tri-state value. This is done in InheritancePolicy at init time.
     */
    public void internalSetForceUpdate(int newForceUpdateValue) {
        this.forceUpdate = newForceUpdateValue;
    }

    /**
     * INTERNAL:
     * internal method to set the tri-state value. This is done in InheritancePolicy at init time.
     */
    public void internalSetUpdateAllFields(int newUpdateAllFieldsValue) {
        this.updateAllFields = newUpdateAllFieldsValue;
    }

    /**
     * INTERNAL:
     * Initialize the CMPPolicy settings.
     */
    public void initialize(ClassDescriptor descriptor, AbstractSession session) throws DescriptorException {
        // updateAllFields is true so set custom query in DescriptorQueryManager
        // to force full SQL.  Don't overwrite a user defined query
        if (this.getUpdateAllFields() && !descriptor.getQueryManager().hasUpdateQuery()) {
            descriptor.getQueryManager().setUpdateQuery(new UpdateObjectQuery());
        }

        // make sure updateAllFields is set if forceUpdate is true
        if (this.getForceUpdate() && !this.getUpdateAllFields()) {
            throw DescriptorException.updateAllFieldsNotSet(descriptor);
        }
    }

    /**
     * INTERNAL:
     * @return Returns the owningDescriptor.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * @param owningDescriptor The owningDescriptor to set.
     */
    public void setDescriptor(ClassDescriptor owningDescriptor) {
        this.descriptor = owningDescriptor;
    }
    
    /**
     * INTERNAL:
     * Return if this policy is for CMP3.
     */
    public boolean isCMP3Policy() {
        return false;
    }
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this object to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
    }
    
    /**
     * INTERNAL:
     * Create an instance of the composite primary key class for the key object.
     */
    public Object createPrimaryKeyInstance(Vector key) {
        Object keyInstance = getPKClassInstance();
        ObjectBuilder builder = getDescriptor().getObjectBuilder();
        KeyElementAccessor[] pkElementArray = this.getKeyClassFields(getPKClass());
                
        for (int index = 0; index < pkElementArray.length; index++) {
            KeyElementAccessor accessor = pkElementArray[index];
            Object fieldValue = key.get(index);
            accessor.setValue(keyInstance, fieldValue);
        }
        
        return keyInstance;
    }
    

    /**
     * INTERNAL:
     * Create an instance of the composite primary key class for the key object.
     */
    public Object createPrimaryKeyInstance(Object key, AbstractSession session) {
        Object keyInstance = getPKClassInstance();
        ObjectBuilder builder = getDescriptor().getObjectBuilder();
        KeyElementAccessor[] pkElementArray = this.getKeyClassFields(getPKClass());
                
        for (int index = 0; index < pkElementArray.length; index++) {
            KeyElementAccessor accessor = pkElementArray[index];
            DatabaseMapping mapping = builder.getMappingForAttributeName(accessor.getAttributeName());
            // With session validation, the mapping shouldn't be null at this 
            // point, don't bother checking.
            
            while (mapping.isAggregateObjectMapping()) {
                mapping = mapping.getReferenceDescriptor().getObjectBuilder().getMappingForAttributeName(pkElementArray[index].getAttributeName());
            
                if (mapping == null) { // must be aggregate
                    mapping = builder.getMappingForField(accessor.getDatabaseField());
                }
            }
            
            Object fieldValue = mapping.getRealAttributeValueFromObject(key, session);
            accessor.setValue(keyInstance, fieldValue);
        }
        
        return keyInstance;
    }
    
    /**
     * INTERNAL:
     */
    public Object getPKClassInstance() {
        // TODO fix this exception so that it is more descriptive
        // This method only works in CMP3Policy but was added here for separation
        // of components
        throw new RuntimeException("Should not get here.");
    }
    
    /**
     * INTERNAL:
     */
    public Class getPKClass() {
        // TODO fix this exception so that it is more descriptive
        // This method only works in CMP3Policy but was added here for separation
        // of components
        throw new RuntimeException("Should not get here.");
    }

    /**
     * INTERNAL:
     * Use the key to create a TopLink primary key Vector.
     * If the key is simple (direct mapped) then just add it to a vector,
     * otherwise must go through the inefficient process of copying the key into the bean
     * and extracting the key from the bean.
     *
     * @param key Object the primary key to use for creating the vector
     * @return Vector
     */
    public Vector createPkVectorFromKey(Object key, AbstractSession session) {
        // TODO fix this exception so that it is more descriptive
        // This method only works in CMP3Policy but was added here for separation
        // of components
        throw new RuntimeException("Should not get here.");
    }

    /**
     * INTERNAL:
     * Use the key to create a bean and initialize its primary key fields.
     * Note: If is a compound PK then a primary key object is being used.
     * This method should only be used for 'templates' when executing
     * queries.  The bean built will not be given an EntityContext and should
     * not be used as an actual entity bean.
     *
     * @param key Object the primary key to use for initializing the bean's
     *            corresponding pk fields
     * @return TopLinkCmpEntity
     */
    public Object createBeanUsingKey(Object key, AbstractSession session) {
        // TODO fix this exception so that it is more descriptive
        // This method only works in CMP3Policy but was added here for separation
        // of components
        throw new RuntimeException("Should not get here.");
    }
    
    /**
     * INTERNAL:
     * @return Returns the keyClassFields.
     */
    protected KeyElementAccessor[] getKeyClassFields(Class clazz) {
        // TODO fix this exception so that it is more descriptive
        // This method only works in CMP3Policy but was added here for separation
        // of components
        throw new RuntimeException("Should not get here.");
    }
    
    /**
     * INTERNAL:
     * This is the interface used to encapsulate the the type of key class element
     */
    protected interface KeyElementAccessor {
        public String getAttributeName();
        public DatabaseField getDatabaseField();
        public Object getValue(Object object);
        public void setValue(Object object, Object value);
    }
    
    /**
     * INTERNAL:
     * This class will be used when the keyClass is a primitive
     */
    protected class KeyIsElementAccessor implements KeyElementAccessor, Serializable {
        protected String attributeName;
        protected DatabaseField databaseField;

        public KeyIsElementAccessor(String attributeName, DatabaseField databaseField) {
            this.attributeName = attributeName;
            this.databaseField = databaseField;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public DatabaseField getDatabaseField() {
            return this.databaseField;
        }
        
        public Object getValue(Object object) {
            return object;
        }
        
        public void setValue(Object object, Object value) {
            // WIP - do nothing for now??? 
        }
    }

}