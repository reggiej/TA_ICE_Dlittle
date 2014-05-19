// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import java.util.*;
import oracle.toplink.mappings.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.queryframework.*;
import oracle.toplink.expressions.*;

/**
 * <p><b>Purpose</b>: An implementation of the OptimisticLockingPolicy interface.
 * This policy compares only the changed fields in the WHERE clause
 * when doing an update.  If any field has been changed, an optimistic
 * locking exception will be thrown.  A delete will only compare
 * the primary key.  <p>
 * NOTE: This policy can only be used inside a unit of work.
 *
 * @since TopLink 2.1
 * @author Peter Krogh
 */
public class ChangedFieldsLockingPolicy extends FieldsLockingPolicy {

    /**
     * PUBLIC:
     * Create a new changed fields locking policy.
     * This locking policy is based on locking on all changed fields by comparing with
     * their previous values to detect field-level collisions.
     *
     * Note: the unit of work must be used for all updates when using field locking. Without
     * a unit of work, there is no way for TopLink to know what the original values were
     * without the back up clone in the unit of work.
     */
    public ChangedFieldsLockingPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Values to be included in the locking mechanism are added to the translation row.
     * For changed fields the normal build row is ok as only changed fields matter.
     */
    public void addLockValuesToTranslationRow(ObjectLevelModifyQuery query) {
        verifyUsage(query.getSession());
        Object object;
        if (query.isDeleteObjectQuery()) {
            return;
        }
        object = query.getBackupClone();
        for (Enumeration enumtr = query.getModifyRow().keys(); enumtr.hasMoreElements();) {
            DatabaseField field = (DatabaseField)enumtr.nextElement();
            DatabaseMapping mapping = descriptor.getObjectBuilder().getMappingForField(field);
            mapping.writeFromObjectIntoRow(object, query.getTranslationRow(), query.getSession());
        }
    }

    /**
     * INTERNAL:
     * When given an expression, this method will return a new expression with the optimistic
     * locking values included.  The values are taken from the passed in database row.
     * This expression will be used in a delete call.
     * No new criteria will be added for changed fields.
     */
    public Expression buildDeleteExpression(DatabaseTable table, Expression mainExpression, AbstractRecord row) {
        return mainExpression;
    }

    /**
     * INTERNAL:
     * Returns the fields that should be compared in the where clause.
     * In this case, it is only the fields that were changed.
     */
    protected Vector getFieldsToCompare(DatabaseTable table, AbstractRecord transRow, AbstractRecord modifyRow) {
        Vector fields = getAllNonPrimaryKeyFields(table);
        Vector returnedFields = new Vector();
        for (Enumeration enumtr = fields.elements(); enumtr.hasMoreElements();) {
            DatabaseField field = (DatabaseField)enumtr.nextElement();
            if (modifyRow.containsKey(field)) {
                returnedFields.addElement(field);
            }
        }
        return returnedFields;
    }
}