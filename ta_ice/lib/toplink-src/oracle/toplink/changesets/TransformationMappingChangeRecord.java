// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.changesets;

import oracle.toplink.sessions.Record;

/**
 * <p>
 * <b>Purpose</b>: To Provide API to the TransformationMappingChangeRecord.
 * <p>
 * <b>Description</b>: This changeRecord stores the particular database row that was changed in the mapping.
 * <p>
 */
public interface TransformationMappingChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method is used to access the changes of the fields in a transformation mapping.
     * @return oracle.toplink.sessions.Record
     */
    public Record getRecord();
}