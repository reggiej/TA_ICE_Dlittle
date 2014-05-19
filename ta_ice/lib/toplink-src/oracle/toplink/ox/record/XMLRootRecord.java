// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.record;

import oracle.toplink.ox.XMLRoot;
import oracle.toplink.internal.ox.StrBuffer;
import oracle.toplink.internal.ox.XMLConversionManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Record for handling simple root elements that have a single text child node, 
 * and are being unmarshalled to a primitive wrapper object.  The characters
 * method will be used to gather the text to be converted.
 */
public class XMLRootRecord extends UnmarshalRecord {
    private Class targetClass;
	private String rootElementName;
    private String rootElementNamespaceUri;
    private StrBuffer characters;
    private boolean shouldReadChars;
    private int elementCount;

    /**
     * Default constructor.
     */
    public XMLRootRecord(Class cls) {
		super(null);
		targetClass = cls;
	    shouldReadChars = true;
	    elementCount = 0;
	}
	
    public void characters(char[] ch, int start, int length) throws SAXException {
    	if (characters == null) {
    		characters = new StrBuffer();
    	}
    	
    	if (shouldReadChars) {
    		characters.append(ch, start, length);
    	}
    }

    public void endDocument() throws SAXException {
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	// once the root element is closed (or any sub-elements for that matter) we don't
    	// want to process any more characters
    	shouldReadChars = false;
    }
    
    /**
     * Return a populated XMLRoot object.
     */
    public Object getCurrentObject() {
		// this assumes that since we're unmarshalling to a primitive wrapper, the root
		// element has a single text node.  if, however, the root element doesn't have
    	// a text node as a first child, we'll try converting null
    	String val = null;
    	if (characters != null) {
    		val = characters.toString();
    	}
    	XMLRoot xmlRoot = new XMLRoot();
    	xmlRoot.setObject(XMLConversionManager.getDefaultXMLManager().convertObject(val, targetClass));
    	xmlRoot.setLocalName(getRootElementName());
    	xmlRoot.setNamespaceURI(getRootElementNamespaceUri());
        return xmlRoot;
    }
    
    /**
     * Return the root element's prefix qualified name
     * 
     * @return
     */
    public String getRootElementName() {
    	return rootElementName;
    }
    
    /**
     * Return the root element's namespace URI
     * 
     * @return
     */
    public String getRootElementNamespaceUri() {
    	return rootElementNamespaceUri;
    }
    
    public void startDocument() throws SAXException {
    }    	

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    	// set the root element's prefix qualified name and namespace prefix
    	if (rootElementName == null) {
    		rootElementName = qName;
    		rootElementNamespaceUri = namespaceURI;
    	}
    	elementCount++;
    	if (elementCount > 1) {
    		// we only want to process characters from the forst text child;
    		// if a subelement occurs, we will stop
    		shouldReadChars = false;
    	}
    }
}
