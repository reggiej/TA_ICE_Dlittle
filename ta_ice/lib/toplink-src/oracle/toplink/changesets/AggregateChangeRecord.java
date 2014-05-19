// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

/**
 * <p>
 * <b>Purpose</b>: Define the Public interface for the Aggregate Change Record.
 * <p>
 * <b>Description</b>: This interface is meant to clarify the public protocol into TopLink.
 */
public interface AggregateChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method is used to return the ObjectChangeSet representing the changed Aggregate.
     * @return oracle.toplink.CahngeSets.ObjectChanges
     */
    public ObjectChangeSet getChangedObject();
}