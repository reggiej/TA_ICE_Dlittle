// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import java.io.*;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.Record;
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 * PUBLIC:
 *  @version $Header: AttributeTransformer.java 11-jul-2006.10:33:44 gyorke Exp $
 *  @author  mmacivor
 *  @since   10
 *  This interface is used by the Transformation Mapping to build the value for a
 *  the mapped attribute on a read. The user must provide an implementation of this interface to the
 *  Transformation Mapping.
 */
public interface AttributeTransformer extends Serializable {

    /**
     * @param mapping - The mapping associated with this transformer. Only used if some special information is required.
     */
    public void initialize(AbstractTransformationMapping mapping);

    /**
     * @param record - The metadata being used to build the object.
     * @param session - the current session
     * @param object - The current object that the attribute is being built for.
     * @return - The attribute value to be built into the object containing this mapping.
     */
    public Object buildAttributeValue(Record record, Object object, Session session);
}