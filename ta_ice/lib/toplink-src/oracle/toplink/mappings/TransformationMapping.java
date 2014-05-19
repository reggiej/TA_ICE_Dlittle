// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;

import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 * <p><b>Purpose</b>: A transformation mapping is used for a specialized translation between how
 * a value is represented in Java and its representation on the databae. Transformation mappings
 * should only be used when other mappings are inadequate.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class TransformationMapping extends AbstractTransformationMapping implements RelationalMapping {

    /**
     * PUBLIC:
     * Default constructor.
     */
    public TransformationMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }
}