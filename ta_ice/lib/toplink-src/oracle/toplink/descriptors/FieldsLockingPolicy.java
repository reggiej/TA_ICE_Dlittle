// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import java.util.*;
import oracle.toplink.internal.descriptors.OptimisticLockingPolicy;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.ObjectChangeSet;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.helper.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.expressions.*;
import oracle.toplink.exceptions.*;

/**
 * <p><b>Purpose</b>: An abstract superclass of some implementations of the OptimisticLockingPolicy
 * interface.  All of the subclasses of this class implement OptimisticLocking
 * based on mapped fields in the object.  These fields are only compared and not modified.
 * Any modification (incrementing etc..) must be handled by the application.
 *
 * @see AllFieldsLockingPolicy
 * @see ChangedFieldsLockingPolicy
 * @see SelectedFieldsLockingPolicy
 * @since TopLink 2.1
 * @author Peter Krogh
 */
public abstract class FieldsLockingPolicy implements OptimisticLockingPolicy {
    protected ClassDescriptor descriptor;
    protected Vector allNonPrimaryKeyFields;

    /**
     * PUBLIC:
     * Create a new field locking policy.
     * A field locking policy is based on locking on a subset of fields by comparing with their previous values to detect field-level collisions.
     * Note: the unit of work must be used for all updates when using field locking.
     */
    public FieldsLockingPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Add update fields for template row.
     * These are any unmapped fields required to write in an update.
     * Since all fields are mapped, there is nothing required.
     */
    public void addLockFieldsToUpdateRow(AbstractRecord Record, AbstractSession session) {
        // Nothing required.
    }

    /**
     * INTERNAL:
     * Values to be included in the locking mechanism are added
     * to the translation row.  Set the translation row to all the original field values.
     */
    public abstract void addLockValuesToTranslationRow(ObjectLevelModifyQuery query);

    /**
     * INTERNAL:
     * Returns the fields that should be compared in the where clause.
     * In this case, it is all the fields, except for the primary key
     * and class indicator fields.
     * This is called durring lazy initialization
     */
    protected Vector buildAllNonPrimaryKeyFields() {
        Vector fields = new Vector();
        for (Enumeration enumtr = descriptor.getFields().elements(); enumtr.hasMoreElements();) {
            DatabaseField dbField = (DatabaseField)enumtr.nextElement();
            if (!isPrimaryKey(dbField)) {
                if (descriptor.hasInheritance()) {
                    DatabaseField classField = descriptor.getInheritancePolicy().getClassIndicatorField();
                    if (!((classField == null) || dbField.equals(classField))) {
                        fields.addElement(dbField);
                    }
                } else {
                    fields.addElement(dbField);
                }
            }
        }

        /* CR#... nullpoint occurs if null is returned, not sure why this was here.
        if (fields.isEmpty()) {
            return null;
        }*/
        return fields;
    }

    /**
     * INTERNAL:
     * When given an expression, this method will return a new expression with the optimistic
     * locking values included.  The values are taken from the passed in database row.
     * This expression will be used in a delete call.
     */
    public Expression buildDeleteExpression(DatabaseTable table, Expression mainExpression, AbstractRecord row) {
        return mainExpression.and(buildExpression(table, row, null, mainExpression.getBuilder()));
    }

    /**
     * INTERNAL:
     * returns the expression to be used in both the delete and update where clause.
     */
    protected Expression buildExpression(DatabaseTable table, AbstractRecord transRow, AbstractRecord modifyRow, ExpressionBuilder builder) {
        Expression exp = null;
        DatabaseField field;
        Enumeration enumtr = getFieldsToCompare(table, transRow, modifyRow).elements();
        if (enumtr.hasMoreElements()) {
            field = (DatabaseField)enumtr.nextElement();//First element
            exp = builder.getField(field).equal(builder.getParameter(field));
        }
        while (enumtr.hasMoreElements()) {
            field = (DatabaseField)enumtr.nextElement();
            exp = exp.and(builder.getField(field).equal(builder.getParameter(field)));
        }
        return exp;
    }

    /**
     * INTERNAL:
     * This method must be included in any locking policy.  When given an
     * expression, this method will return a new expression with the optimistic
     * locking values included.  The values are taken from the passed in database row.
     * This expression will be used in a delete call.
     */
    public Expression buildUpdateExpression(DatabaseTable table, Expression mainExpression, AbstractRecord transRow, AbstractRecord modifyRow) {
        return mainExpression.and(buildExpression(table, transRow, modifyRow, mainExpression.getBuilder()));
    }

