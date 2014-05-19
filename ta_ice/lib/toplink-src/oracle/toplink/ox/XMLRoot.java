// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox;

import javax.xml.namespace.QName;
import oracle.toplink.internal.ox.XPathFragment;

public class XMLRoot {
    protected Object rootObject;
    protected XPathFragment rootFragment;
    protected String encoding;
    protected String xmlVersion;
    protected String schemaLocation;
    protected String noNamespaceSchemaLocation;
    protected QName schemaType;

    public XMLRoot() {
        rootFragment = new XPathFragment();
    }

    public Object getObject() {
        return rootObject;
    }

    public String getLocalName() {
        return rootFragment.getLocalName();
    }

    public String getNamespaceURI() {
        return rootFragment.getNamespaceURI();
    }

    public void setObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    /**
     * Set the element name.  This method will parse the qualified
     * name in an attempt to set the prefix and localName fields.  If
     * there is no prefix, the prefix field is set to null.
     *
     * @param qualifiedName a fully qualified element name
     */
    public void setLocalName(String name) {
        rootFragment.setXPath(name);
    }

    public void setNamespaceURI(String rootElementUri) {
        rootFragment.setNamespaceURI(rootElementUri);
    }

    /**
     * INTERNAL:
     */
    public XPathFragment getRootFragment() {
        return rootFragment;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getXMLVersion() {
        return xmlVersion;
    }

    public void setVersion(String version) {
        this.xmlVersion = version;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }

    public void setNoNamespaceSchemaLocation(String noNamespaceSchemaLocation) {
        this.noNamespaceSchemaLocation = noNamespaceSchemaLocation;
    }

    public void setSchemaType(QName schemaType) {
        this.schemaType = schemaType;
    }

    public QName getSchemaType() {
        return schemaType;
    }
}