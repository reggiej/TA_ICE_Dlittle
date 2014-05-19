// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.io.*;

import oracle.toplink.sessions.Record;

/**
 * This interface defines the protocol from translating an XML document
 * to a database row and vice versa.
 *
 * @author Les Davis
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public interface XMLTranslator extends Serializable {

    /**
     * Build a database row from the XML document contained
     * in the specified stream.
     */
    Record read(Reader stream);

    /**
     * Write an XML document representing the specified database
     * row on the specified stream.
     */
    void write(Writer stream, Record row);
}