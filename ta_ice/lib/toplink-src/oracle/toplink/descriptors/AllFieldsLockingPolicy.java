// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import java.util.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.queryframework.*;

/**
 * <p><b>Purpose</b>: An implementation of the OptimisticLockingPolicy interface.
 * This policy compares every field in the table in the WHERE clause
 * when doing an update or a delete.  If any field has been changed, an
 * optimistic locking exception will be thrown.<p>
 * NOTE: This policy can only be used inside a unit of work.
 *
 * @since TopLink 2.1
 * @author Peter Krogh
 */
public class AllFieldsLockingPolicy extends FieldsLockingPolicy {

    /**
     * PUBLIC:
     * Create a new all fields locking policy.
     * A field locking policy is based on locking on all fields by comparing with their previous values to detect field-level collisions.
     * Note: the unit of work must be used for all updates when using field locking.
     */
    public AllFieldsLockingPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Values to be included in the locking mechanism are added
     * to the translation row.  Set the translation row to all the original field values.
     */
    public void addLockValuesToTranslationRow(ObjectLevelModifyQuery query) {
        verifyUsage(query.getSession());
        query.setTranslationRow(descriptor.getObjectBuilder().buildRowForWhereClause(query));
    }

    /**
     * INTERNAL:
     * Returns the fields that should be compared in the where clause.
     * In this case, it is all the fields, except for the primary key
     * and class indicator.
     */
    protected Vector getFieldsToCompare(DatabaseTable table, AbstractRecord transRow, AbstractRecord modifyRow) {
        return getAllNonPrimaryKeyFields(table);
    }
}