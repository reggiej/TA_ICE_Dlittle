// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import java.util.Vector;
import oracle.toplink.sessions.Session;

/**
 * <p>
 * <b>Purpose</b>: Provides API to the Class that holds all changes made to a particular Object.
 * <p>
 * <b>Description</b>: The ObjectChangeSet class represents a single Object registered in the UnitOfWork.
 * It is owned by the larger UnitOfWorkChangeSet.
 * <p>
 */
public interface ObjectChangeSet {
    boolean equals(ObjectChangeSet objectChange);

    /**
     * ADVANCED:
     * This method will return a collection of the fieldnames of attributes changed in an object.
     */
    Vector getChangedAttributeNames();

    /**
     * ADVANCED:
     * This method returns a reference to the collection of changes within this changeSet.
     */
    Vector getChanges();

    /**
     * ADVANCE:
     * This method returns the class type that this changeSet Represents.
     */
    Class getClassType(Session session);

    /**
     * ADVANCE:
     * This method returns the class Name that this changeSet Represents.
     */
    String getClassName();

    /**
     * ADVANCED:
     * This method returns the key value that this object was stored under in it's respective Map.
     * This is old relevant for collection mappings that use a Map.
     */
    Object getOldKey();

    /**
     * ADVANCED:
     * This method returns the key value that this object will be stored under in it's respective Map.
     * This is old relevant for collection mappings that use a Map.
     */
    Object getNewKey();

    /**
     * ADVANCED:
     * This method returns the primary keys for the object that this change set represents.
     */
    Vector getPrimaryKeys();

    /**
     * ADVANCED:
     * This method is used to return the parent ChangeSet.
     */
    UnitOfWorkChangeSet getUOWChangeSet();

    /**
     * ADVANCED:
     * This method is used to return the lock value of the object this changeSet represents.
     */
    Object getWriteLockValue();
    
    /**
     * ADVANCED:
     * Returns the change record for the specified attribute name.
     */
    ChangeRecord getChangesForAttributeNamed(String attributeName);
    
    /**
     * ADVANCED:
     * This method will return true if the specified attributue has been changed..
     * @param String the name of the attribute to search for.
     */
    boolean hasChangeFor(String attributeName);

    /**
     * ADVANCED:
     * Returns true if this particular changeSet has changes.
     */
    boolean hasChanges();

    /**
     * ADVANCED:
     * Returns true if this ObjectChangeSet represents a new object.
     */
    boolean isNew();
}