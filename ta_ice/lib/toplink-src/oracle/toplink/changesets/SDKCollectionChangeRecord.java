// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: To provide API into the SDKCollectionChangeSet.
 * <p>
 * <b>Description</b>: Capture the changes for an unordered collection as
 * collections of adds and removes.
 * <p>
 * @see SDKAggregateCollectionMapping
 * @see SDKObjectCollectionMapping
 * @see SDKDirectCollectionMapping
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).
 */
public interface SDKCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * Return the objects added to the collection.
     * The contents of this collection is determined by the mapping that
     * populated it:
     * <ul>
     * <li>SDKAggregateCollectionMapping will store ObjectChangeSets
     * <li>SDKDirectCollectionMapping will store the direct elements themselves
     * <li>SDKObjectCollectionMapping will store the foreign keys
     * </ul>
     */
    public Vector getAdds();

    /**
     * <p>
     * ADVANCED:
     * Return the objets whose Map keys have changed.
     * The contents of this collection is determined by the mapping that
     * populated it:
     * </p>
     * <ul>
     * <li>SDKAggregateCollectionMapping will store ObjectChangeSets
     * <li>SDKDirectCollectionMapping will store the direct elements themselves
     * <li>SDKObjectCollectionMapping will store the foreign keys
     * </ul>
     */
    public Vector getChangedMapKeys();

    /**
     * ADVANCED:
     * Return the removed objects.
     * The contents of this collection is determined by the mapping that
     * populated it:<ul>
     * <li>SDKAggregateCollectionMapping will store ObjectChangeSets
     * <li>SDKDirectCollectionMapping will store the direct elements themselves
     * <li>SDKObjectCollectionMapping will store the foreign keys
     * </ul>
     */
    public Vector getRemoves();

    /**
     * ADVANCED:
     * Return whether any changes have been recorded with the change record.
     */
    public boolean hasChanges();
}