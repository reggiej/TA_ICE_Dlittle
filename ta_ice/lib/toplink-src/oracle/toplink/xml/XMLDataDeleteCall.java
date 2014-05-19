// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.io.*;
import oracle.toplink.sessions.Record;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.localization.TraceLocalization;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * XMLDataDeleteCall simply deletes the appropriate
 * XML document.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLDataDeleteCall extends XMLDataCall {

    /**
     * Default constructor.
     */
    public XMLDataDeleteCall() {
        super();
    }

    /**
     * Delete the appropriate data. The translation row
     * holds the primary key for the data.
     * Return a delete count.
     */
    public Object execute(AbstractRecord translationRow, Accessor accessor) throws XMLDataStoreException {
        Record row = this.convertToFullyQualifiedRow(translationRow);
        return this.getStreamPolicy().deleteStream(this.getRootElementName(), translationRow, this.getOrderedPrimaryKeyElements(), accessor);
    }

    /**
     * Append a string describing the call to the specified writer.
     */
    protected void writeLogDescription(PrintWriter writer) {
        writer.write(TraceLocalization.buildMessage("XML_data_delete", (Object[])null));
    }
}