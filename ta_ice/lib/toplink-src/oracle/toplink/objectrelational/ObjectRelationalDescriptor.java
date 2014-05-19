// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.objectrelational;

import java.util.*;

import java.sql.*;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.queryframework.*;
import oracle.toplink.descriptors.RelationalDescriptor;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.sessions.DatabaseRecord;

/**
 * <p><b>Purpose:</b>
 * Differenciates object-relational descriptors from normal relational descriptors.
 * The object-relational descriptor describes a type not a table, (although there
 * is normally a table associated with the type, unless it is aggregate).
 */
public class ObjectRelationalDescriptor extends RelationalDescriptor {
    protected String structureName;
    protected Vector orderedFields;

    public ObjectRelationalDescriptor() {
        this.orderedFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
    }
    
    /** INTERNAL:
     * Auto-Default orderedFields to fields
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        if (orderedFields==null || orderedFields.size()==0){
           orderedFields=getAllFields();
        }
    }


    /**
     * PUBLIC:
     * Order the fields in a specific
     * Add the field ordering, this will order the fields in the order this method is called.
     * @param fieldName the name of the field to add ordering on.
     */
    public void addFieldOrdering(String fieldName) {
        getOrderedFields().addElement(new DatabaseField(fieldName));
    }

    /**
     * INTERNAL:
     * Extract the direct values from the specified field value.
     * Return them in a vector.
     * The field value better be an Array.
     */
    public Vector buildDirectValuesFromFieldValue(Object fieldValue) throws DatabaseException {
        
        if(fieldValue == null) {
            return null;
        }

        return Helper.vectorFromArray((Object[])fieldValue);
    }

    /**
     * INTERNAL:
     * Build the appropriate field value for the specified
     * set of direct values.
     * The database better be expecting an ARRAY.
     */
    public Object buildFieldValueFromDirectValues(Vector directValues, String elementDataTypeName, AbstractSession session) throws DatabaseException {
        Object[] fields = Helper.arrayFromVector(directValues);
        try {
            ((DatabaseAccessor)session.getAccessor()).incrementCallCount(session);
            java.sql.Connection connection = ((DatabaseAccessor)session.getAccessor()).getConnection();
            return session.getPlatform().createArray(elementDataTypeName, fields, session,connection);
        } catch (java.sql.SQLException ex) {
            throw DatabaseException.sqlException(ex, session);
        } finally {
            ((DatabaseAccessor)session.getAccessor()).decrementCallCount();
        }
    }
    
    /**
     * INTERNAL:
     * Build and return the field value from the specified nested database row.
     * The database better be expecting a Struct.
     */
    public Object buildFieldValueFromNestedRow(AbstractRecord nestedRow, AbstractSession session) throws DatabaseException {
        java.sql.Connection connection = ((DatabaseAccessor)session.getAccessor()).getConnection();
        return this.buildStructureFromRow(nestedRow, session, connection);
    }

