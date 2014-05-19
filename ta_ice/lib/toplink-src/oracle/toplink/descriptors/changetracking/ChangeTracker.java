// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors.changetracking;

import java.beans.PropertyChangeListener;

/**
 * <p>
 * <b>Purpose</b>: Define an interface for any object that wishes to use attribute change tracking.
 * <p>
 * <b>Description</b>: Build a bridge between an object and its PropertyChangeListener.
 * <p>
 */
public interface ChangeTracker {

    /**
     * PUBLIC:
     * Return the PropertyChangeListener for the object.
     */
    public PropertyChangeListener _persistence_getPropertyChangeListener();

    /**
     * PUBLIC:
     * Set the PropertyChangeListener for the object.
     */
    public void _persistence_setPropertyChangeListener(PropertyChangeListener listener);
}