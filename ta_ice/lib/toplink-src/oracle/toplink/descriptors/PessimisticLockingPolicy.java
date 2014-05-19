// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.exceptions.ValidationException;

/**
 * <p> <b>Description</b>: This policy is used to configure bean level pessimistic locking feautre.
 * It is set on the CMPPolicy instance of the ClassDescriptor
 *
 * Note that bean is not pessimistic locked in the following scenarios:
 * <ul>
 * <li> No presence of a JTA transaction
 * <li> The current transaction is created and started by the Container for the invoking entity bean's method only. (i.e. invoke a business method without a client transaction)
 * <li> The bean has already been pessimistic locked in the current transaction
 * <li> Execution of ejbSelect
 * <li> Traversing relationship does not lock the returned result.
 * </ul>
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Configure locking modes of WAIT or NO_WAIT
 * <li> Provide utitity to configure an ObjectLevelReadQuery with pessimstic locking based on this policy.
 * </ul>
 *
 * @see oracle.toplink.descriptors.CMPPolicy
 *
 * @since TopLink 10.1.3
 */
public class PessimisticLockingPolicy implements Cloneable, java.io.Serializable {
    protected short lockingMode;

    public PessimisticLockingPolicy() {
        lockingMode = ObjectLevelReadQuery.LOCK;
    }

    /**
     * PUBLIC:
     * Return locking mode.  Default locking mode is ObjectLevelReadQuery.LOCK.
     * @return short locking mode value of ObjectLevelReadQuery.LOCK or ObjectLevelReadQuery.LOCK_NOWAIT
     */
    public short getLockingMode() {
        return lockingMode;
    }

    /**
     * PUBLIC:
     * Set locking mode.  If the mode is not a valid value, the locking mode is unchanged.
     * @param short mode must be value of ObjectLevelReadQuery.LOCK or ObjectLevelReadQuery.LOCK_NOWAIT
     */
    public void setLockingMode(short mode) {
        if ((mode == ObjectLevelReadQuery.LOCK) || (mode == ObjectLevelReadQuery.LOCK_NOWAIT)) {
            lockingMode = mode;
        } else {
            throw ValidationException.invalidMethodArguments();
        }
    }

    /**
     * INTERNAL:
     * Clone the policy
     */
    public Object clone() {
        PessimisticLockingPolicy clone = new PessimisticLockingPolicy();
        clone.setLockingMode(this.lockingMode);
        return clone;
    }
}