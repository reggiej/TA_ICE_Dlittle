// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.objectrelational;


// This must be bond to Oracle JDBC currently, JDBC 2.0 should fix this.
import java.sql.*;
import java.util.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.mappings.*;
import oracle.toplink.queryframework.*;

/**
 * <p><b>Purpose:</b>
 * In an object-relational data model, structures reference each other through "Refs"; not through foreign keys as
 * in the relational data model. TopLink supports using the Ref to reference the target object.
 */
public class ReferenceMapping extends ObjectReferenceMapping {

    /** A ref is always stored in a single field. */
    protected DatabaseField field;

    /**
     * Returns all the aggregate fields.
     */
    protected Vector collectFields() {
        Vector fields = new Vector(1);
        fields.addElement(getField());
        return fields;
    }

    /**
     * INTERNAL:
     * Returns the field which this mapping represents.
     */
    public DatabaseField getField() {
        return field;
    }

    /**
     * PUBLIC:
     * Return the name of the field this mapping represents.
     */
    public String getFieldName() {
        return getField().getName();
    }

    /**
     * INTERNAL:
     * Join criteria is created to read target records (nested table) from the table.
     */
    public Expression getJoinCriteria(oracle.toplink.internal.expressions.QueryKeyExpression exp) {
        return null;
    }

    /**
     * INTERNAL:
     * The returns if the mapping has any constraint dependencies, such as foreign keys and join tables.
     */
    public boolean hasConstraintDependency() {
        return true;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        setReferenceDescriptor(session.getDescriptor(getReferenceClass()));

        if (referenceDescriptor == null) {
            throw DescriptorException.descriptorIsMissing(getReferenceClass().getName(), this);
        }

        // For bug 2730536 convert the field to be an ObjectRelationalDatabaseField.
        ObjectRelationalDatabaseField field = (ObjectRelationalDatabaseField)getField();
        field.setSqlType(java.sql.Types.REF);
        if (referenceDescriptor instanceof ObjectRelationalDescriptor) {
            field.setSqlTypeName(((ObjectRelationalDescriptor)referenceDescriptor).getStructureName());
        }

        setField(getDescriptor().buildField(getField()));
        setFields(collectFields());

        // May require native connection in WLS to avoid wrapping wrapped.
        getDescriptor().setIsNativeConnectionRequired(true);
    }

    /**
     * INTERNAL:
     */
    public boolean isReferenceMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Insert privately owned parts
     */
    public void preInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Checks if privately owned parts should be inserted or not.
        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // Get the privately owned parts
        Object object = getRealAttributeValueFromObject(query.getObject(), query.getSession());

        if (object == null) {
            return;
        }

        if (isPrivateOwned()) {
            // No need to set changeSet as insert is a straight copy anyway
            InsertObjectQuery insertQuery = new InsertObjectQuery();
            insertQuery.setIsExecutionClone(true);
            insertQuery.setObject(object);
            insertQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(insertQuery);
        } else {
            ObjectChangeSet changeSet = null;
            UnitOfWorkChangeSet uowChangeSet = null;
            if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object);
            }
            WriteObjectQuery writeQuery = new WriteObjectQuery();
            writeQuery.setIsExecutionClone(true);
            writeQuery.setObject(object);
            writeQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(writeQuery);
        }
    }

    /**
     * INTERNAL:
     * Update privately owned parts
     */
    public void preUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        if (isPrivateOwned()) {
            Object objectInDatabase = readPrivateOwnedForObject(query);
            if (objectInDatabase != null) {
                query.setProperty(this, objectInDatabase);
            }
        }

        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // Get the privately owned parts in the memory
        Object object = getRealAttributeValueFromObject(query.getObject(), query.getSession());
        if (object != null) {
            ObjectChangeSet changeSet = null;
            UnitOfWorkChangeSet uowChangeSet = null;
            if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object);
            }
            WriteObjectQuery writeQuery = new WriteObjectQuery();
            writeQuery.setIsExecutionClone(true);
            writeQuery.setObject(object);
            writeQuery.setObjectChangeSet(changeSet);
            writeQuery.setCascadePolicy(query.getCascadePolicy());
            query.getSession().executeQuery(writeQuery);
        }
    }

    /**
     * INTERNAL:
     * Insert privately owned parts
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        return;
    }

    /**
     * INTERNAL:
     * Delete privately owned parts
     */
    public void postDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        return;
    }

    /**
     * INTERNAL:
     * Update privately owned parts
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        return;
    }

    /**
     * INTERNAL:
     * Delete privately owned parts
     */
    public void preDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        return;
    }

    /**
     * Set the field in the mapping.
     */
    protected void setField(DatabaseField field) {
        this.field = field;
    }

    /**
     * PUBLIC:
     * Set the field name in the mapping.
     */
    public void setFieldName(String fieldName) {
        setField(new ObjectRelationalDatabaseField(fieldName));
    }

    /**
     * PUBLIC:
     * This is a reference class whose instances this mapping will store in the domain objects.
     */
    public void setReferenceClass(Class referenceClass) {
        this.referenceClass = referenceClass;
    }

    /**
     * INTERNAL:
     * Return the value of the field from the row or a value holder on the query to obtain the object.
     * Check for batch + aggregation reading.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) throws DatabaseException {
        Ref ref = (Ref)row.get(getField());

        if (ref == null) {
            return null;
        }

        Struct struct;
        try {
            ((DatabaseAccessor)executionSession.getAccessor()).incrementCallCount(executionSession); 
            java.sql.Connection connection = ((DatabaseAccessor)executionSession.getAccessor()).getConnection();
            struct = (Struct)executionSession.getPlatform().getRefValue(ref,executionSession,connection);
        } catch (java.sql.SQLException exception) {
            throw DatabaseException.sqlException(exception, executionSession);
        }
        AbstractRecord targetRow = ((ObjectRelationalDescriptor)getReferenceDescriptor()).buildRowFromStructure(struct);
        ((DatabaseAccessor)executionSession.getAccessor()).decrementCallCount();

        return getReferenceDescriptor().getObjectBuilder().buildObject(query, targetRow, joinManager);
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord record, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        Object referenceObject = getRealAttributeValueFromObject(object, session);

        if (referenceObject == null) {
            // Fix for 2730536, must put something in modify row, even if it is null.
            record.put(getField(), null);
            return;
        }

        Ref ref = ((ObjectRelationalDescriptor)getReferenceDescriptor()).getRef(referenceObject, session);

        record.put(getField(), ref);
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord record, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        ObjectChangeSet changeSet = (ObjectChangeSet)((ObjectReferenceChangeRecord)changeRecord).getNewValue();
        Object referenceObject = changeSet.getUnitOfWorkClone();

        if (referenceObject == null) {
            return;
        }

        Ref ref = ((ObjectRelationalDescriptor)getReferenceDescriptor()).getRef(referenceObject, session);

        record.put(getField(), ref);
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     * The foreign keys must be set to null to avoid constraints.
     */
    public void writeFromObjectIntoRowForShallowInsert(Object object, AbstractRecord record, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        record.put(getField(), null);
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     * The foreign keys must be set to null to avoid constraints.
     */
    public void writeFromObjectIntoRowForShallowInsertWithChangeRecord(ChangeRecord changeRecord, AbstractRecord record, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        record.put(getField(), null);
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord record, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        record.put(getField(), null);
    }
}
