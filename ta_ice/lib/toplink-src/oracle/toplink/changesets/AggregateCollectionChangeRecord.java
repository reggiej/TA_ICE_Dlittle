// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: Define the Public interface for the Aggregate Collection Change Record.
 * <p>
 * <b>Description</b>: This interface is meant to clarify the public protocol into TopLink.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * </ul>
 */
public interface AggregateCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * Return the values representing the changed AggregateCollection.
     * @return java.util.Vector
     */
    public Vector getChangedValues();
}