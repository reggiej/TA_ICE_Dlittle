// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;


/**
 * <p>
 * <b>Purpose</b>: Define the base Change Record API.
 * <p>
 * <b>Description</b>: This interface is meant to clarify the public protocol into TopLink.
 * It provides access into the information available from the TopLink Change Set
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the API for ChangeRecord.
 * </ul>
 */
public interface ChangeRecord {

    /**
     * ADVANCED:
     * Returns the name of the attribute this ChangeRecord Represents
     * @return java.lang.String
     */
    public String getAttribute();

    /**
     * ADVANCED:
     * This method returns the ObjectChangeSet that references this ChangeRecord
     * @return oracle.toplink.changesets.ObjectChangeSet
     */
    public ObjectChangeSet getOwner();
}