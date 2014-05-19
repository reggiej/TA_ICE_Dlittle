// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.record;

import javax.xml.namespace.QName;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.XMLMarshaller;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.ox.documentpreservation.DocumentPreservationPolicy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * PUBLIC:
 * Provides a Record/Map API on an XML DOM element.
 */
public abstract class XMLRecord extends AbstractRecord {
    private XMLMarshaller marshaller;
    private XMLUnmarshaller unmarshaller;
    private DocumentPreservationPolicy docPresPolicy;
    private Object owningObject;
    private Object currentObject;
    private QName leafElementType;
    private NamespaceResolver namespaceResolver;

    public XMLRecord() {
        super(null, null);
        namespaceResolver = new NamespaceResolver();
        // Required for subclasses.
    }

    /**
     * PUBLIC:
     * Get the local name of the context root element.
     */
    public abstract String getLocalName();

    /**
     * PUBLIC:
     *  Get the namespace URI for the context root element.
     */
    public abstract String getNamespaceURI();

    /**
     * PUBLIC:
     * Clear the sub-nodes of the DOM.
     */
    public abstract void clear();

    /**
     * PUBLIC:
     * Return the document.
     */
    public abstract Document getDocument();

    /**
     * PUBLIC:
     * Check if the value is contained in the row.
     */
    public boolean contains(Object value) {
        return values().contains(value);
    }

    /**
    * PUBLIC:
    * Return the DOM.
    */
    public abstract Node getDOM();

    /**
     * Return the XML string representation of the DOM.
     */
    public abstract String transformToXML();

    /**
     * INTERNAL:
     * Convert a DatabaseField to an XMLField
     */
    protected XMLField convertToXMLField(DatabaseField databaseField) {
        try {
            return (XMLField)databaseField;
        } catch (ClassCastException ex) {
            return new XMLField(databaseField.getName());
        }
    }

    /**
     * INTERNAL:
     * Retrieve the value for the field. If missing null is returned.
     */
    public Object get(DatabaseField key) {
        return getIndicatingNoEntry(key);
    }
    /**
     * INTERNAL:
     * Retrieve the value for the field name.
     */
    public Object getIndicatingNoEntry(String fieldName) {
        return getIndicatingNoEntry(new XMLField(fieldName));
    }

    public String resolveNamespacePrefix(String prefix) {
        return null;
    }

    /**
     * INTERNAL:
     */
    public XMLMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * INTERNAL:
     */
    public void setMarshaller(XMLMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * INTERNAL:
     */
    public XMLUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * INTERNAL:
     */
    public void setUnmarshaller(XMLUnmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public void setDocPresPolicy(DocumentPreservationPolicy policy) {
        this.docPresPolicy = policy;
    }
    
    public DocumentPreservationPolicy getDocPresPolicy() {
        return docPresPolicy;
    }
    /**
     * INTERNAL:
     */
    public Object getOwningObject() {
        return owningObject;
    }

    /**
     * INTERNAL:
     */
    public void setOwningObject(Object obj) {
        this.owningObject = obj;
    }

    /**
     * INTERNAL:
     */
    public Object getCurrentObject() {
        return currentObject;
    }

    /**
     * INTERNAL:
     */
    public void setCurrentObject(Object obj) {
        this.currentObject = obj;
    }
    /**
     * INTERNAL:
     */
    public QName getLeafElementType() {
        return leafElementType;
    }
    /**
     * INTERNAL:
     */
    public void setLeafElementType(QName type) {
        leafElementType = type;
    }

    public void setNamespaceResolver(NamespaceResolver nr) {
        namespaceResolver = nr;
    }

    public NamespaceResolver getNamespaceResolver() {
        return namespaceResolver;
    }
}
