// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.localization.TraceLocalization;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.sessions.Record;

/**
 * This class checks the XML data store for the existence of the
 * XML document.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLDoesExistCall extends XMLCall {

    /**
     * Default constructor.
     */
    public XMLDoesExistCall() {
        super();
    }

    /**
     * If the data exists, return the row, otherwise return null.
     */
    public Object execute(AbstractRecord translationRow, Accessor accessor) throws XMLDataStoreException {
        Reader stream = this.getStreamPolicy().getExistenceCheckStream(this.getRootElementName(), translationRow, this.getOrderedPrimaryKeyElements(), accessor);
        if (stream == null) {
            return null;
        }

        Record row = this.getXMLTranslator().read(stream);
        return this.getFieldTranslator().translateForRead(row);
    }

    /**
     * Append a string describing the call to the specified writer.
     */
    protected void writeLogDescription(PrintWriter writer) {
        writer.write(TraceLocalization.buildMessage("XML_existence_check", (Object[])null));
    }
}