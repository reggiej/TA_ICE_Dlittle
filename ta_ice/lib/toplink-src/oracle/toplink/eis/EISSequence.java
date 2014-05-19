// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import oracle.toplink.sequencing.QuerySequence;

/**
 * <p>The <code>EISSequence</code> class allows access to sequence resources 
 * using custom read (ValueReadQuery) and update (DataModifyQuery) queries and a 
 * user specified preallocation size.  This allows sequencing to be performed 
 * using stored procedures, and access to sequence resources that are not 
 * supported by the other sequencing types provided by TopLink.
 * 
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class EISSequence extends QuerySequence {
    public EISSequence() {
        super();
        setShouldSelectBeforeUpdate(true);
    }

    public EISSequence(String name) {
        super(name);
        setShouldSelectBeforeUpdate(true);
    }

    public EISSequence(String name, int size) {
        super(name, size);
        setShouldSelectBeforeUpdate(true);
    }

    public boolean equals(Object obj) {
        if (obj instanceof EISSequence) {
            return equalNameAndSize(this, (EISSequence)obj);
        } else {
            return false;
        }
    }
}