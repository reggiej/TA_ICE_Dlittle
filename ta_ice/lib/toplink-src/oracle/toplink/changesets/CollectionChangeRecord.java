// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import oracle.toplink.internal.helper.IdentityHashtable;

/**
 * <p>
 * <b>Purpose</b>: This interface defines the API for the changeRecord that maintains the changes made to a collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: The CollectionChangeRecord stores a list of objects removed from the collection and a seperate list of objects
 * added to a collection
 */
public interface CollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method returns the IdentityHashtable that contains the added values to the collection
     * and their corresponding ChangeSets.
     * @return java.util.Vector
     */
    public IdentityHashtable getAddObjectList();

    /**
     * ADVANCED:
     * This method returns the IdentityHashtable that contains the removed values from the collection
     * and their corresponding ChangeSets.
     * @return java.util.Vector
     */
    public IdentityHashtable getRemoveObjectList();

    /**
     * ADVANCED:
     * This method returns true if the change set has changes
     * @return boolean
     */
    public boolean hasChanges();
}