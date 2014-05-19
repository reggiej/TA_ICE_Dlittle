// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import java.io.*;
import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 * PUBLIC:
 * This interface is used by the Transformation Mapping to build the value for a
 * specific field. The user must provide implementations of this interface to the
 * Transformation Mapping.
 * @author  mmacivor
 * @since   10.1.3
 */
public interface FieldTransformer extends Serializable {

    /**
     * Initialize this transformer. Only required if the user needs some special
     * information from the mapping in order to do the transformation
     * @param mapping - the mapping this transformer is associated with.
     */
    public void initialize(AbstractTransformationMapping mapping);

    /**
     * @param instance - an instance of the domain class which contains the attribute
     * @param session - the current session
     * @param fieldName - the name of the field being transformed. Used if the user wants to use this transformer for multiple fields.
     * @return - The value to be written for the field associated with this transformer
     */
    public Object buildFieldValue(Object instance, String fieldName, Session session);
}