    /**
     * INTERNAL:
     * Build and return the appropriate field value for the specified
     * set of nested rows.
     * The database better be expecting an ARRAY.
     * It looks like we can ignore inheritance here....
     */
    public Object buildFieldValueFromNestedRows(Vector nestedRows, String structureName, AbstractSession session) throws DatabaseException {
        Object[] fields = new Object[nestedRows.size()];
        java.sql.Connection connection = ((DatabaseAccessor)session.getAccessor()).getConnection();
        boolean reconnected = false;
        
        try {
            if (connection == null) {
                ((DatabaseAccessor)session.getAccessor()).incrementCallCount(session);
                reconnected = true;
                connection = ((DatabaseAccessor)session.getAccessor()).getConnection();
            }
        
            int i = 0;
            for (Enumeration stream = nestedRows.elements(); stream.hasMoreElements();) {
                AbstractRecord nestedRow = (AbstractRecord)stream.nextElement();
                fields[i++] = this.buildStructureFromRow(nestedRow, session, connection);
            }

            return session.getPlatform().createArray(structureName, fields, session,connection);
        } catch (java.sql.SQLException exception) {
            throw DatabaseException.sqlException(exception, session);
        } finally {
            if (reconnected) {
                ((DatabaseAccessor)session.getAccessor()).decrementCallCount();
            }
        }
    }
     /**
      * INTERNAL:
      * Build and return the nested rows from the specified field value.
      * This method allows the field value to  be an ARRAY containing other structures 
      * such as arrays or Struct, or direct values.
      */
     static public Object buildContainerFromArray(Array fieldValue, ObjectRelationalDatabaseField arrayField, AbstractSession session) throws DatabaseException {        
        if (arrayField.getType()==null){
            return fieldValue;
        }
        Object[] objects = null;
        try {
            objects = (Object[])fieldValue.getArray();
        } catch (java.sql.SQLException ex) {
            throw DatabaseException.sqlException(ex, session);
        }
        if (objects == null) {
            return null;
        }

        boolean isNestedStructure = false;
        ObjectRelationalDescriptor ord=null;
        DatabaseField nestedType = null;
        if (arrayField != null){
            nestedType = arrayField.getNestedTypeField();
            if ((nestedType != null) && nestedType.getSqlType()==Types.STRUCT){
                ClassDescriptor descriptor = session.getDescriptor(nestedType.getType());
                if ((descriptor != null) && (descriptor.isObjectRelationalDescriptor())) {
                    //this is used to convert non-null objects passed through stored procedures and custom SQL to structs 
                    ord=(ObjectRelationalDescriptor)descriptor;
                }
            } else if ((nestedType != null) && (nestedType instanceof ObjectRelationalDatabaseField) ){
                isNestedStructure = true;
            }
        }
        //handle ARRAY conversions
        ReadObjectQuery query = new ReadObjectQuery();
        query.setSession(session);
        ContainerPolicy cp = ContainerPolicy.buildPolicyFor(arrayField.getType());
        Object container = cp.containerInstance(objects.length);
        for (int i = 0; i < objects.length; i++) {
            Object arrayValue = objects[i];
            if (arrayValue == null) {
                return null;
            }
            if (ord!=null){
                AbstractRecord nestedRow = ord.buildRowFromStructure( (Struct)arrayValue);
                ClassDescriptor descriptor = ord;
                if (descriptor.hasInheritance()) {
                    Class newElementClass = descriptor.getInheritancePolicy().classFromRow(nestedRow, session);
                    if (!descriptor.getJavaClass().equals(newElementClass)) {
                        descriptor = session.getDescriptor(newElementClass);
                        if (descriptor==null){
                            descriptor=ord;
                        }
                    }
                }
                arrayValue = descriptor.getObjectBuilder().buildObject(query, nestedRow);
            }else if (isNestedStructure && (arrayValue instanceof Array)){
                arrayValue = buildContainerFromArray((Array)arrayValue, (ObjectRelationalDatabaseField)nestedType, session);
            }             
             
            cp.addInto(arrayValue, container, session);
        }
        return container;
     }

    /**
     * INTERNAL:
     * Build and return the nested database row from the specified field value.
     * The field value better be an Struct.
     */
    public AbstractRecord buildNestedRowFromFieldValue(Object fieldValue) throws DatabaseException {
        
        AbstractRecord row = new DatabaseRecord();
        Object[] attributes = (Object[])fieldValue;

        for (int index = 0; index < getOrderedFields().size(); index++) {
            DatabaseField field = (DatabaseField)getOrderedFields().elementAt(index);
            row.put(field, attributes[index]);
        }

        return row;
    }

    /**
     * INTERNAL:
     * Build and return the nested rows from the specified field value.
     * The field value better be an ARRAY.
     */
    public Vector buildNestedRowsFromFieldValue(Object fieldValue, AbstractSession session) throws DatabaseException {
        
        if(fieldValue==null){
            return null;
        }
        
        Object[] structs = (Object[])fieldValue;

        Vector nestedRows = new Vector(structs.length);
        for (int i = 0; i < structs.length; i++) {
            Object[] struct = (Object[])structs[i];
            if (struct == null) {
                return null;
            }
            nestedRows.addElement(this.buildNestedRowFromFieldValue(struct));
        }
        return nestedRows;
    }

