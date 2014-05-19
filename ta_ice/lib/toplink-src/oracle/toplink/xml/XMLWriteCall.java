// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * XMLWriteCall simply adds the assumption that the
 * query is a ModifyQuery.
 *
 * @see oracle.toplink.queryframework.ModifyQuery
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public abstract class XMLWriteCall extends XMLCall {

    /**
     * Default constructor.
     */
    public XMLWriteCall() {
        super();
    }

    /**
     * Write the necessary data. The translation row
     * holds the primary key for the data. But the modify row holds
     * all the data to be written.
     * Return a modify count.
     */
    public Object execute(AbstractRecord translationRow, Accessor accessor) throws XMLDataStoreException {
        Writer stream = this.getWriteStream(accessor, this.getRootElementName(), translationRow, this.getOrderedPrimaryKeyElements());
        AbstractRecord row = this.getModifyRow(accessor);
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
     * Return the row appropriate for logging.
     */
    protected AbstractRecord getLogRow() {
        return this.getModifyRow();
    }

    /**
     * Convenience method.
     * Return the modify row associated with the call.
     * This row contains all the data to be written to the datastore.
     */
    private AbstractRecord getModifyRow() {
        return ((ModifyQuery)this.getQuery()).getModifyRow();
    }

    /**
     * Convenience method.
     * Return the modify row associated with the call.
     * This row contains all the data to be written to the datastore.
     */
    protected AbstractRecord getModifyRow(Accessor accessor) {
        return (AbstractRecord)((XMLAccessor)accessor).convert(this.getModifyRow(), this.getSession());
    }

    /**
     * Return the appropriate write stream.
     */
    protected abstract Writer getWriteStream(Accessor accessor, String rootElementName, AbstractRecord translationRow, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;
}