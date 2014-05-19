// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import java.io.*;

import oracle.toplink.sessions.Record;


/**
 * This interface defines a mechanism for translating the field names in a
 * <code>Record</code> from those defined in the data store to
 * those expected by the appropriate <code>ClassDescriptor</code>(s)
 * and vice versa.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public interface FieldTranslator extends Serializable {

    /**
     * Translate and return the specified database row that was
     * read from the data store.
     */
	Record translateForRead(Record row);

    /**
     * Translate and return the specified database row that will
     * be written to the data store.
     */
	Record translateForWrite(Record row);
}