// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.annotations;

/** 
 * The IdValidation enum determines the type value that are valid for an Id.
 * By default null is not allowed, and 0 is not allow for singleton ids of long or int type.
 * The default value is ZERO for singleton ids, and NULL for composite ids.
 * This can only be currently set through the ClassDescriptor.setIdValidation() API using
 * a DescriptorCustomizer.
 * 
 * @see org.eclipse.persistence.descriptors.ClassDescriptor
 * @author James Sutherland
 */
public enum IdValidation {
    /**
     * Only null is not allowed as an id value, 0 is allowed.
     */
    NULL,

    /**
     * null and 0 are not allowed, (only int and long).
     */
    ZERO,

    /**
     * No id validation is done.
     */
    NONE
}
