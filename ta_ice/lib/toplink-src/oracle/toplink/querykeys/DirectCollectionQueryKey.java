// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.querykeys;


/**
 * <p>
 * <b>Purpose</b>:Represents a direct collection join query.
 */
public class DirectCollectionQueryKey extends ForeignReferenceQueryKey {

    /**
     * INTERNAL:
     * override the isCollectionQueryKey() method in the superclass to return true.
     * @return boolean
     */
    public boolean isCollectionQueryKey() {
        return true;
    }

    /**
     * INTERNAL:
     * override the isDirectCollectionQueryKey() method in the superclass to return true.
     * @return boolean
     */
    public boolean isDirectCollectionQueryKey() {
        return true;
    }
}