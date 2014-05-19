// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import oracle.toplink.ox.mappings.nullpolicy.AbstractNullPolicy;


/**
 * INTERNAL
 * All nillable mappings which can be added to oracle.toplink.ox.XMLDescriptor must
 * implement this interface.<br>
 * The default policy is AbstractNullPolicy.<br>
 *
 *@see oracle.toplink.ox.mappings
 */
public interface XMLNillableMapping {

    /**
     * Set the AbstractNullPolicy on the mapping<br>
     * The default policy is NullPolicy.<br>
     *
     * @param aNullPolicy
     */
    public void setNullPolicy(AbstractNullPolicy aNullPolicy);

    /**
     * Get the AbstractNullPolicy from the Mapping.<br>
     * The default policy is NullPolicy.<br>
     * @return
     */
    public AbstractNullPolicy getNullPolicy();
}