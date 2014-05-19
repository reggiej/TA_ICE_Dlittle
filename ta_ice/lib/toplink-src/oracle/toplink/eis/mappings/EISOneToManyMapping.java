// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.mappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.eis.EISException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.exceptions.OptimisticLockException;
import oracle.toplink.expressions.Expression;
import oracle.toplink.internal.descriptors.ObjectBuilder;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.identitymaps.CacheKey;
import oracle.toplink.internal.indirection.EISOneToManyQueryBasedValueHolder;
import oracle.toplink.ox.XMLField;
import oracle.toplink.internal.ox.XPathEngine;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.mappings.CollectionMapping;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.*;
import org.w3c.dom.Element;

/**
 * <p>An EIS one-to-many mapping is a reference mapping that represents the relationship between 
 * a single source object and a collection of mapped persistent Java objects.  The source object usually 
 * contains a foreign key (pointer) to the target objects (key on source); alternatively, the target 
 * objects may contiain a foreign key to the source object (key on target).  Because both the source 
 * and target objects use interactions, they must all be configured as root object types.  
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
public class EISOneToManyMapping extends CollectionMapping implements EISMapping {

    /** Keeps track if any of the fields are foreign keys. */
    protected boolean isForeignKeyRelationship;

    /** The target foreign key fields that reference the sourceKeyFields. */
    protected transient List targetForeignKeyFields;

    /** The (typically primary) source key fields that are referenced by the targetForeignKeyFields. */
    protected transient List sourceForeignKeyFields;

    /** This maps the source foreign key fields to the corresponding (primary) target key fields. */
    protected transient Map sourceForeignKeysToTargetKeys;

    /** The grouping-element field. */
    protected XMLField foreignKeyGroupingElement;

    public EISOneToManyMapping() {
        this.isForeignKeyRelationship = false;
        this.sourceForeignKeyFields = new ArrayList(1);
        this.targetForeignKeyFields = new ArrayList(1);
        this.sourceForeignKeysToTargetKeys = new HashMap(2);

        this.deleteAllQuery = new DeleteAllQuery();
    }

    /**
     * INTERNAL:
     */
    public boolean isEISMapping() {
        return true;
    }

    /**
         * PUBLIC:
         * Define the source foreign key relationship in the one-to-many mapping.
         * This method is used for composite source foreign key relationships.
         * That is, the source object's table has multiple foreign key fields
         * that are references to
         * the target object's (typically primary) key fields.
         * Both the source foreign key field name and the corresponding
         * target primary key field name must be specified.
         */
    public void addForeignKeyField(DatabaseField sourceForeignKeyField, DatabaseField targetKeyField) {
        this.getSourceForeignKeyFields().add(sourceForeignKeyField);
        this.getTargetForeignKeyFields().add(targetKeyField);

        this.setIsForeignKeyRelationship(true);
    }

    /**
     * PUBLIC:
     * Define the source foreign key relationship in the one-to-many mapping.
     * This method is used for composite source foreign key relationships.
     * That is, the source object's table has multiple foreign key fields
     * that are references to
     * the target object's (typically primary) key fields.
     * Both the source foreign key field name and the corresponding
     * target primary key field name must be specified.
     */
    public void addForeignKeyFieldName(String sourceForeignKeyFieldName, String targetKeyFieldName) {
        this.addForeignKeyField(new DatabaseField(sourceForeignKeyFieldName), new DatabaseField(targetKeyFieldName));
    }

    /**
     * INTERNAL:
     * Return if the 1-M mapping has a foreign key dependency to its target.
     * This is true if any of the foreign key fields are true foreign keys,
     * i.e. populated on write from the targets primary key.
     */
    public boolean isForeignKeyRelationship() {
        return isForeignKeyRelationship;
    }

    /**
     * INTERNAL:
     * Set if the 1-M mapping has a foreign key dependency to its target.
     * This is true if any of the foreign key fields are true foreign keys,
     * i.e. populated on write from the targets primary key.
     */
    public void setIsForeignKeyRelationship(boolean isForeignKeyRelationship) {
        this.isForeignKeyRelationship = isForeignKeyRelationship;
    }

    /**
     * Get the grouping element field on the mapping.
     * This is an optional setting.
     */
    public XMLField getForeignKeyGroupingElement() {
        return this.foreignKeyGroupingElement;
    }

    /**
     * Set the grouping element field on the mapping.
     * This is an optional setting; however it is a required setting when
     * there are more than one foreign keys specified
     */
    public void setForeignKeyGroupingElement(String xpath) {
        setForeignKeyGroupingElement(new XMLField(xpath));
    }

    public boolean hasCustomDeleteAllQuery() {
        return hasCustomDeleteAllQuery;
    }

    public ModifyQuery getDeleteAllQuery() {
        if (deleteAllQuery == null) {
            deleteAllQuery = new DataModifyQuery();
        }
        return deleteAllQuery;
    }

    /**
     * PUBLIC:
     * The default delete all call for this mapping can be overridden by specifying the new call.
     * This call is responsible for doing the deletion required by the mapping,
     * such as optimized delete all of target objects for 1-M.
     */
    public void setDeleteAllCall(Call call) {
        DeleteAllQuery deleteAllQuery = new DeleteAllQuery();
        deleteAllQuery.setCall(call);
        setDeleteAllQuery(deleteAllQuery);
        setHasCustomDeleteAllQuery(true);
    }

    /**
     * Set if the grouping element field on the mapping.
     * This is an optional setting; however it is a required setting when
     * there are more than one foreign keys specified.
     */
    public void setForeignKeyGroupingElement(XMLField field) {
        this.foreignKeyGroupingElement = field;
    }

    /**
     * INTERNAL:
     * Return the source foreign key fields.
     */
    public List getSourceForeignKeyFields() {
        return sourceForeignKeyFields;
    }

    /**
     * INTERNAL:
     * Sets the source foreign key fields.
     */
    public void setSourceForeignKeyFields(List fields) {
        sourceForeignKeyFields = fields;
        if ((fields != null) && (fields.size() > 0)) {
            this.setIsForeignKeyRelationship(true);
        }
    }

    /**
     * INTERNAL:
     * Return the source foreign key fields.
     */
    public List getTargetForeignKeyFields() {
        return targetForeignKeyFields;
    }

    /**
     * INTERNAL:
     * Sets the target foreign key fields.
     */
    public void setTargetForeignKeyFields(List fields) {
        targetForeignKeyFields = fields;
    }

    /**
    * INTERNAL:
    * Sets the target foreign key fields.
    */
    public Map getSourceForeignKeysToTargetKeys() {
        return sourceForeignKeysToTargetKeys;
    }

    /**
     * INTERNAL:
     * Set the source keys to target keys fields association.
     */
    public void setSourceForeignKeysToTargetKeys(Map sourceToTargetKeyFields) {
        this.sourceForeignKeysToTargetKeys = sourceToTargetKeyFields;
        if ((sourceToTargetKeyFields != null) && (sourceToTargetKeyFields.keySet() != null) && (sourceToTargetKeyFields.keySet().size() > 0)) {
            this.setIsForeignKeyRelationship(true);
        }
    }

    /**
     * INTERNAL:
     * Return whether the mapping has any inverse constraint dependencies,
     * such as foreign keys.
     */
    public boolean hasInverseConstraintDependency() {
        return true;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        if (!this.hasCustomSelectionQuery()) {
            throw DescriptorException.operationNotSupported("customSelectionQueryRequired");
        }

        if ((this.getForeignKeyGroupingElement() == null) && (this.getSourceForeignKeysToTargetKeys().size() > 1)) {
            throw EISException.groupingElementRequired();
        }

        if (this.getForeignKeyGroupingElement() != null) {
            DatabaseField field = this.getDescriptor().buildField(this.getForeignKeyGroupingElement());
            setForeignKeyGroupingElement((XMLField)field);
        }

        this.initializeSourceForeignKeysToTargetKeys();

        this.initializeDeleteAllQuery();
    }

    protected void initializeSourceForeignKeysToTargetKeys() throws DescriptorException {
        // Since we require a custom selection query, these keys are optional.
        if (getSourceForeignKeyFields().size() != getTargetForeignKeyFields().size()) {
            throw DescriptorException.sizeMismatchOfForeignKeys(this);
        }

        for (int i = 0; i < getTargetForeignKeyFields().size(); i++) {
            DatabaseField field = getReferenceDescriptor().buildField((DatabaseField)getTargetForeignKeyFields().get(i));
            getTargetForeignKeyFields().set(i, field);
        }

        for (int i = 0; i < getSourceForeignKeyFields().size(); i++) {
            DatabaseField field = getReferenceDescriptor().buildField((DatabaseField)getSourceForeignKeyFields().get(i));
            getSourceForeignKeyFields().set(i, field);
            getSourceForeignKeysToTargetKeys().put(field, getTargetForeignKeyFields().get(i));
        }
    }

    /**
     * Initialize the delete all query.
     * This query is used to delete the collection of objects from the
     * database.
     **/
    protected void initializeDeleteAllQuery() {
        ((DeleteAllQuery)this.getDeleteAllQuery()).setReferenceClass(this.getReferenceClass());
        if (!this.hasCustomDeleteAllQuery()) {
            // the selection criteria are re-used by the delete all query
            this.getDeleteAllQuery().setSelectionCriteria(this.getSelectionCriteria());
        }
    }

    /**
     * Return whether the reference objects must be deleted
     * one by one, as opposed to with a single DELETE statement.
     */
    protected boolean mustDeleteReferenceObjectsOneByOne() {
        ClassDescriptor referenceDescriptor = this.getReferenceDescriptor();
        return referenceDescriptor.hasDependencyOnParts() || referenceDescriptor.usesOptimisticLocking() || (referenceDescriptor.hasInheritance() && referenceDescriptor.getInheritancePolicy().shouldReadSubclasses()) || referenceDescriptor.hasMultipleTables();
    }

    /**
     * Return whether any process leading to object modification
     * should also affect its parts.
     * Used by write, insert, update, and delete.
     */
    protected boolean shouldObjectModifyCascadeToParts(ObjectLevelModifyQuery query) {
        if (isForeignKeyRelationship()) {
            return super.shouldObjectModifyCascadeToParts(query);
        } else {
            if (this.isReadOnly()) {
                return false;
            }

            if (this.isPrivateOwned()) {
                return true;
            }

            return query.shouldCascadeAllParts();
        }
    }

    /**
     * INTERNAL:
     * Used to verify whether the specified object is deleted or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        if (this.isPrivateOwned()) {
            Object objects = this.getRealCollectionAttributeValueFromObject(object, session);

            ContainerPolicy containerPolicy = getContainerPolicy();
            for (Object iter = containerPolicy.iteratorFor(objects); containerPolicy.hasNext(iter);) {
                if (!session.verifyDelete(containerPolicy.next(iter, session))) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * INTERNAL:
     * Insert the reference objects.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // only cascade dependents in UOW
        if (query.shouldCascadeOnlyDependentParts()) {
            return;
        }

        Object objects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        // insert each object one by one
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            Object object = cp.next(iter, query.getSession());
            if (this.isPrivateOwned()) {
                // no need to set changeSet as insert is a straight copy
                InsertObjectQuery insertQuery = new InsertObjectQuery();
                insertQuery.setIsExecutionClone(true);
                insertQuery.setObject(object);
                insertQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(insertQuery);
            } else {
                // This will happen in a unit of work or cascaded query.
                // This is done only for persistence by reachability and is not required if the targets are in the queue anyway
                // Avoid cycles by checking commit manager, this is allowed because there is no dependency.
                if (!query.getSession().getCommitManager().isCommitInPreModify(object)) {
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
        }
    }

    /**
     * INTERNAL:
     * Update the reference objects.
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // if the target objects are not instantiated, they could not have been changed....
        if (!this.isAttributeValueInstantiatedOrChanged(query.getObject())) {
            return;
        }

        // manage objects added and removed from the collection
        Object objectsInMemory = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        Object objectsInDB = this.readPrivateOwnedForObject(query);

        this.compareObjectsAndWrite(objectsInDB, objectsInMemory, query);
    }

    /**
     * INTERNAL:
     * Delete the reference objects.
     */
    @Override
    public void postDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }
        Object referenceObjects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        // if we have a custom delete all query, use it;
        // otherwise, delete the reference objects one by one
        if (this.hasCustomDeleteAllQuery()) {
            this.deleteAll(query, referenceObjects);
        } else {
            ContainerPolicy cp = this.getContainerPolicy();
            for (Object iter = cp.iteratorFor(referenceObjects); cp.hasNext(iter);) {
                DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                deleteQuery.setIsExecutionClone(true);
                deleteQuery.setObject(cp.next(iter, query.getSession()));
                deleteQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(deleteQuery);
            }
            if (!query.getSession().isUnitOfWork()) {
                // This deletes any objects on the database, as the collection in memory may have been changed.
                // This is not required for unit of work, as the update would have already deleted these objects,
                // and the backup copy will include the same objects, causing double deletes.
                this.deleteReferenceObjectsLeftOnDatabase(query);
            }
        }
    }

    /**
     * INTERNAL:
     * Delete the reference objects.
     */
    public void preDelete(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        Object objects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        ContainerPolicy cp = this.getContainerPolicy();

        // if privately-owned parts have their privately-owned sub-parts, delete them one by one;
        // else delete everything in one shot
        if (this.mustDeleteReferenceObjectsOneByOne()) {
            for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
                DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                deleteQuery.setIsExecutionClone(true);
                deleteQuery.setObject(cp.next(iter, query.getSession()));
                deleteQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(deleteQuery);
            }
            if (!query.getSession().isUnitOfWork()) {
                // This deletes any objects on the database, as the collection in memory may have been changed.
                // This is not required for unit of work, as the update would have already deleted these objects,
                // and the backup copy will include the same objects causing double deletes.
                this.deleteReferenceObjectsLeftOnDatabase(query);
            }
        } else {
            this.deleteAll(query);
        }
    }

    /**
    * INTERNAL:
    * Insert privately owned parts
    */
    public void preInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!this.isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // only cascade dependents in UOW
        if (query.shouldCascadeOnlyDependentParts()) {
            return;
        }

        Object objects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        // insert each object one by one
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            Object object = cp.next(iter, query.getSession());
            if (this.isPrivateOwned()) {
                // no need to set changeset here as insert is just a copy of the object anyway
                InsertObjectQuery insertQuery = new InsertObjectQuery();
                insertQuery.setIsExecutionClone(true);
                insertQuery.setObject(object);
                insertQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(insertQuery);
            } else {
                // This will happen in a unit of work or cascaded query.
                // This is done only for persistence by reachability and is not required if the targets are in the queue anyway
                // Avoid cycles by checking commit manager, this is allowed because there is no dependency.
                if (!query.getSession().getCommitManager().isCommitInPreModify(object)) {
                    WriteObjectQuery writeQuery = new WriteObjectQuery();
                    writeQuery.setIsExecutionClone(true);
                    if (query.getSession().isUnitOfWork()) {
                        UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                        if (uowChangeSet != null) {
                            writeQuery.setObjectChangeSet((ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object));
                        }
                    }
                    writeQuery.setObject(object);
                    writeQuery.setCascadePolicy(query.getCascadePolicy());
                    query.getSession().executeQuery(writeQuery);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Update the privately owned parts.
     */
    public void preUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!this.isForeignKeyRelationship()) {
            return;
        }

        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // if the target objects are not instantiated, they could not have been changed....
        if (!this.isAttributeValueInstantiatedOrChanged(query.getObject())) {
            return;
        }

        // manage objects added and removed from the collection
        Object objectsInMemory = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        Object objectsInDB = this.readPrivateOwnedForObject(query);

        this.compareObjectsAndWrite(objectsInDB, objectsInMemory, query);
    }

    /**
     * INTERNAL:
     * Build and return a new element based on the change set.
     */
    public Object buildAddedElementFromChangeSet(Object changeSet, MergeManager mergeManager) {
        ObjectChangeSet objectChangeSet = (ObjectChangeSet)changeSet;

        if (this.shouldMergeCascadeParts(mergeManager)) {
            Object targetElement = null;
            if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                targetElement = objectChangeSet.getTargetVersionOfSourceObject(mergeManager.getSession(), true);
            } else {
                targetElement = objectChangeSet.getUnitOfWorkClone();
            }
            mergeManager.mergeChanges(targetElement, objectChangeSet);
        }

        return this.buildElementFromChangeSet(changeSet, mergeManager);
    }

    /**
     * INTERNAL:
     * Build and return a change set for the specified element.
     */
    public Object buildChangeSet(Object element, ObjectChangeSet owner, AbstractSession session) {
        ObjectBuilder objectBuilder = session.getDescriptor(element).getObjectBuilder();
        return objectBuilder.createObjectChangeSet(element, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
    }

    /**
     * Build and return a new element based on the change set.
     */
    protected Object buildElementFromChangeSet(Object changeSet, MergeManager mergeManager) {
        return ((ObjectChangeSet)changeSet).getTargetVersionOfSourceObject(mergeManager.getSession());
    }

    /**
     * INTERNAL:
     * Build and return a new element based on the specified element.
     */
    public Object buildElementFromElement(Object element, MergeManager mergeManager) {
        if (this.shouldMergeCascadeParts(mergeManager)) {
            ObjectChangeSet objectChangeSet = null;
            if (mergeManager.getSession().isUnitOfWork()) {
                UnitOfWorkChangeSet uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)mergeManager.getSession()).getUnitOfWorkChangeSet();
                if (uowChangeSet != null) {
                    objectChangeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(element);
                }
            }
            Object mergeElement = mergeManager.getObjectToMerge(element);
            mergeManager.mergeChanges(mergeElement, objectChangeSet);
        }

        return mergeManager.getTargetVersionOfSourceObject(element);

    }

    /**
     * INTERNAL:
     * Build and return a new element based on the change set.
     */
    public Object buildRemovedElementFromChangeSet(Object changeSet, MergeManager mergeManager) {
        ObjectChangeSet objectChangeSet = (ObjectChangeSet)changeSet;

        if (!mergeManager.shouldMergeChangesIntoDistributedCache()) {
            mergeManager.registerRemovedNewObjectIfRequired(objectChangeSet.getUnitOfWorkClone());
        }

        return this.buildElementFromChangeSet(changeSet, mergeManager);
    }

    /**
     * INTERNAL:
     * Clone the appropriate attributes.
     */
    public Object clone() {
        EISOneToManyMapping clone = (EISOneToManyMapping)super.clone();
        clone.setSourceForeignKeysToTargetKeys((Map)((HashMap)getSourceForeignKeysToTargetKeys()).clone());
        return clone;
    }

    /**
     * Return all the fields mapped by the mapping.
     */
    protected Vector collectFields() {
        if (isForeignKeyRelationship()) {
            if (this.getForeignKeyGroupingElement() != null) {
                Vector fields = new Vector(1);
                fields.addElement(this.getForeignKeyGroupingElement());
                return fields;
            } else {
                return NO_FIELDS;
            }
        } else {
            return NO_FIELDS;
        }
    }

    /**
     * INTERNAL:
     * Compare the non-null elements and return true if they are alike.
     */
    public boolean compareElements(Object element1, Object element2, AbstractSession session) {
        if (!isForeignKeyRelationship()) {
            return false;
        }

        Vector primaryKey1 = this.getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(element1, session);
        Vector primaryKey2 = this.getReferenceDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(element2, session);

        CacheKey cacheKey1 = new CacheKey(primaryKey1);
        CacheKey cacheKey2 = new CacheKey(primaryKey2);

        if (!cacheKey1.equals(cacheKey2)) {
            return false;
        }

        if (this.isPrivateOwned()) {
            return session.compareObjects(element1, element2);
        } else {
            return true;
        }
    }

    /**
     * INTERNAL:
     * Return whether the element's user-defined Map key has changed
     * since it was cloned from the original version.
     * Object elements can change their keys without detection.
     * Get the original object and compare keys.
     */
    public boolean mapKeyHasChanged(Object element, AbstractSession session) {
        //CR 4172 compare keys will now get backup if required
        return !this.getContainerPolicy().compareKeys(element, session);
    }

    /**
     * INTERNAL:
     * Compare the non-null elements and return true if they are alike.
     * Here we use object identity.
     */
    public boolean compareElementsForChange(Object element1, Object element2, AbstractSession session) {
        return element1 == element2;
    }

    /**
     * INTERNAL:
     * Compare the changes between two collections. Element comparisons are
     * made using identity and, when appropriate, the value of the element's key
     * for the Map container.
     */
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        if (isForeignKeyRelationship()) {
            if ((this.getAttributeValueFromObject(clone) != null) && (!this.isAttributeValueInstantiatedOrChanged(clone))) {
                return null;// never instantiated - no changes to report
            }
            return (new EISOneToManyMappingHelper(this)).compareForChange(clone, backup, owner, session);
        } else {
            return super.compareForChange(clone, backup, owner, session);
        }
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object object1, Object object2, AbstractSession session) {
        if (isForeignKeyRelationship()) {
            return (new EISOneToManyMappingHelper(this)).compareObjects(object1, object2, session);
        }
        return super.compareObjects(object1, object2, session);
    }

    /**
    * ADVANCED:
    * This method is used to have an object add to a collection once the changeSet is applied
    * The referenceKey parameter should only be used for direct Maps.
    */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        (new EISOneToManyMappingHelper(this)).simpleAddToCollectionChangeRecord(referenceKey, changeSetToAdd, changeSet, session);
    }

    /**
    * ADVANCED:
    * This method is used to have an object removed from a collection once the changeSet is applied
    * The referenceKey parameter should only be used for direct Maps.
    */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object changeSetToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        (new EISOneToManyMappingHelper(this)).simpleRemoveFromCollectionChangeRecord(referenceKey, changeSetToRemove, changeSet, session);
    }

    /**
     * Delete all the reference objects.
     */
    protected void deleteAll(DeleteObjectQuery query, Object referenceObjects) throws DatabaseException {
        ((DeleteAllQuery)this.getDeleteAllQuery()).executeDeleteAll(query.getSession().getSessionForClass(this.getReferenceClass()), query.getTranslationRow(), this.getContainerPolicy().vectorFor(referenceObjects, query.getSession()));
    }

    /**
     * Delete all the reference objects.
     */
    protected void deleteAll(DeleteObjectQuery query) throws DatabaseException {
        Object referenceObjects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        deleteAll(query, referenceObjects);
    }

    /**
     *    This method will make sure that all the records privately owned by this mapping are
     * actually removed. If such records are found then those are all read and removed one
     * by one along with their privately owned parts.
     */
    protected void deleteReferenceObjectsLeftOnDatabase(DeleteObjectQuery query) throws DatabaseException, OptimisticLockException {
        Object objects = this.readPrivateOwnedForObject(query);

        // delete all these objects one by one
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            query.getSession().deleteObject(cp.next(iter, query.getSession()));
        }
    }

    /**
     * Build and return a database row that contains
     * a foreign key for the specified reference object.
     * This will be stored in the nested row(s).
     */
    protected XMLRecord extractKeyRowFromReferenceObject(Object object, AbstractSession session, XMLRecord parentRecord) {
        Element newNode = XPathEngine.getInstance().createUnownedElement(parentRecord.getDOM(), getForeignKeyGroupingElement());
        XMLRecord result = new DOMRecord(newNode);

        for (int i = 0; i < this.getSourceForeignKeyFields().size(); i++) {
            DatabaseField fkField = (DatabaseField)getSourceForeignKeyFields().get(i);
            if (object == null) {
                result.add(fkField, null);
            } else {
                DatabaseField pkField = (DatabaseField)this.getSourceForeignKeysToTargetKeys().get(fkField);
                Object value = this.getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(object, pkField, session);
                result.add(fkField, value);
            }
        }
        return result;

    }

    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ReadQuery targetQuery = getSelectionQuery();
        if (!isForeignKeyRelationship) {
            // if the source query is cascading then the target query must use the same settings
            if (targetQuery.isObjectLevelReadQuery() && (sourceQuery.shouldCascadeAllParts() || (sourceQuery.shouldCascadePrivateParts() && isPrivateOwned()) || (sourceQuery.shouldCascadeByMapping() && this.cascadeRefresh))) {
                targetQuery = (ObjectLevelReadQuery)targetQuery.clone();
                ((ObjectLevelReadQuery)targetQuery).setShouldRefreshIdentityMapResult(sourceQuery.shouldRefreshIdentityMapResult());
                targetQuery.setCascadePolicy(sourceQuery.getCascadePolicy());
                //CR #4365
                targetQuery.setQueryId(sourceQuery.getQueryId());
                // For queries that have turned caching off, such as aggregate collection, leave it off.
                if (targetQuery.shouldMaintainCache()) {
                    targetQuery.setShouldMaintainCache(sourceQuery.shouldMaintainCache());
                }
            }

            return getIndirectionPolicy().valueFromQuery(targetQuery, row, sourceQuery.getSession());
        } else {
            if (getIndirectionPolicy().usesIndirection()) {
                EISOneToManyQueryBasedValueHolder valueholder = new EISOneToManyQueryBasedValueHolder(this, targetQuery, row, sourceQuery.getSession());
                return valueholder;
            } else {
                Vector subRows = getForeignKeyRows(row);

                if (subRows == null) {
                    return null;
                }

                ContainerPolicy cp = this.getContainerPolicy();
                Object results = cp.containerInstance(subRows.size());

                for (int i = 0; i < subRows.size(); i++) {
                    XMLRecord subRow = (XMLRecord)subRows.elementAt(i);

                    Object object = getIndirectionPolicy().valueFromQuery(targetQuery, subRow, sourceQuery.getSession());
                    if (object instanceof Collection) {
                        java.util.Iterator iter = ((Collection)object).iterator();
                        while (iter.hasNext()) {
                            cp.addInto(iter.next(), results, executionSession);
                        }
                    } else if (object instanceof java.util.Map) {
                        java.util.Iterator iter = ((java.util.Map)object).values().iterator();
                        while (iter.hasNext()) {
                            cp.addInto(iter.next(), results, executionSession);
                        }
                    } else {
                        cp.addInto(object, results, executionSession);
                    }
                }
                if (cp.sizeFor(results) == 0) {
                    return null;
                }
                return results;
            }
        }
    }

    /**
     * INTERNAL:
     */
    public Vector getForeignKeyRows(AbstractRecord row) {
        Vector subRows = new Vector();
        if (getForeignKeyGroupingElement() == null) {
            if (this.getSourceForeignKeyFields().size() > 0) {
                Object values = row.getValues((DatabaseField)this.getSourceForeignKeyFields().get(0));

                if (values != null) {
                    if (values instanceof Vector) {
                        int valuesSize = ((Vector)values).size();
                        for (int j = 0; j < valuesSize; j++) {
                            XMLRecord newRecord = new DOMRecord("test");
                            newRecord.put(this.getSourceForeignKeyFields().get(0), ((Vector)values).get(j));
                            subRows.add(newRecord);
                        }
                    } else {
                        XMLRecord newRecord = new DOMRecord("test");
                        newRecord.put(getSourceForeignKeyFields().get(0), values);
                        subRows.add(newRecord);
                    }
                }
            }
        } else {
            subRows = (Vector)row.getValues(getForeignKeyGroupingElement());
        }
        return subRows;
    }

    /**
     * INTERNAL:
     * Get the appropriate attribute value from the object
     * and put it in the appropriate field of the database row.
     * Loop through the reference objects and extract the
     * primary keys and put them in the vector of "nested" rows.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        if (!isForeignKeyRelationship) {
            return;
        }

        if (((getSourceForeignKeysToTargetKeys()) == null) || (getSourceForeignKeysToTargetKeys().size() == 0)) {
            return;
        }

        if (this.isReadOnly()) {
            return;
        }

        AbstractRecord referenceRow = this.getIndirectionPolicy().extractReferenceRow(this.getAttributeValueFromObject(object));
        if (referenceRow != null) {
            // the reference objects have not been instantiated - use the value from the original row				
            if (getForeignKeyGroupingElement() != null) {
                row.put(this.getForeignKeyGroupingElement(), referenceRow.getValues(this.getForeignKeyGroupingElement()));
            } else if (getSourceForeignKeyFields().size() > 0) {
                DatabaseField foreignKeyField = (DatabaseField)getSourceForeignKeyFields().get(0);
                row.put(foreignKeyField, referenceRow.getValues(foreignKeyField));
            }
            return;
        }

        ContainerPolicy cp = this.getContainerPolicy();

        // extract the keys from the objects
        Object attributeValue = this.getRealCollectionAttributeValueFromObject(object, session);
        Vector nestedRows = new Vector(cp.sizeFor(attributeValue));

        if (getForeignKeyGroupingElement() != null) {
            for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
                XMLRecord nestedRow = this.extractKeyRowFromReferenceObject(cp.next(iter, session), session, (XMLRecord)row);
                nestedRows.addElement(nestedRow);
            }
            row.add(this.getForeignKeyGroupingElement(), nestedRows);
        } else {
            DatabaseField singleField = (DatabaseField)getSourceForeignKeyFields().get(0);
            DatabaseField pkField = (DatabaseField)getSourceForeignKeysToTargetKeys().get(singleField);
            for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
                Object singleValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(cp.next(iter, session), pkField, session);
                row.add(singleField, singleValue);
            }
        }
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     * The foreign keys must be set to null to avoid constraints.
     */
    public void writeFromObjectIntoRowForShallowInsert(Object object, AbstractRecord row, AbstractSession session) {
        if (isForeignKeyRelationship() && !isReadOnly()) {
            if (getForeignKeyGroupingElement() != null) {
                row.put(getForeignKeyGroupingElement(), null);
            } else if (this.getSourceForeignKeyFields().size() > 0) {
                row.put(getSourceForeignKeyFields().get(0), null);
            }
        } else {
            super.writeFromObjectIntoRowForShallowInsert(object, row, session);
        }
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     * The foreign keys must be set to null to avoid constraints.
     */
    public void writeFromObjectIntoRowForShallowInsertWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        if (isForeignKeyRelationship() && !isReadOnly()) {
            if (getForeignKeyGroupingElement() != null) {
                row.put(getForeignKeyGroupingElement(), null);
            } else if (this.getSourceForeignKeyFields().size() > 0) {
                row.put(getSourceForeignKeyFields().get(0), null);
            }
        } else {
            super.writeFromObjectIntoRowForShallowInsertWithChangeRecord(changeRecord, row, session);
        }
    }

    /**
     * INTERNAL:
     * If any of the references objects has changed, write out
     * all the keys.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery writeQuery, AbstractRecord row) throws DescriptorException {
        if (!this.isAttributeValueInstantiatedOrChanged(writeQuery.getObject())) {
            return;
        }

        AbstractSession session = writeQuery.getSession();

        if (session.isUnitOfWork()) {
            // PRS2074 fix for "traditional" Indirection
            Object collection1 = this.getRealCollectionAttributeValueFromObject(writeQuery.getObject(), session);
            Object collection2 = this.getRealCollectionAttributeValueFromObject(writeQuery.getBackupClone(), session);
            if (this.compareObjectsWithoutPrivateOwned(collection1, collection2, session)) {
                return;// nothing has changed - don't put anything in the row
            }
        }
        this.writeFromObjectIntoRow(writeQuery.getObject(), row, session);

    }

    /**
     * INTERNAL:
     * Get the appropriate attribute value from the object
     * and put it in the appropriate field of the database row.
     * Loop through the reference objects and extract the
     * primary keys and put them in the vector of "nested" rows.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        if (isForeignKeyRelationship()) {
            Object object = ((ObjectChangeSet)changeRecord.getOwner()).getUnitOfWorkClone();
            this.writeFromObjectIntoRow(object, row, session);
        } else {
            super.writeFromObjectIntoRowWithChangeRecord(changeRecord, row, session);
        }
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord row, AbstractSession session) {
        if (isForeignKeyRelationship() && !isReadOnly()) {
            if (getForeignKeyGroupingElement() != null) {
                row.put(getForeignKeyGroupingElement(), null);
            } else if (this.getSourceForeignKeyFields().size() > 0) {
                row.put(getSourceForeignKeyFields().get(0), null);
            }
        } else {
            super.writeInsertFieldsIntoRow(row, session);
        }
    }

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    protected Object executeBatchQueryForPessimisticLocking(DatabaseQuery query, UnitOfWorkImpl unitOfWork, AbstractRecord argumentRow) {
        throw DescriptorException.invalidMappingOperation(this, "executeBatchQueryForPessimisticLocking");
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
    public void setSelectionCriteria(Expression anExpression) {
        throw DescriptorException.invalidMappingOperation(this, "setSelectionCriteria");
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

    /**
     * INTERNAL:
     * This method is not supported in an EIS environment.
     */
    public void setDeleteAllSQLString(String sqlString) {
        throw DescriptorException.invalidMappingOperation(this, "setDeleteAllSQLString");
    }
}
