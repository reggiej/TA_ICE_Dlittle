// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;


/**
 * <p>
 * <b>Purpose</b>: This interface defines the API for the ChangeRecord that holds the changes made to a direct collection attribute of
 * an object.
 * <p>
 * <b>Description</b>: Collections are compared to each other and added and removed objects are
 * recorded seperately
 */
public interface DirectCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method returns the collection of Primitive Objects that were added to the collection.
     * @return java.util.Vector
     */
    public java.util.Vector getAddObjectList();

    /**
     * ADVANCED:
     * This method returns the collection of Primitive Objects that were removed to the collection.
     * @return java.util.Vector
     */
    public java.util.Vector getRemoveObjectList();
}