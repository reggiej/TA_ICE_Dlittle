// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sequencing;

import java.util.Vector;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Reference to the default sequence.
 * <p>
 * <b>Description</b>
 * This sequence can be used to provide a sequence using the session's
 * default sequencing mechanism but override the pre-allocation size.
 */
public class DefaultSequence extends Sequence {
    protected Sequence defaultSequence;

    public DefaultSequence() {
        super();
    }

    /**
     * Create a new sequence with the name.
     */
    public DefaultSequence(String name) {
        super(name, 0);
    }
    
    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public DefaultSequence(String name, int size) {
        super(name, size);
    }

    public DefaultSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }
    
    /**
     * INTERNAL:
     * Return the platform's default sequence.
     */
    public Sequence getDefaultSequence() {
        return getDatasourcePlatform().getDefaultSequence();
    }

    public boolean hasPreallocationSize() {
        return size != 0;
    }

    public int getPreallocationSize() {
        if ((size != 0) || (getDefaultSequence() == null)) {
            return size;
        } else {
            return getDefaultSequence().getPreallocationSize();
        }
    }

    public int getInitialValue() {
        if ((initialValue != 0) || (getDefaultSequence() == null)) {
            return initialValue;
        } else {
            return getDefaultSequence().getInitialValue();
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof DefaultSequence) {
            return equalNameAndSize(this, (DefaultSequence)obj);
        } else {
            return false;
        }
    }

    /**
    * INTERNAL:
    * Indicates whether sequencing value should be acquired after INSERT.
    * Note that preallocation could be used only in case sequencing values
    * should be acquired before insert (this method returns false).
    * In default implementation, it is true for table sequencing and native
    * sequencing on Oracle platform, false for native sequencing on other platforms.
    */
    public boolean shouldAcquireValueAfterInsert() {
        return getDefaultSequence().shouldAcquireValueAfterInsert();
    }

    /**
    * INTERNAL:
    * Indicates whether TopLink should internally call beginTransaction() before
    * getGeneratedValue/Vector, and commitTransaction after.
    * In default implementation, it is true for table sequencing and
    * false for native sequencing.
    */
    public boolean shouldUseTransaction() {
        return getDefaultSequence().shouldUseTransaction();
    }

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public boolean shouldOverrideExistingValue(String seqName, Object existingValue) {
        return getDefaultSequence().shouldOverrideExistingValue(seqName, existingValue);
    }

    /**
    * INTERNAL:
    * Return the newly-generated sequencing value.
    * Used only in case preallocation is not used (shouldUsePreallocation()==false).
    * Accessor may be non-null only in case shouldUseSeparateConnection()==true.
    * Even in this case accessor could be null - if SequencingControl().shouldUseSeparateConnection()==false;
    * Therefore in case shouldUseSeparateConnection()==true, implementation should handle
    * both cases: use a separate connection if provided (accessor != null), or get by
    * without it (accessor == null).
    * @param accessor Accessor is a separate sequencing accessor (may be null);
    * @param writeSession Session is a Session used for writing (either ClientSession or DatabaseSession);
    * @param seqName String is sequencing number field name
    */
    public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {
        return getDefaultSequence().getGeneratedValue(accessor, writeSession, seqName);
    }

    /**
    * INTERNAL:
    * Return a Vector of newly-generated sequencing values.
    * Used only in case preallocation is used (shouldUsePreallocation()==true).
    * Accessor may be non-null only in case shouldUseSeparateConnection()==true.
    * Even in this case accessor could be null - if SequencingControl().shouldUseSeparateConnection()==false;
    * Therefore in case shouldUseSeparateConnection()==true, implementation should handle
    * both cases: use a separate connection if provided (accessor != null), or get by
    * without it (accessor == null).
    * @param accessor Accessor is a separate sequencing accessor (may be null);
    * @param writeSession Session is a Session used for writing (either ClientSession or DatabaseSession);
    * @param seqName String is sequencing number field name
    * @param size int number of values to preallocate (output Vector size).
    */
    public Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession, String name, int size) {
        return getDefaultSequence().getGeneratedVector(accessor, writeSession, name, size);
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is created.
    * It's a chance to do initialization.
    * @param ownerSession DatabaseSession
    */
    protected void onConnect() {
        // nothing to do
    }

    /**
    * INTERNAL:
    * This method is called when Sequencing object is destroyed..
    * It's a chance to do deinitialization.
    */
    public void onDisconnect() {
        // nothing to do
    }
}
