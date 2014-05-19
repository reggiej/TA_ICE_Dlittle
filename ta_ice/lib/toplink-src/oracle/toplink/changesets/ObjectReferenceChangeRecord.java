// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;


/**
 * <p>
 * <b>Purpose</b>: Provides API for the ObjectReferenceChangeRecord.
 * <p>
 * <b>Description</b>: This Interface represents changes made in a one to one mapping and other single object reference mappings.
 * <p>
 */
public interface ObjectReferenceChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * Returns the new reference for this object
     * @return oracle.toplink.changesets.ObjectChangeSet
     */
    public ObjectChangeSet getNewValue();
}