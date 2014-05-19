// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.changetracking;

/**
 * <p>
 * <b>Purpose</b>: Define an interface for any collection that wishes to use attribute change track.
 * <p>
 * <b>Description</b>: Build a bridge between an object and its PropertyChangeListener.  Which will be
 * The listener of the parent object.
 * <p>
 */
public interface CollectionChangeTracker extends ChangeTracker{

    /**
     * PUBLIC:
     * Return the Attribute name this collection is mapped under.
     */
    public String getTopLinkAttributeName();

    /**
     * PUBLIC:
     * Set the Attribute name this collection is mapped under.
     */
    public void setTopLinkAttributeName(String attributeName);
}