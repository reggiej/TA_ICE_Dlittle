// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

/** 
 * An enum that is used within the Cache annotation.
 * 
 * @see oracle.toplink.annotations.Cache
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
public enum CacheCoordinationType {
    /**
     * Sends a list of changed objects including data about the changes. 
     * This data is merged into the receiving cache.
     */
    SEND_OBJECT_CHANGES,

    /**
     * Sends a list of the identities of the objects that have changed. The 
     * receiving cache invalidates the objects (rather than changing any of the 
     * data)
     */
    INVALIDATE_CHANGED_OBJECTS,

    /**
     * Same as SEND_OBJECT_CHANGES except it also includes any newly created 
     * objects from the transaction.
     */
    SEND_NEW_OBJECTS_WITH_CHANGES,

    /**
     * Does no cache coordination.
     */
    NONE
}
