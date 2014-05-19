// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.mappings;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.expressions.Expression;
import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.mappings.ObjectReferenceMapping;
import oracle.toplink.queryframework.*;

/**
 * <p>An EIS one-to-one mapping is a reference mapping that represents the relationship between 
 * a single source object and a single mapped persistent Java object.  The source object usually 
 * contains a foreign key (pointer) to the target object (key on source); alternatively, the target 
 * object may contiain a foreign key to the source object (key on target).  Because both the source 
 * and target objects use interactions, they must both be configured as root object types.  
 * 
 * <p><table border="1">
 * <tr>
 * <th id="c1" align="left">Record Type</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1">Indexed</td>
 * <td headers="c2">Ordered collection of record elements.  The indexed record EIS format 
 * enables Java class attribute values to be retreived by position or index.</td>
 * </tr>
 * <tr>
 * <td headers="c1">Mapped</td>
 * <td headers="c2">Key-value map based representation of record elements.  The mapped record
 * EIS format enables Java class attribute values to be retreived by an object key.</td>
 * </tr>
 * <tr>
 * <td headers="c1">XML</td>
 * <td headers="c2">Record/Map representation of an XML DOM element.</td>
 * </tr>
 * </table>
 * 
 * @see oracle.toplink.eis.EISDescriptor#useIndexedRecordFormat
 * @see oracle.toplink.eis.EISDescriptor#useMappedRecordFormat
 * @see oracle.toplink.eis.EISDescriptor#useXMLRecordFormat
 * 
 * @since Oracle TopLink 10<i>g</i> Release 2 (10.1.3)
 */
public class EISOneToOneMapping extends ObjectReferenceMapping implements EISMapping {

    /** Maps the source foreign/primary key fields to the target primary/foreign key fields. */

    protected Map sourceToTargetKeyFields;

    /** Maps the target primary/foreign key fields to the source foreign/primary key fields. */
    protected Map targetToSourceKeyFields;

    /** These are used for non-unit of work modification to check if the value of the 1-1 was changed and a deletion is required. */
    protected boolean shouldVerifyDelete;
    protected transient Expression privateOwnedCriteria;

    public EISOneToOneMapping() {
        this.selectionQuery = new ReadObjectQuery();

        this.foreignKeyFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(1);

        this.sourceToTargetKeyFields = new HashMap(2);
        this.targetToSourceKeyFields = new HashMap(2);
    }

    /**
     * INTERNAL:
     */
    public boolean isEISMapping() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public boolean isOneToOneMapping() {
        return true;
    }

    /**
         * PUBLIC:
         * Define the source foreign key relationship in the one-to-one mapping.
         * This method is used to add foreign key relationships to the mapping.
         * Both the source foreign key field name and the corresponding
         * target primary key field name must be specified.
         */
    public void addForeignKeyField(DatabaseField sourceForeignKeyField, DatabaseField targetKeyField) {
        this.getSourceToTargetKeyFields().put(sourceForeignKeyField, targetKeyField);
        this.getTargetToSourceKeyFields().put(targetKeyField, sourceForeignKeyField);

        this.getForeignKeyFields().add(sourceForeignKeyField);
        this.setIsForeignKeyRelationship(true);
    }

    /**
     * PUBLIC:
     * Define the source foreign key relationship in the one-to-one mapping.
     * This method is used to add foreign key relationships to the mapping.
     * Both the source foreign key field name and the corresponding
     * target primary key field name must be specified.
     */
    public void addForeignKeyFieldName(String sourceForeignKeyFieldName, String targetKeyFieldName) {
        this.addForeignKeyField(new DatabaseField(sourceForeignKeyFieldName), new DatabaseField(targetKeyFieldName));
    }

