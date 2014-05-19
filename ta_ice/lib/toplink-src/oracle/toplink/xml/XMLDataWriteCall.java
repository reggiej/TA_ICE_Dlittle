// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * XMLDataWriteCall simply consolidates the behavior
 * common to XMLDataInsertCall and XMLDataUpdateCall.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public abstract class XMLDataWriteCall extends XMLDataCall {

    /**
     * Default constructor.
     */
    public XMLDataWriteCall() {
        super();
    }

    /**
     * Insert the necessary data. The translation row
     * holds the primary key for the data.
     * Return an write count.
     */
    public Object execute(AbstractRecord translationRow, Accessor accessor) throws XMLDataStoreException {
    	AbstractRecord row = this.convertToFullyQualifiedRow(translationRow);
        Writer stream = this.getWriteStream(accessor, this.getRootElementName(), row, this.getOrderedPrimaryKeyElements());
        row = (AbstractRecord)this.getFieldTranslator().translateForWrite(row);
        try {
            this.getXMLTranslator().write(stream, row);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw XMLDataStoreException.unableToCloseWriteStream(this, e);
            }
        }
        return new Integer(1);
    }

    /**
     * Return the appropriate write stream.
     */
    protected abstract Writer getWriteStream(Accessor accessor, String rootElementName, AbstractRecord translationRow, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;
}