// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.helper.Helper;

/**
 * <p>
 * <code>SDKSequence</code>
 * builds the queries for using sequence numbers.
 * Subclasses will need to override the appropriate methods that build
 * the platform-specific <code>Call</code>s.
 * <p>
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKPlatform extends DatasourcePlatform {

    /**
     * Default constructor.
     * Initialize the new instance.
     */
    public SDKPlatform() {
        super();
        this.initialize();
    }

    /**
     * Initialize the instance.
     */
    protected void initialize() {
        // do nothing
    }

    /**
     * OBSOLETE:
     * Return the name of the field holding the sequence counter.
     * @deprecated use ((SDKSequence)getDefaultSequence()).getCounterFieldName() instead
     */
    public String getSequenceCounterFieldName() {
        if (getDefaultSequence() instanceof SDKSequence) {
            return ((SDKSequence)getDefaultSequence()).getCounterFieldName();
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getCounterFieldName");
        }
    }

    /**
     * OBSOLETE:
     * Return the name of the field holding the sequence name.
     * @deprecated use ((SDKSequence)getDefaultSequence()).getNameFieldName() instead
     */
    public String getSequenceNameFieldName() {
        if (getDefaultSequence() instanceof SDKSequence) {
            return ((SDKSequence)getDefaultSequence()).getNameFieldName();
        } else {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "getNameFieldName");
        }
    }

    /**
     * OBSOLETE:
     * Set the name of the field holding the sequence counter.
     * @deprecated use ((SDKSequence)getDefaultSequence()).setCounterFieldName() instead
     */
    public void setSequenceCounterFieldName(String sequenceCounterFieldName) {
        if (getDefaultSequence() instanceof SDKSequence) {
            ((SDKSequence)getDefaultSequence()).setCounterFieldName(sequenceCounterFieldName);
        } else if (!sequenceCounterFieldName.equals((new SDKSequence()).getCounterFieldName())) {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "setCounterFieldName");
        }
    }

    /**
     * OBSOLETE:
     * Set the name of the field holding the sequence name.
     * @deprecated use ((SDKSequence)getDefaultSequence()).setNameFieldName() instead
     */
    public void setSequenceNameFieldName(String sequenceNameFieldName) {
        if (getDefaultSequence() instanceof SDKSequence) {
            ((SDKSequence)getDefaultSequence()).setNameFieldName(sequenceNameFieldName);
        } else if (!sequenceNameFieldName.equals((new SDKSequence()).getNameFieldName())) {
            throw ValidationException.wrongSequenceType(Helper.getShortClassName(getDefaultSequence()), "setNameFieldName");
        }
    }
}