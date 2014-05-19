// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.changetracking;

import java.beans.PropertyChangeEvent;

/**
 * <p>
 * <b>Purpose</b>: Define a change event for Map types.
 * <p>
 * <b>Description</b>: For any object that wishes to use either object change tracking or
 * attribute change tracking, its map attributes need to fire MapChangeEvent
 * in the put or remove methods.  In the case of a replace (ie key already exists) both
 * a remove for that key and a put using the new value and old key must be fired.
 * <p>
 * <b>Responsibilities</b>: Create a MapChangeEvent for an object
 * <ul>
 * </ul>
 */
public class MapChangeEvent extends CollectionChangeEvent {
    /**
     * INTERNAL:
     * Thie value of the key that was updated.
     */
    protected Object key;

    /**
     * PUBLIC:
     * Create a MapChangeEvent for an object based on the property name, the updated Map, the new Key and the new Value
     * and change type (add or remove)
     */
    public MapChangeEvent(Object collectionOwner, String propertyName, Object collectionChanged, Object elementKey, Object elementValue, int changeType) {
        super(collectionOwner, propertyName, collectionChanged, elementValue, changeType);
        this.key = elementKey;
    }

    /**
     * INTERNAL:
     * Return the change type
     */
    public Object getKey() {
        return key;
    }

    /**
     * INTERNAL:
     * Set the change type
     */
    public void setKey(Object key) {
        this.key = key;
    }
}