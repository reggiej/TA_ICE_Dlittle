// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import oracle.toplink.ox.XMLBinder;
import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.XMLRoot;

/**
 * INTERNAL
 *  <p><b>Purpose:</b>Provide a TopLink implementation of the javax.xml.bind.Binder interface
 *  <p><b>Responsibilities:</b><ul>
 *  <li>Provide an implementation of Binder</li>
 *  <li>Provide a means to preserve unmapped XML Data</li>
 *  
 *  @author  mmacivor
 *  @since   Oracle TopLink 11.1.1.0.0
 *  @see javax.xml.bind.Binder
 */
public class JAXBBinder extends Binder {
    private XMLContext xmlContext;
    private XMLBinder xmlBinder;
   
    public JAXBBinder(XMLContext xmlContext) {
        this.xmlContext = xmlContext;
        this.xmlBinder = this.xmlContext.createBinder();        
        this.xmlBinder.getDocumentPreservationPolicy().setNodeOrderingPolicy(new oracle.toplink.ox.documentpreservation.IgnoreNewElementsOrderingPolicy());
    }
    public void marshal(Object obj, Object xmlNode) {}
    
    public Object updateXML(Object obj) {
        this.xmlBinder.updateXML(obj);
        return xmlBinder.getXMLNode(obj);
    }

    public Object updateXML(Object obj, Object xmlNode) {
        if(!(xmlNode instanceof org.w3c.dom.Node)) {
            return null;
        } else {
            xmlBinder.updateXML(obj, ((Element)xmlNode));
            return xmlNode;
        }
    }
    
    public void setSchema(Schema schema) {
        
    }
    
    public Schema getSchema() {
        return null;
    }
    
    public JAXBElement getJAXBNode(Object obj) {
        Element elem = (Element)xmlBinder.getXMLNode(obj);
        return new JAXBElement(new QName(elem.getNamespaceURI(), elem.getLocalName()), obj.getClass(), obj);
    }
    
    public void setEventHandler(ValidationEventHandler handler) {
        
    }
    
    public ValidationEventHandler getEventHandler() {
        return null;
    }
    
    public Object updateJAXB(Object obj) {
        if(!(obj instanceof Node)) {
            return null;
        }
        xmlBinder.updateObject((Node)obj);
        return xmlBinder.getObject((Node)obj);
    }
    
    public Object getProperty(String propName) {
        return null;
    }
    
    public void setProperty(String propName, Object value) {
    }
    
    public Object getXMLNode(Object obj) {
        return null;
    }
    
    public Object unmarshal(Object obj) {
        if(!(obj instanceof Node)) {
            return null;
        }
        return xmlBinder.unmarshal((Node)obj);
    }
    
    public JAXBElement unmarshal(Object obj, Class javaClass) {
        if(!(obj instanceof Node)) {
            return null;
        }
        XMLRoot xmlRoot = (XMLRoot)xmlBinder.unmarshal((Node)obj, javaClass);
        return new JAXBElement(new QName(xmlRoot.getNamespaceURI(), xmlRoot.getLocalName()), javaClass, xmlRoot.getObject());
    }
}
