// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: To provide API into the EISCollectionChangeSet.
 * <p>
 * <b>Description</b>: Capture the changes for an unordered collection as
 * collections of adds and removes.
 * <p>
 */
public interface EISCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * Return the objects added to the collection.
     * The contents of this collection is determined by the mapping that
     * populated it
     */
    public Vector getAdds();

    /**
     * <p>
     * ADVANCED:
     * Return the objets whose Map keys have changed.
     * The contents of this collection is determined by the mapping that
     * populated it
     */
    public Vector getChangedMapKeys();

    /**
     * ADVANCED:
     * Return the removed objects.
     * The contents of this collection is determined by the mapping that
     * populated it
     */
    public Vector getRemoves();

    /**
     * ADVANCED:
     * Return whether any changes have been recorded with the change record.
     */
    public boolean hasChanges();
}