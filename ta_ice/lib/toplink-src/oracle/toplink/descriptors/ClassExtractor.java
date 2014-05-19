// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import oracle.toplink.exceptions.*;
import oracle.toplink.sessions.*;

/**
 * <p><b>Purpose</b>:
 * Abstract class to allow complex inheritance support.  Typically class indicators are used to define inheritance in the database,
 * however in complex cases the class type may be determined through another mechanism.
 * The class extractor must be able to determine and return the class type from the database row.
 *
 * @see oracle.toplink.descriptors.InheritancePolicy#setClassExtractor(ClassExtrator)
 */
public abstract class ClassExtractor {

    /**
     * Extract/compute the class from the database row and return the class.
     * Map is used as the public interface to database row, the key is the field name,
     * the value is the database value.
     */
    public abstract Class extractClassFromRow(Record databaseRow, Session session);

    /**
     * Allow for any initialization.
     */
    public void initialize(ClassDescriptor descriptor, Session session) throws DescriptorException {
        // Do nothing by default.
    }
}