// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.localization.TraceLocalization;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * XMLDataUpdateCall simply writes the passed in database row
 * into the specified "table" at the specified "primary key",
 * overwriting the existing data.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLDataUpdateCall extends XMLDataWriteCall {

    /**
     * Default constructor.
     */
    public XMLDataUpdateCall() {
        super();
    }

    /**
     * Return the appropriate write stream.
     */
    protected Writer getWriteStream(Accessor accessor, String rootElementName, AbstractRecord translationRow, Vector orderedPrimaryKeyElements) throws XMLDataStoreException {
        return this.getStreamPolicy().getExistingWriteStream(rootElementName, translationRow, orderedPrimaryKeyElements, accessor);
    }

    /**
     * Append a string describing the call to the specified writer.
     */
    protected void writeLogDescription(PrintWriter writer) {
        writer.write(TraceLocalization.buildMessage("XML_data_update", (Object[])null));
    }
}