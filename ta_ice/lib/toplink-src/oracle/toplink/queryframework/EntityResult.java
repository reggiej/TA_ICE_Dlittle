// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oracle.toplink.exceptions.QueryException;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.mappings.OneToOneMapping;
import oracle.toplink.sessions.DatabaseRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to represent the EntityResult structure as defined by
 * the EJB 3.0 Persistence specification.  This class is a subcompent of the 
 * SQLResultSetMapping
 * 
 * @see SQLResultSetMapping
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class EntityResult extends SQLResult {
    /** Stores the class name of result  */
    protected String entityClassName;
    protected Class entityClass;
    
    /** Stores the list of FieldResult */
    protected Map fieldResults;
    
    /** Stores the column that will contain the value to determine the correct subclass
     * to create if applicable.
     */
    protected String discriminatorColumn;
    
    public EntityResult(Class entityClass){
        this.entityClass = entityClass;
        if (this.entityClass == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_for_entity_result"));
        }
    }
    
    public EntityResult(String entityClassName){
        this.entityClassName = entityClassName;
        if (this.entityClassName == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_for_entity_result"));
        }
    }
    
    public void addFieldResult(FieldResult fieldResult){
        if (fieldResult == null || fieldResult.getAttributeName() == null){
            return;
        }
        FieldResult existingFieldResult = (FieldResult)getFieldResults().get(fieldResult.getAttributeName());
        if (existingFieldResult==null){
            getFieldResults().put(fieldResult.getAttributeName(), fieldResult);
        }else{
            existingFieldResult.add(fieldResult);
        }
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
        Class entityClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    entityClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(entityClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(entityClassName, exception.getException());
                }
            } else {
                entityClass = oracle.toplink.internal.security.PrivilegedAccessHelper.getClassForName(entityClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(entityClassName, exc);
        }
        this.entityClass = entityClass;
    };   

    /**
     * Accessor for the internally stored list of FieldResult.  Calling this
     * method will result in a collection being created to store the FieldResult
     */
    public Map getFieldResults(){
        if (this.fieldResults == null){
            this.fieldResults = new HashMap();
        }
        return this.fieldResults;
    }
    
    /**
     * Returns the column name for the column that will store the value used to
     * determine the subclass type if applicable.
     */
    public String getDiscriminatorColumn(){
        return this.discriminatorColumn;
    }

    /**
     * Sets the column name for the column that will store the value used to
     * determine the subclass type if applicable.
     */
    public void setDiscriminatorColumn(String column){
        if (column == null){
            return;
        }
        this.discriminatorColumn = column;
    }

    /**
     * INTERNAL:
     * This method is a convience method for extracting values from Results
     */
    public Object getValueFromRecord(DatabaseRecord record, ResultSetMappingQuery query){
        // From the row data build result entity.
        // To do this let's collect the column based data for this entity from
        // the results and call build object with this new row.
        ClassDescriptor descriptor = query.getSession().getDescriptor(this.entityClass);
        DatabaseRecord entityRecord = new DatabaseRecord(descriptor.getFields().size());
        if (descriptor.hasInheritance()) {
            if (this.discriminatorColumn != null) {
                Object value = record.get(this.discriminatorColumn);
                if (value == null){
                    throw QueryException.discriminatorColumnNotSelected(this.discriminatorColumn, query.getSQLResultSetMapping().getName());
                }
                entityRecord.put(descriptor.getInheritancePolicy().getClassIndicatorField(), record.get(this.discriminatorColumn));
            } else {
                entityRecord.put(descriptor.getInheritancePolicy().getClassIndicatorField(), record.get(descriptor.getInheritancePolicy().getClassIndicatorField()));
            }
            // if the descriptor uses inheritance and multiple types may have been read
            //get the correct descriptor.
            if (descriptor.hasInheritance() && descriptor.getInheritancePolicy().shouldReadSubclasses()) {
                Class classValue = descriptor.getInheritancePolicy().classFromRow(entityRecord, query.getSession());
                descriptor = query.getSession().getDescriptor(classValue);
            }
        }
        for (Iterator mappings = descriptor.getMappings().iterator(); mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            FieldResult fieldResult = (FieldResult)this.getFieldResults().get(mapping.getAttributeName());
            if (fieldResult != null){
                if (mapping.getFields().size() == 1 ) {
                    entityRecord.put(mapping.getFields().firstElement(), record.get(fieldResult.getColumnName()));
                } else if (mapping.getFields().size() >1){
                    getValueFromRecordForMapping(entityRecord,mapping,fieldResult,record);
                }
            } else {
                for (Iterator fields = mapping.getFields().iterator(); fields.hasNext();) {
                    DatabaseField field = (DatabaseField)fields.next();
                    entityRecord.put(field, record.get(field));
                }
            }
        }
        query.setReferenceClass(this.entityClass);
        query.setDescriptor(descriptor);
        return descriptor.getObjectBuilder().buildObject(query, entityRecord, null);
    }

    public boolean isEntityResult(){
        return true;
    }
    
    /**
     * INTERNAL:
     *   This method is for processing all FieldResults for a mapping.  Adds DatabaseFields to the passed in entityRecord
     */
    public void getValueFromRecordForMapping(DatabaseRecord entityRecord,DatabaseMapping mapping, FieldResult fieldResult, DatabaseRecord databaseRecord){
        ClassDescriptor currentDescriptor = mapping.getReferenceDescriptor();
        /** check if this FieldResult contains any other FieldResults, process it if it doesn't */
        if (fieldResult.getFieldResults()==null){
            DatabaseField dbfield = processValueFromRecordForMapping(currentDescriptor,fieldResult.getMultipleFieldIdentifiers(),1);
            /** If it is a 1:1 mapping we need to do the target to source field conversion.  If it is an aggregate, it is fine as it is*/
            if (mapping.isOneToOneMapping()){
                dbfield = (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(dbfield));
            }
            entityRecord.put(dbfield, databaseRecord.get(fieldResult.getColumnName()));
            return;
        }
        /** This processes each FieldResult stored in the collection of FieldResults individually */
        Iterator fieldResults = fieldResult.getFieldResults().iterator();
        while (fieldResults.hasNext()){
            FieldResult tempFieldResult = ((FieldResult)fieldResults.next());
            DatabaseField dbfield = processValueFromRecordForMapping(currentDescriptor,tempFieldResult.getMultipleFieldIdentifiers(),1);
             if (mapping.isOneToOneMapping()){
                dbfield = (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(dbfield));
            }
            entityRecord.put(dbfield, databaseRecord.get(tempFieldResult.getColumnName()));
        }
    }
    
    /**
     * INTERNAL:
     *   This method is for processing a single FieldResult, returning the DatabaseField it refers to.
     */
    public DatabaseField processValueFromRecordForMapping(ClassDescriptor descriptor, String[] attributeNames, int currentLoc){
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeNames[currentLoc]);
        if (mapping==null){throw QueryException.mappingForFieldResultNotFound(attributeNames,currentLoc);}
        currentLoc++;
        if (attributeNames.length!=currentLoc){
            ClassDescriptor currentDescriptor = mapping.getReferenceDescriptor();
            DatabaseField df= processValueFromRecordForMapping(currentDescriptor, attributeNames, currentLoc);
            if (mapping.isOneToOneMapping()){
                return (DatabaseField)(((OneToOneMapping)mapping).getTargetToSourceKeyFields().get(df));
            }
            return df;
        }else{
            //this is it.. return this mapping's field
            return (DatabaseField) mapping.getFields().firstElement();
        }
    }
    
}