    /**
     * INTERNAL:
     * This methods clones all the fields and ensures that each collection refers to
     * the same clones.
     */
    public Object clone() {
        EISOneToOneMapping clone = (EISOneToOneMapping)super.clone();
        clone.setForeignKeyFields(oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(getForeignKeyFields().size()));
        clone.setSourceToTargetKeyFields(new HashMap(getSourceToTargetKeyFields().size()));
        clone.setTargetToSourceKeyFields(new HashMap(getTargetToSourceKeyFields().size()));
        Map setOfFields = new HashMap(getTargetToSourceKeyFields().size());

        for (Enumeration enumtr = getForeignKeyFields().elements(); enumtr.hasMoreElements();) {
            DatabaseField field = (DatabaseField)enumtr.nextElement();

            DatabaseField fieldClone = (DatabaseField)field.clone();
            setOfFields.put(field, fieldClone);
            clone.getForeignKeyFields().addElement(fieldClone);
        }

        //get clones from set for source hashtable.  If they do not exist, create a new one.		
        Iterator sourceKeyIterator = getSourceToTargetKeyFields().keySet().iterator();
        while (sourceKeyIterator.hasNext()) {
            DatabaseField sourceField = (DatabaseField)sourceKeyIterator.next();
            DatabaseField targetField = (DatabaseField)getSourceToTargetKeyFields().get(sourceField);

            DatabaseField targetClone = (DatabaseField)setOfFields.get(targetField);
            if (targetClone == null) {
                targetClone = (DatabaseField)targetField.clone();
                setOfFields.put(targetField, targetClone);
            }

            DatabaseField sourceClone = (DatabaseField)sourceField.clone();
            if (sourceClone == null) {
                sourceClone = (DatabaseField)sourceField.clone();
                setOfFields.put(sourceField, sourceClone);
            }
            clone.getSourceToTargetKeyFields().put(sourceClone, targetClone);
        }

        //get clones from set for target hashtable.  If they do not exist, create a new one.						
        Iterator targetKeyIterator = getTargetToSourceKeyFields().keySet().iterator();
        while (targetKeyIterator.hasNext()) {
            DatabaseField targetField = (DatabaseField)targetKeyIterator.next();
            DatabaseField sourceField = (DatabaseField)getTargetToSourceKeyFields().get(targetField);

            DatabaseField targetClone = (DatabaseField)setOfFields.get(targetField);
            if (targetClone == null) {
                targetClone = (DatabaseField)targetField.clone();
                setOfFields.put(targetField, targetClone);
            }

            DatabaseField sourceClone = (DatabaseField)setOfFields.get(sourceField);
            if (sourceClone == null) {
                sourceClone = (DatabaseField)sourceField.clone();
                setOfFields.put(sourceField, sourceClone);
            }
            clone.getTargetToSourceKeyFields().put(targetClone, sourceClone);
        }

        return clone;
    }

    /**
     * INTERNAL:
     * Extract the foreign key value from the source row.
     */
    protected Vector extractForeignKeyFromRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector();

        Iterator sourceKeyIterator = getSourceToTargetKeyFields().keySet().iterator();
        while (sourceKeyIterator.hasNext()) {
            DatabaseField field = (DatabaseField)sourceKeyIterator.next();
            Object value = row.get(field);

            // Must ensure the classificatin to get a cache hit.
            try {
                value = session.getDatasourcePlatform().getConversionManager().convertObject(value, getDescriptor().getObjectBuilder().getFieldClassification(field));
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
            }

            key.addElement(value);
        }

