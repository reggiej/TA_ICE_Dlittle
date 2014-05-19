// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.changetracking;

import java.beans.PropertyChangeEvent;

/**
 * <p>
 * <b>Purpose</b>: Define a change event for collection types.
 * <p>
 * <b>Description</b>: For any object that wishes to use either object change tracking or
 * attribute change tracking, its collection attributes need to fire CollectionChangeEvent
 * in the add or remove methods.
 * <p>
 * <b>Responsibilities</b>: Create a CollectionChangeEvent for an object
 * <ul>
 * </ul>
 */
public class CollectionChangeEvent extends PropertyChangeEvent {
    public static int ADD = 0;
    public static int REMOVE = 1;

    /**
     * INTERNAL:
     * Change type is either add or remove
     */
    protected int changeType;

    /**
     * PUBLIC:
     * Create a CollectionChangeEvent for an object based on the property name, old value, new value
     * and change type (add or remove)
     */
    public CollectionChangeEvent(Object collectionOwner, String propertyName, Object collectionChanged, Object elementChanged, int changeType) {
        super(collectionOwner, propertyName, collectionChanged, elementChanged);
        this.changeType = changeType;
    }

    /**
     * INTERNAL:
     * Return the change type
     */
    public int getChangeType() {
        return changeType;
    }

    /**
     * INTERNAL:
     * Set the change type
     */
    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }
}