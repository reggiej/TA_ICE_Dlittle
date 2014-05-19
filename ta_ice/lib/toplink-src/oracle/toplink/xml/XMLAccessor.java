// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.sessions.Record;

/**
 * This interface defines methods needed for <code>Accessor</code> in the XML context.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public interface XMLAccessor extends Accessor {

    /**
     * Everything in XML must be strings.
     */
    Record convert(Record row, AbstractSession session);

    /**
     * Create a source for data streams for
     * the XML documents with the specified root element name.
     */
    void createStreamSource(String rootElementName) throws XMLDataStoreException;

    /**
     * Delete the data for the specified root element and primary key.
     * Return the stream count (1 or 0).
     */
    Integer deleteStream(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * Drop the source for data streams for
     * the XML documents with the specified root element name.
     */
    void dropStreamSource(String rootElementName) throws XMLDataStoreException;

    /**
     * If it exists, return a read stream on the data for the specified
     * root element and primary key.
     * If it does not exist, return null.
     */
    Reader getExistenceCheckStream(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * Return a write stream that will overwrite the data for the specified
     * root element and primary key.
     */
    Writer getExistingWriteStream(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * Return a new write stream for the specified
     * root element and primary key.
     */
    Writer getNewWriteStream(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * Return a read stream on the data for the specified
     * root element and primary key.
     * If the stream is not found return null.
     */
    Reader getReadStream(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * Return an enumeration on a collection of read streams,
     * one for *every* document with the specified root element.
     */
    Enumeration getReadStreams(String rootElementName) throws XMLDataStoreException;

    /**
     * Return an enumeration on a collection of streams,
     * one for every specified foreign key.
     * If a particular stream is not found the
     * enumeration will return null in its place.
     */
    Enumeration getReadStreams(String rootElementName, Vector foreignKeys, Vector orderedForeignKeyElements) throws XMLDataStoreException;

    /**
     * Return the default XML translator for all data store calls.
     */
    XMLTranslator getXMLTranslator();

    /**
     * Set the default XML translator for all data store calls.
     */
    void setXMLTranslator(XMLTranslator translator);
}