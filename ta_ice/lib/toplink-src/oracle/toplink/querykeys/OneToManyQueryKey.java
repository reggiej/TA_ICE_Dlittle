// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.querykeys;


/**
 * <p>
 * <b>Purpose</b>:Represents a 1-m join query.
 */
public class OneToManyQueryKey extends ForeignReferenceQueryKey {

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
     * override the isOneToManyQueryKey() method in the superclass to return true.
     * @return boolean
     */
    public boolean isOneToManyQueryKey() {
        return true;
    }
}