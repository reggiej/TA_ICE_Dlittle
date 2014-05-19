// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.ChangeRecord;
import oracle.toplink.internal.sessions.MergeManager;
import oracle.toplink.internal.sessions.ObjectChangeSet;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.mappings.foundation.AbstractCompositeCollectionMapping;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import oracle.toplink.queryframework.ObjectLevelReadQuery;

/**
 * Chunks of data from non-relational data sources can have
 * embedded collections of component objects. These can be
 * mapped using this mapping. The format of the embedded
 * data is determined by the reference descriptor.
 *
 * @see SDKDescriptor
 * @see SDKFieldValue
 * @see oracle.toplink.sdk.SDKCollectionMappingHelper
 * @see oracle.toplink.sdk.SDKCollectionChangeRecord
 * @see oracle.toplink.sdk.SDKOrderedCollectionChangeRecord
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKAggregateCollectionMapping extends AbstractCompositeCollectionMapping implements SDKCollectionMapping {

    /**
     * Default constructor.
     */
    public SDKAggregateCollectionMapping() {
        super();
    }

    /**
     * PUBLIC:
     * Return the mapping's field name.
     * This is the field in the database row that will
     * hold the nested rows that make up the aggregate collection.
     */
    public String getFieldName() {
        return this.getField().getName();
    }

    /**
     * PUBLIC:
     * Set the mapping's field name.
     * This is the field in the database row that will
     * hold the nested rows that make up the aggregate collection.
     */
    public void setFieldName(String fieldName) {
        this.setField(new DatabaseField(fieldName));
    }

    protected Object buildCompositeObject(ClassDescriptor descriptor, AbstractRecord nestedRow, ObjectBuildingQuery query, JoinedAttributeManager joinManager) {
        Object element = descriptor.getObjectBuilder().buildNewInstance();
        descriptor.getObjectBuilder().buildAttributesIntoObject(element, nestedRow, query, joinManager, false);
        return element;
    }

    protected AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord parentRow) {
        return this.getObjectBuilder(attributeValue, session).buildRow(attributeValue, session);
    }

    /**
     * INTERNAL:
     * Build and return the change record that results
     * from comparing the two aggregate collection attributes.
     */
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        // Fixed to match build-update-row.
        if (session.isClassReadOnly(this.getReferenceClass())) {
            return null;
        }
        return (new SDKCollectionMappingHelper(this)).compareForChange(clone, backup, owner, session);
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object object1, Object object2, AbstractSession session) {
        return (new SDKCollectionMappingHelper(this)).compareObjects(object1, object2, session);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        (new SDKCollectionMappingHelper(this)).mergeChangesIntoObject(target, changeRecord, source, mergeManager);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     * Simply replace the entire target collection.
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        //Helper.toDo("bjv: need to figure out how to handle read-only elements...");
        if (mergeManager.getSession().isClassReadOnly(this.getReferenceClass())) {
            return;
        }

        (new SDKCollectionMappingHelper(this)).mergeIntoObject(target, isTargetUnInitialized, source, mergeManager);
    }

    /**
     * ADVANCED:
     * This method is used to have an object add to a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        (new SDKCollectionMappingHelper(this)).simpleAddToCollectionChangeRecord(referenceKey, changeSetToAdd, changeSet, session);
    }

    /**
     * ADVANCED:
     * This method is used to have an object removed from a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object changeSetToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        (new SDKCollectionMappingHelper(this)).simpleRemoveFromCollectionChangeRecord(referenceKey, changeSetToRemove, changeSet, session);
    }
}