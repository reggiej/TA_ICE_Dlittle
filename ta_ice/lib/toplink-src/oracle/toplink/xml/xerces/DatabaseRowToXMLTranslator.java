// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml.xerces;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformException;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.platform.xml.XMLTransformer;
import oracle.toplink.sdk.*;
import oracle.toplink.sessions.Record;
import oracle.toplink.xml.XMLDataStoreException;

/**
 * This class has a singular purpose: convert a <code>Record</code> to an XML document.
 * Given a <code>Record</code>, this class will write a corresponding XML document on a
 * <code>Writer</code>.
 * This is old obsoletish code that use to be hardcoded to xerces, but is now hard coded to oracle.
 * The generic convert is in the xml package and parser independent.
 *
 * @see oracle.toplink.sessions.Record
 * @see oracle.toplink.sdk.SDKFieldValue
 *
 * @author Les Davis
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class DatabaseRowToXMLTranslator extends oracle.toplink.xml.DatabaseRowToXMLTranslator {

    /**
     * Default constructor.
     */
    public DatabaseRowToXMLTranslator() {
        super();
    }

    /**
     * Convert the specified database row to an XML document and
     * write it to the specified stream.
     */
    public void write(Writer outputStream, AbstractRecord row) throws XMLDataStoreException {
        if (row.isEmpty()) {
            return;
        }
        try {
            XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
            Document document = xmlPlatform.createDocument();
            document.appendChild(buildRootElement(document, row));

            XMLTransformer xmlTransformer = xmlPlatform.newXMLTransformer();
            xmlTransformer.setVersion("1.0");
            xmlTransformer.setEncoding("UTF-8");
            xmlTransformer.transform(document, outputStream);
        } catch (XMLPlatformException e) {
            throw XMLDataStoreException.generalException(e);
        }
    }

    /**
     * Builds the root element for the document
     */
    protected Node buildRootElement(Document document, AbstractRecord row) throws XMLDataStoreException {
        String rootElementName = ((DatabaseField)row.getFields().firstElement()).getTableName();
        Element rootElement = document.createElement(rootElementName);
        addFieldsToElementFromRow(document, rootElement, row, rootElementName);

        return rootElement;
    }

    /**
     * Build a element representing a nested row.
     * Temporary need to disable the way nested rows are handled.
     */
    protected Node buildNestedRowElement(Document document, Node parent, DatabaseField field, SDKFieldValue value) throws XMLDataStoreException {
        String elementDataTypeName = field.getName();
        Node rootElement = document.createElement(elementDataTypeName);
        for (Enumeration enumtr = value.getElements().elements(); enumtr.hasMoreElements();) {
            Object row = enumtr.nextElement();
            if (row instanceof AbstractRecord) {
            	AbstractRecord dbRow = (AbstractRecord)row;
                String subRowDataTypeName = value.getElementDataTypeName();
                Node subRowElement;
                if (rowIsAllNulls(dbRow)) {
                    subRowElement = document.createElement(subRowDataTypeName);
                    rootElement.appendChild(subRowElement);
                } else {
                    subRowElement = document.createElement(subRowDataTypeName);
                    addFieldsToElementFromRow(document, subRowElement, dbRow, subRowDataTypeName);
                    rootElement.appendChild(subRowElement);
                }
            } else {
                throw XMLDataStoreException.invalidFieldValue(elementDataTypeName, row);
            }
        }
        return rootElement;
    }
}