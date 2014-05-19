// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.querykeys;

import oracle.toplink.expressions.*;

/**
 * <p>
 * <b>Purpose</b>: Define an alias to a foreign one to one object.
 * <p>
 */
public class OneToOneQueryKey extends ForeignReferenceQueryKey {
    // CR#2466 removed joinCriteria because it is already in ForeignReferenceQueryKey - TW

    /**
     * INTERNAL:
     * override the isOneToOneQueryKey() method in the superclass to return true.
     * @return boolean
     */
    public boolean isOneToOneQueryKey() {
        return true;
    }
}