    /**
     * INTERNAL:
     * Clone the policy
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Indicates whether compareWriteLockValues method is supported by the policy.
     * Numeric or timestamp lock values could be compared:
     * for every pair of values v1 and v2 - either v1<v2; or v1==v2; or v1>v2.
     * However it's impossible to compare values for FieldsLockingPolicy for two reasons:
     * 1. there is no "linear order": v1<v2 and v>v2 is not defined: either v1==v2 or v1!=v2;
     * 2. locking value is not a single field which is not part of mapped object value
     *    but rather a set of object's mapped fields. That means any object's mapped attribute change
     *    is potentially a change of the locking value.
     *    For ChangedFieldsLockingPolicy every mapped attribute's change is a change of locking value.
     *    The pattern used by versioning: "if the original locking value is unchanged
     *    then the object hasn't been changed outside of the application", which allows
     *    to distinguish between the change made inside and outside the application,
     *    doesn't work for fields locking.
     *    It degenerates into useless pattern: "if the original locking value is unchanged
     *    then the object hasn't been changed".
     *    
     * Use compareWriteLockValues method only if this method returns true.
     */
    public boolean supportsWriteLockValuesComparison() {
        return false;
    }

    /**
     * INTERNAL:
     * This method shouldn't be called if supportsWriteLockValuesComparison() returns false.
     * This method compares two writeLockValues.
     * The writeLockValues should be non-null and of the correct type.
     * Returns:
     * -1 if value1 is less (older) than value2;
     *  0 if value1 equals value2;
     *  1 if value1 is greater (newer) than value2.
     * Throws:
     *  NullPointerException if the passed value is null;
     *  ClassCastException if the passed value is of a wrong type.
     */
    public int compareWriteLockValues(Object value1, Object value2){
        // should never be called because supportsWriteLockValuesComparison() returns false.
    	return -1;    	
    }

    /**
     * INTERNAL:
     * Returns the fields that should be compared in the where clause.
     * In this case, it is all the fields, except for the primary key
     * and class indicator field.
     */
    protected Vector getAllNonPrimaryKeyFields() {
        if (allNonPrimaryKeyFields == null) {
            allNonPrimaryKeyFields = buildAllNonPrimaryKeyFields();
        }
        return allNonPrimaryKeyFields;
    }

    /**
     * INTERNAL:
     * filter the fields based on the passed in table.  Only return fields of this table.
     */
    protected Vector getAllNonPrimaryKeyFields(DatabaseTable table) {
        Vector filteredFields = new Vector();
        for (Enumeration enumtr = getAllNonPrimaryKeyFields().elements(); enumtr.hasMoreElements();) {
            DatabaseField dbField = (DatabaseField)enumtr.nextElement();
            if (dbField.getTableName().equals(table.getName())) {
                filteredFields.addElement(dbField);
            }
        }
        return filteredFields;
    }

    /**
     * INTERNAL:
     * This is the base value that is older than all other values, it is used in the place of
     * null in some situations.
     */
    public Object getBaseValue(){
        return null; // this locking type does not store values in the cache
    }
    
    /**
     * INTERNAL:
     * Returns the fields that should be compared in the where clause.
     * This method must be implemented by the subclass
     */
    protected abstract Vector getFieldsToCompare(DatabaseTable table, AbstractRecord transRow, AbstractRecord modifyRow);

    /**
       * INTERNAL:
       * Return the write lock field.
       */
    public DatabaseField getWriteLockField() {
        // Does not apply to any field locking policy, so return null
        return null;
    }

    /**
     * INTERNAL:
     */
    public Expression getWriteLockUpdateExpression(ExpressionBuilder builder) {
        // Does not apply to any field locking policy, so return null
        return null;
    }

    /**
     * INTERNAL:
     * Return the value that should be stored in the identity map.  If the value
     * is stored in the object, then return a null.
     */
    public Object getValueToPutInCache(AbstractRecord row, AbstractSession session) {
        return null;
    }

    /**
     * INTERNAL:
     * Return the number of version difference between the two states of the object.
     */
    public int getVersionDifference(Object currentValue, Object domainObject, Vector primaryKeys, AbstractSession session) {
        //There is no way of knowing what the difference is so return -1
        // This will merge the object.
        return -1;
    }

