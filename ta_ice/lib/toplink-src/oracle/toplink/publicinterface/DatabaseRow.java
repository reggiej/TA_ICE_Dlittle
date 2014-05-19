// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.publicinterface;

import java.io.*;
import java.util.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.sessions.DatabaseRecord;
import oracle.toplink.sessions.Record;

/**
 * <p>
 * <b>Purpose</b>: Define a representation of a database row as field=>value pairs.
 * <p>
 * <b>Responsibilities</b>: <ul>
 *        <li> Implement the common hashtable collection protocol.
 *        <li> Allow get and put on the field or field name.
 * </ul>
 * @see DatabaseField
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.sessions.DatabaseRecord}, and
 *         {@link oracle.toplink.sessions.Record}
 */
public class DatabaseRow extends DatabaseRecord implements Record, Cloneable, Serializable, Map {

    public DatabaseRow() {
        super();
    }

    /**
     * INTERNAL:
     * TopLink converts JDBC results to collections of rows.
     */
    public DatabaseRow(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * INTERNAL:
     * TopLink converts JDBC results to collections of rows.
     */
    public DatabaseRow(Vector fields, Vector values) {
        super(fields, values);
    }

}