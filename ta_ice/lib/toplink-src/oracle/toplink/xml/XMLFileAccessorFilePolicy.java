// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;

import oracle.toplink.sessions.Record;

/**
 * This policy is used by <code>XMLFileAccessor</code> to build the
 * files that will be wrapped in XML streams.
 *
 * @author Big Country
 * @since TOPLink/Java 4.5
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public interface XMLFileAccessorFilePolicy {

    /**
     * If necessary, create the source that holds all the files for
     * the XML documents with the specified root element name.
     */
    void createFileSource(String rootElementName) throws XMLDataStoreException;

    /**
     * If necessary, drop the source that holds all the files for
     * the XML documents with the specified root element name.
     */
    void dropFileSource(String rootElementName) throws XMLDataStoreException;

    /**
     * Return an enumeration on a <i>every</i> file for the specified root element name.
     */
    Enumeration getAllFiles(String rootElementName) throws XMLDataStoreException;

    /**
     * Return a file for the specified root element name and primary key.
     * The name of this file will typically take the form of
     *         [base dir]/[root element]/[key].xml
     */
    File getFile(String rootElementName, Record row, Vector orderedPrimaryKeyElements) throws XMLDataStoreException;

    /**
     * The accessor is connecting; if necessary, validate the policy's settings.
     */
    void validateConfiguration() throws XMLDataStoreException;
}