    /**
     * INTERNAL:
     * This method will return the optimistic lock value for the object
     */
    public Object getWriteLockValue(Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        //There is no way of knowing if this value is newer or not, so always return true.
        return null;
    }

    /**
     * INTERNAL:
     * It is responsible for initializing the policy;
     */
    public void initialize(AbstractSession session) {
        //by default everything is lazy initialized
    }

    /**
     * INTERNAL:
     * It is responsible for initializing the policy;
     */
    public void initializeProperties() {
        //nothing to do
    }
    
     /**
      * PUBLIC:
      * Return true if the lock value is stored in the cache.
      */
     public boolean isStoredInCache() {
         return false;
     }
         
    /**
     * PUBLIC:
     * Return true if the policy uses cascade locking. Currently, not supported
     * on this policy at this time.
     */
     public boolean isCascaded() {
         return false;
     }
     
    /**
     * INTERNAL:
     */
    public boolean isChildWriteLockValueGreater(AbstractSession session, java.util.Vector primaryKey, Class original, ObjectChangeSet changeSet) {
        return false;
    }

    /**
     * INTERNAL:
     * Compares the value  and the value from the object
     * (or cache).  Will return true if the object is newer
     * than the row.
     */
    public boolean isNewerVersion(Object currentValue, Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        //There is no way of knowing if this value is newer or not, so always return true.
        return true;
    }

    /**
     * INTERNAL:
     * Compares the value from the row and from the object
     * (or cache).  Will return true if the object is newer
     * than the row.
     */
    public boolean isNewerVersion(AbstractRecord Record, Object domainObject, java.util.Vector primaryKey, AbstractSession session) {
        //There is no way of knowing if this value is newer or not, so always return true.
        return true;
    }

    /**
     * INTERNAL:
     * Returns whether or not this field is a primary key.
     * This method will also return true for secondary table primarykeys
     */
    protected boolean isPrimaryKey(DatabaseField dbField) {
        if (descriptor.getPrimaryKeyFields().contains(dbField)) {
            return true;
        } else {
            if (descriptor.isMultipleTableDescriptor()) {
                for (Iterator enumtr = descriptor.getAdditionalTablePrimaryKeyFields().values().iterator();
                         enumtr.hasNext();) {
                    if (((Map)enumtr.next()).containsKey(dbField)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * Only applicable when the value is stored in the cache.
     */
    public void mergeIntoParentCache(UnitOfWorkImpl uow, java.util.Vector primaryKey, Object object) {
        // nothing to do
    }

    /**
     * INTERNAL: Set method for all the primary keys
     */
    protected void setAllNonPrimaryKeyFields(Vector allNonPrimaryKeyFields) {
        this.allNonPrimaryKeyFields = allNonPrimaryKeyFields;
    }

    /**
     * INTERNAL: Set method for the descriptor
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * INTERNAL:
     * Put the initial writelock value into the modifyRow.
     * There is nothing to do because all the lock values are in the mappings.
     */
    public void setupWriteFieldsForInsert(ObjectLevelModifyQuery query) {
        //nothing to do.
    }

    /**
     * INTERNAL:
     * Nothing to do because all updates are handled by the application
     */
    public void updateRowAndObjectForUpdate(ObjectLevelModifyQuery query, Object domainObject) {
        //nothing to do
    }

    /**
     * INTERNAL:
     * Check the row count for lock failure.
     */
    public void validateDelete(int rowCount, Object object, DeleteObjectQuery query) {
        if (rowCount <= 0) {
            // Mark the object as invalid in the session cache.
            query.getSession().getParentIdentityMapSession(query, true, true).getIdentityMapAccessor().invalidateObject(object);
            throw OptimisticLockException.objectChangedSinceLastReadWhenDeleting(object, query);
        }
    }

    /**
     * INTERNAL:
     * Check the row count for lock failure.
     */
    public void validateUpdate(int rowCount, Object object, WriteObjectQuery query) {
        if (rowCount <= 0) {
            // Mark the object as invalid in the session cache.
            query.getSession().getParentIdentityMapSession(query, true, true).getIdentityMapAccessor().invalidateObject(object);
            throw OptimisticLockException.objectChangedSinceLastReadWhenUpdating(object, query);
        }
    }

    /**
     * INTERNAL:
     * throw an exception if not inside a unit of work at this point
     */
    protected void verifyUsage(AbstractSession session) {
        if (!session.isUnitOfWork()) {
            throw ValidationException.fieldLevelLockingNotSupportedWithoutUnitOfWork();
        }
    }
}