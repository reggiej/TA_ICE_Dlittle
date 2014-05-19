// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import oracle.toplink.sessions.Record;
import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 * <p><b>Purpose</b>: Provides an empty implementation of AttributeTransformer.
 * Users who do not require the full AttributeTransformer API can subclass this class
 * and implement only the methods required.
 *  @see oracle.toplink.mappings.AttributeTransformer
 *  @version $Header: AttributeTransformerAdapter.java 11-jul-2006.10:33:44 gyorke Exp $
 *  @author  mmacivor
 *  @since   10
 */
public class AttributeTransformerAdapter implements AttributeTransformer {
    public void initialize(AbstractTransformationMapping mapping) {
    }

    public Object buildAttributeValue(Record record, Object object, Session session) {
        return null;
    }
}