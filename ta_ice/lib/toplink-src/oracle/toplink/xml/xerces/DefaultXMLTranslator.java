// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml.xerces;

import java.io.*;

import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.sessions.Record;

/**
 * This is the default implementation of the <code>XMLTranslator</code>
 * interface.  It implements the read and write methods
 * for converting XML documents to database rows and vice versa.
 *
 * @see XMLCall
 *
 * @author Les Davis
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class DefaultXMLTranslator implements oracle.toplink.xml.XMLTranslator {

    /** The translator the converts an XML document to a Record. */
    private XMLToDatabaseRowTranslator readTranslator;

    /** The translator the converts a Record to an XML document. */
    private DatabaseRowToXMLTranslator writeTranslator;

    /**
     * Default constructor.
     */
    public DefaultXMLTranslator() {
        super();
    }

    /**
     * Return the read translator.
     * Lazy initialize because it may not be needed.
     */
    protected XMLToDatabaseRowTranslator getReadTranslator() {
        if (readTranslator == null) {
            readTranslator = new XMLToDatabaseRowTranslator();
        }
        return readTranslator;
    }

    /**
     * Return the write translator.
     * Lazy initialize because it may not be needed.
     */
    protected DatabaseRowToXMLTranslator getWriteTranslator() {
        if (writeTranslator == null) {
            writeTranslator = new DatabaseRowToXMLTranslator();
        }
        return writeTranslator;
    }

    /**
     * Read an XML document from the specified input stream and
     * convert it to a TopLink database row.
     * Close the stream and return the row.
     */
    public Record read(Reader stream) {
        return this.getReadTranslator().read(stream);
    }

    /**
     * Convert the specified database row to an XML document and
     * write it to the specified stream.
     */
    public void write(java.io.Writer stream, Record row) {
        this.getWriteTranslator().write(stream, (AbstractRecord)row);
    }
}