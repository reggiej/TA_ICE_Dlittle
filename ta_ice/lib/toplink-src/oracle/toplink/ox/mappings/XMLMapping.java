// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.ox.record.XMLRecord;
/**
 * INTERNAL
 * All mappings which can be added to oracle.toplink.ox.XMLDescriptor must
 * implement this interface.
 *
 *@see oracle.toplink.ox.mappings
 */
public interface XMLMapping {
    
    /**
     * INTERNAL:
     * A method that marshals a single value to the provided Record based on this mapping's
     * XPath. Used for Sequenced marshalling.
     * @param value - The value to be marshalled
     * @param record - The Record the value is being marshalled too. 
     */
    public void writeSingleValue(Object value, Object parent, XMLRecord record, AbstractSession session);
}