        return key;
    }

    /**
     * INTERNAL:
     * Extract the key value from the reference object.
     */
    protected Vector extractKeyFromReferenceObject(Object object, AbstractSession session) {
        Vector key = new Vector();

        Iterator sourceKeyIterator = getSourceToTargetKeyFields().keySet().iterator();
        while (sourceKeyIterator.hasNext()) {
            DatabaseField field = (DatabaseField)sourceKeyIterator.next();
            if (object == null) {
                key.addElement(null);
            } else {
                key.addElement(getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(object, field, session));
            }
        }
        return key;
    }

    /**
     * INTERNAL:
     * Return the primary key for the reference object (i.e. the object
     * object referenced by domainObject and specified by mapping).
     * This key will be used by a RemoteValueHolder.
     */
    public Vector extractPrimaryKeysForReferenceObjectFromRow(AbstractRecord row) {
        List primaryKeyFields = getReferenceDescriptor().getPrimaryKeyFields();
        Vector result = new Vector(primaryKeyFields.size());
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField targetKeyField = (DatabaseField)primaryKeyFields.get(index);
            DatabaseField sourceKeyField = (DatabaseField)getTargetToSourceKeyFields().get(targetKeyField);
            if (sourceKeyField == null) {
                return new Vector(1);
            }
            result.addElement(row.get(sourceKeyField));
        }
        return result;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        // Must build foreign keys fields.
        List foreignKeyFields = getForeignKeyFields();
        int size = foreignKeyFields.size();
        for (int index = 0; index < size; index++) {
            DatabaseField foreignKeyField = (DatabaseField)foreignKeyFields.get(index);
            foreignKeyField = getDescriptor().buildField(foreignKeyField);
            foreignKeyFields.set(index, foreignKeyField);
        }

        initializeForeignKeys(session);

        if (shouldInitializeSelectionCriteria()) {
            initializeSelectionCriteria(session);
        } else {
            setShouldVerifyDelete(false);
        }
        setFields(collectFields());
    }

    /**
     * INTERNAL:
     * The foreign keys primary keys are stored as database fields in the hashtable.
     */
    protected void initializeForeignKeys(AbstractSession session) {
        Iterator sourceKeyIterator = new HashMap(getSourceToTargetKeyFields()).keySet().iterator();
        while (sourceKeyIterator.hasNext()) {
            DatabaseField sourceField = (DatabaseField)sourceKeyIterator.next();
            DatabaseField targetField = (DatabaseField)getSourceToTargetKeyFields().get(sourceField);

            sourceField = getDescriptor().buildField(sourceField);
            targetField = getReferenceDescriptor().buildField(targetField);
            getSourceToTargetKeyFields().put(sourceField, targetField);
            getTargetToSourceKeyFields().put(targetField, sourceField);
        }
    }

    /**
     * INTERNAL:
     * Selection criteria is created with source foreign keys and target keys.
     * This criteria is then used to read target records from the table.
     *
     * CR#3922 - This method is almost the same as buildSelectionCriteria() the difference
     * is that getSelectionCriteria() is called
     */
    protected void initializeSelectionCriteria(AbstractSession session) {
        if (this.getSourceToTargetKeyFields().isEmpty()) {
            throw DescriptorException.noForeignKeysAreSpecified(this);
        }

        Expression criteria;
        Expression builder = new ExpressionBuilder();
        Iterator keyIterator = getSourceToTargetKeyFields().keySet().iterator();
        while (keyIterator.hasNext()) {
            DatabaseField foreignKey = (DatabaseField)keyIterator.next();
            DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(foreignKey);

            Expression expression = builder.getField(targetKey).equal(builder.getParameter(foreignKey));
            criteria = expression.and(getSelectionCriteria());
            setSelectionCriteria(criteria);
        }
    }

    /**
     * INTERNAL:
     * Reads the private owned object.
     */
    protected Object readPrivateOwnedForObject(ObjectLevelModifyQuery modifyQuery) throws DatabaseException {
        if (modifyQuery.getSession().isUnitOfWork()) {
            return getRealAttributeValueFromObject(modifyQuery.getBackupClone(), modifyQuery.getSession());
        } else {
            if (!shouldVerifyDelete()) {
                return null;
            }
            ReadObjectQuery readQuery = (ReadObjectQuery)getSelectionQuery().clone();

            readQuery.setSelectionCriteria(getPrivateOwnedCriteria());
            return modifyQuery.getSession().executeQuery(readQuery, modifyQuery.getTranslationRow());
        }
    }

    /**
     * INTERNAL:
     * Selection criteria is created with source foreign keys and target keys.
     */
    protected void initializePrivateOwnedCriteria() {
        if (!isForeignKeyRelationship()) {
            setPrivateOwnedCriteria(getSelectionCriteria());
        } else {
            Expression pkCriteria = getDescriptor().getObjectBuilder().getPrimaryKeyExpression();
            ExpressionBuilder builder = new ExpressionBuilder();
            Expression backRef = builder.getManualQueryKey(getAttributeName() + "-back-ref", getDescriptor());
            Expression newPKCriteria = pkCriteria.rebuildOn(backRef);
            Expression twistedSelection = backRef.twist(getSelectionCriteria(), builder);
            if (getDescriptor().getQueryManager().getAdditionalJoinExpression() != null) {
                // We don't have to twist the additional join because it's all against the same node, which is our base
                // but we do have to rebuild it onto the manual query key
                Expression rebuiltAdditional = getDescriptor().getQueryManager().getAdditionalJoinExpression().rebuildOn(backRef);
                if (twistedSelection == null) {
                    twistedSelection = rebuiltAdditional;
                } else {
                    twistedSelection = twistedSelection.and(rebuiltAdditional);
                }
            }
            setPrivateOwnedCriteria(newPKCriteria.and(twistedSelection));
        }
    }

    /**
     * INTERNAL:
     * Return the value of the field from the row or a value holder on the query to obtain the object.
     * Check for batch + aggregation reading.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession session) throws DatabaseException {
        Object referenceObject;

        // If any field in the foreign key is null then it means there are no referenced objects
        // Skip for partial objects as fk may not be present.
        if (!query.hasPartialAttributeExpressions()) {
            for (Enumeration enumeration = getFields().elements(); enumeration.hasMoreElements();) {
                DatabaseField field = (DatabaseField)enumeration.nextElement();
                if (row.get(field) == null) {
                    return getIndirectionPolicy().nullValueFromRow();
                }
            }
        }

        // Call the default which executes the selection query,
        // or wraps the query with a value holder.
        //return super.valueFromRow(row, query);
        ReadQuery targetQuery = getSelectionQuery();

        // if the source query is cascading then the target query must use the same settings
        if (targetQuery.isObjectLevelReadQuery() && (query.shouldCascadeAllParts() || (query.shouldCascadePrivateParts() && isPrivateOwned()) || (query.shouldCascadeByMapping() && this.cascadeRefresh))) {
            targetQuery = (ObjectLevelReadQuery)targetQuery.clone();
            ((ObjectLevelReadQuery)targetQuery).setShouldRefreshIdentityMapResult(query.shouldRefreshIdentityMapResult());
            targetQuery.setCascadePolicy(query.getCascadePolicy());
            //CR #4365
            targetQuery.setQueryId(query.getQueryId());
            // For queries that have turned caching off, such as aggregate collection, leave it off.
            if (targetQuery.shouldMaintainCache()) {
                targetQuery.setShouldMaintainCache(query.shouldMaintainCache());
            }
        }

        return getIndirectionPolicy().valueFromQuery(targetQuery, row, query.getSession());
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord Record, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        AbstractRecord referenceRow = getIndirectionPolicy().extractReferenceRow(getAttributeValueFromObject(object));
        if (referenceRow == null) {
            // Extract from object.
            Object referenceObject = getRealAttributeValueFromObject(object, session);

            for (int i = 0; i < getForeignKeyFields().size(); i++) {
                DatabaseField sourceKey = (DatabaseField)getForeignKeyFields().get(i);
                DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(sourceKey);

                Object referenceValue = null;

                // If privately owned part is null then method cannot be invoked.
                if (referenceObject != null) {
                    referenceValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(referenceObject, targetKey, session);
                }
                Record.add(sourceKey, referenceValue);
            }
        } else {
            for (int i = 0; i < getForeignKeyFields().size(); i++) {
                DatabaseField sourceKey = (DatabaseField)getForeignKeyFields().get(i);
                Record.add(sourceKey, referenceRow.get(sourceKey));
            }
        }
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) throws DescriptorException {
        DatabaseField fieldInTarget = (DatabaseField)getSourceToTargetKeyFields().get(fieldToClassify);
        if (fieldInTarget == null) {
            return null;// Can be registered as multiple table secondary field mapping
        }
        DatabaseMapping mapping = getReferenceDescriptor().getObjectBuilder().getMappingForField(fieldInTarget);
        if (mapping == null) {
            return null;// Means that the mapping is read-only
        }
        return mapping.getFieldClassification(fieldInTarget);
    }

    /**
     * INTERNAL:
     * The private owned criteria is only used outside of the unit of work to compare the previous value of the reference.
     */
    public Expression getPrivateOwnedCriteria() {
        if (privateOwnedCriteria == null) {
            initializePrivateOwnedCriteria();
        }
        return privateOwnedCriteria;
    }

    /**
     * INTERNAL:
     * Private owned criteria is used to verify the deletion of the target.
     * It joins from the source table on the foreign key to the target table,
     * with a parameterization of the primary key of the source object.
     */
    protected void setPrivateOwnedCriteria(Expression expression) {
        privateOwnedCriteria = expression;
    }

    /**
     * PUBLIC:
     * Verify delete is used during delete and update on private 1:1's outside of a unit of work only.
     * It checks for the previous value of the target object through joining the source and target tables.
     * By default it is always done, but may be disabled for performance on distributed database reasons.
     * In the unit of work the previous value is obtained from the backup-clone so it is never used.
     */
    public void setShouldVerifyDelete(boolean shouldVerifyDelete) {
        this.shouldVerifyDelete = shouldVerifyDelete;
    }

    /**
    * PUBLIC:
    * Verify delete is used during delete and update outside of a unit of work only.
    * It checks for the previous value of the target object through joining the source and target tables.
    */
    public boolean shouldVerifyDelete() {
        return shouldVerifyDelete;
    }

    /**
     * INTERNAL:
     * Gets the foreign key fields.
     */
    public Map getSourceToTargetKeyFields() {
        return sourceToTargetKeyFields;
    }

    /**
     * INTERNAL:
     * Gets the target foreign key fields.
     */
    public Map getTargetToSourceKeyFields() {
        return targetToSourceKeyFields;
    }

    /**
     * INTERNAL:
     * Set the source keys to target keys fields association.
     */
    public void setSourceToTargetKeyFields(Map sourceToTargetKeyFields) {
        this.sourceToTargetKeyFields = sourceToTargetKeyFields;
    }

    /**
     * INTERNAL:
     * Set the source keys to target keys fields association.
     */
    public void setTargetToSourceKeyFields(Map targetToSourceKeyFields) {
        this.targetToSourceKeyFields = targetToSourceKeyFields;
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void setSelectionSQLString(String sqlString) {
        throw DescriptorException.invalidMappingOperation(this, "setSelectionSQLString");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void setUsesBatchReading(boolean usesBatchReading) {
        throw DescriptorException.invalidMappingOperation(this, "setUsesBatchReading");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public boolean shouldUseBatchReading() {
        throw DescriptorException.invalidMappingOperation(this, "shouldUseBatchReading");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void useBatchReading() {
        throw DescriptorException.invalidMappingOperation(this, "useBatchReading");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void dontUseBatchReading() {
        throw DescriptorException.invalidMappingOperation(this, "dontUseBatchReading");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void addAscendingOrdering(String queryKeyName) {
        throw DescriptorException.invalidMappingOperation(this, "addAscendingOrdering");
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void addDescendingOrdering(String queryKeyName) {
        throw DescriptorException.invalidMappingOperation(this, "addDescendingOrdering");
    }
}
