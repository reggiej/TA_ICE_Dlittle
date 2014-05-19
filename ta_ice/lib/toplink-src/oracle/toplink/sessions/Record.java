// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.Map;

/**
 * This interface defines the public interface for the TopLink DatabaseRecord (was Record),
 * and the other record types XMLRecord, EISRecord.
 * The Map API should be used to access the record data.
 * The data is keyed on the field name, or XPath, and the value is the field data value.
 * @author  mmacivor
 * @since   10.1.3
 */
public interface Record extends Map {
    // Uses Map API.
}