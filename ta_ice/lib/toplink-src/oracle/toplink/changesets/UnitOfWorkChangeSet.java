// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;


/**
 * <p>
 * <b>Purpose</b>: To Provide API to the UnitOfWorkChangeSet.
 * <p>
 * <b>Description</b>:The UnitOfWorkChangeSet contains all of the individual ObjectChangeSets.  It is stored and used by the UnitOfWork
 * <p>
 */
public interface UnitOfWorkChangeSet {

    /**
     * ADVANCED:
     * This method returns a reference to the collection.  Not All ChangeSets that Exist in this list may have changes
     * @return oracle.toplink.internal.helper.IdentityHashtable
     */
    public oracle.toplink.internal.helper.IdentityHashtable getAllChangeSets();

    /**
     * ADVANCED:
     * This method returns the reference to the deleted objects from the changeSet
     * @return oracle.toplink.internal.helper.IdentityHashtable
     */
    public oracle.toplink.internal.helper.IdentityHashtable getDeletedObjects();

    /**
     * ADVANCED:
     * Get ChangeSet for a particular clone
     * @return oracle.toplink.changesets.ObjectChangeSet the changeSet that represents a particular clone
     */
    public ObjectChangeSet getObjectChangeSetForClone(Object clone);

    /**
     * ADVANCED:
     * This method returns the Clone for a particular changeSet
     * @return Object the clone represented by the changeSet
     */
    public Object getUOWCloneForObjectChangeSet(ObjectChangeSet changeSet);

    /**
     * ADVANCED:
     * Returns true if the Unit Of Work change Set has changes
     * @return boolean
     */
    public boolean hasChanges();
}