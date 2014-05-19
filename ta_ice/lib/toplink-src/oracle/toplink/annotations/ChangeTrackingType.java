// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

/** 
 * An enum that is used within the ChangeTracking annotation.
 * 
 * @see oracle.toplink.annotations.ChangeTracking.
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
public enum ChangeTrackingType {
    /**
     * An ATTRIBUTE change tracking type allows change tracking at the attribute 
     * level of an object. Objects with changed attributes will be processed in 
     * the commit process to include any changes in the results of the commit.
     * Unchanged objects will be ignored.
     */
    ATTRIBUTE,

    /**
     * An OBJECT change tracking policy allows an object to calculate for itself 
     * whether it has changed. Changed objects will be processed in the commit 
     * process to include any changes in the results of the commit.
     * Unchanged objects will be ignored.
     */
    OBJECT,

    /**
     * A DEFERRED change tracking policy defers all change detection to the 
     * UnitOfWork's change detection process. Essentially, the calculateChanges() 
     * method will run for all objects in a UnitOfWork. 
     * This is the default ObjectChangePolicy
     */
    DEFERRED,

    /**
     * Will not set any change tracking policy, and the change tracking will be
     * determined at runtime.
     */
    AUTO
}
