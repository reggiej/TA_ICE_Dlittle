// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 * <p><b>Purpose</b>: Provides an empty implementation of FieldTransformer.
 * Users who do not require the full FieldTransformer API can subclass this class
 * and implement only the methods required.
 *  @see oracle.toplink.mappings.FieldTransformer
 *  @version $Header: FieldTransformerAdapter.java 11-jul-2006.10:33:44 gyorke Exp $
 *  @author  mmacivor
 *  @since   10
 */
public class FieldTransformerAdapter implements FieldTransformer {
    public void initialize(AbstractTransformationMapping mapping) {
    }

    public Object buildFieldValue(Object object, String fieldName, Session session) {
        return null;
    }
}