    /**
     * INTERNAL:
     * Build a row representation from the ADT strcuture field array.
     * TopLink will then build the object from the row.
     */
    public AbstractRecord buildRowFromStructure(Struct structure) throws DatabaseException {
    	AbstractRecord row = new DatabaseRecord();
        Object[] attributes;
        try {
            attributes = structure.getAttributes();
        } catch (java.sql.SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
        
        if(attributes!=null){
            for(int i=0;i<attributes.length;i++){
                if(attributes[i] instanceof Array ){
                    attributes[i]=ObjectRelationalDescriptor.buildArrayObjectFromArray(attributes[i]);
                }else if(attributes[i] instanceof Struct){
                    attributes[i]=ObjectRelationalDescriptor.buildArrayObjectFromStruct(attributes[i]);
                }
            }
        }

        return buildNestedRowFromFieldValue(attributes);
    }

    /**
     * INTERNAL:
     * Build a ADT structure from the row data.
     */
    public Struct buildStructureFromRow(AbstractRecord row, AbstractSession session, java.sql.Connection connection) throws DatabaseException {
        Struct structure;
        boolean reconnected = false;
        
        try {
            if (connection == null) {
                ((DatabaseAccessor)session.getAccessor()).incrementCallCount(session);
                reconnected = true;
                connection = ((DatabaseAccessor)session.getAccessor()).getConnection();
            }
        
            Object[] fields = new Object[getOrderedFields().size()];
            for (int index = 0; index < getOrderedFields().size(); index++) {
                DatabaseField field = (DatabaseField)getOrderedFields().elementAt(index);
                fields[index] = row.get(field);
            }
                        
            structure = session.getPlatform().createStruct(getStructureName(), fields, session, connection);
        } catch (java.sql.SQLException exception) {
            throw DatabaseException.sqlException(exception, session);
        } finally {
            if (reconnected) {
                ((DatabaseAccessor)session.getAccessor()).decrementCallCount();    
            }
        }

        return structure;
    }
    
    /**
     * INTERNAL:
     * Build array of objects for Array data type.
     */
    
    public static Object buildArrayObjectFromArray(Object array) throws DatabaseException {
        Object[] objects = null;
        if(array==null){
            return array;
        }
        try {
            objects = (Object[])((Array)array).getArray();
        } catch (java.sql.SQLException ex) {
            throw DatabaseException.sqlException(ex);
        }
        if (objects == null ) {
            return null;
        } else {
            for (int i=0;i<objects.length;i++){
                if (objects[i] instanceof Array){
                    objects[i] = buildArrayObjectFromArray((Array)objects[i]);
                }
                if (objects[i] instanceof Struct){
                    objects[i] = buildArrayObjectFromStruct((Struct)objects[i]);
                }
            }
        }
        return objects;
    }
    
    /**
     * INTERNAL:
     * Build array of objects for Struct data type.
     */
    
    public static Object buildArrayObjectFromStruct(Object structure) throws DatabaseException{
        Object[] attributes = null;
        if(structure==null){
            return structure;
        }
        try {
            attributes = ((Struct)structure).getAttributes();
        } catch (java.sql.SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
        if (attributes==null){
            return null;
        } else {
            for(int i=0;i<attributes.length;i++){
                if (attributes[i] instanceof Array){
                    attributes[i] = buildArrayObjectFromArray((Array)attributes[i]);
                }
                if (attributes[i] instanceof Struct){
                    attributes[i] = buildArrayObjectFromStruct((Struct)attributes[i]);
                }
            }
        }
        return attributes;
    }

    /**
     * INTERNAL:
     * Aggregates use a dummy table as default.
     */
    protected DatabaseTable extractDefaultTable() {
        if (isAggregateDescriptor()) {
            return new DatabaseTable();
        }

        return super.extractDefaultTable();
    }

    /**
     * INTERNAL:
     * Return the field order.
     */
    public Vector getOrderedFields() {
        return orderedFields;
    }

    /**
     * INTERNAL:
     * Get the ref for the object.
     * This is required for use by Refs, there might be a better way to do it when objID are supported.
     * (i.e. getting it from the object or identity map).
     */
    public Ref getRef(Object object, AbstractSession session) {
        SQLSelectStatement statement = new SQLSelectStatement();
        statement.addTable((DatabaseTable)getTables().firstElement());// Assumed only one for obj-rel descriptors.
        statement.getFields().addElement(new oracle.toplink.expressions.ExpressionBuilder().ref());
        statement.setWhereClause(getObjectBuilder().buildPrimaryKeyExpressionFromObject(object, session));
        statement.setRequiresAliases(true);
        statement.normalize(session, this);

        ValueReadQuery valueQuery = new ValueReadQuery();
        valueQuery.setSQLStatement(statement);

        valueQuery.prepareCall(session, new DatabaseRecord());
        // Must return unwrapped Ref on WLS.
        valueQuery.getCall().setIsNativeConnectionRequired(true);

        Ref ref = (Ref)session.executeQuery(valueQuery);

        return ref;
    }

    /**
     * PUBLIC:
     * Return the name of the structure.
     * This is the name of the user defined data type as defined on the database.
     */
    public String getStructureName() {
        return structureName;
    }
    
    /**
     *  PUBLIC:
     *  Return if this is an ObjectRelationalDescriptor.
     */
    public boolean isObjectRelationalDescriptor(){
        return true;
    }

    /**
     * INTERNAL:
     * Aggregates obj-rel are initialized normally as no cloning is required.
     */
    public boolean requiresInitialization() {
        return true;
    }

    protected void validateMappingType(DatabaseMapping mapping) {
        //do nothing
    }

    /**
     * INTERNAL:
     * Set the field order.
     */
    public void setOrderedFields(Vector orderedFields) {
        this.orderedFields = orderedFields;
    }

    /**
     * PUBLIC:
     * Set the name of the structure.
     * This is the name of the user defined data type as defined on the database.
     */
    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }
}
