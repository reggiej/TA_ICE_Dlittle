// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.record;

import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.record.XMLFragmentReader;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>Use this type of MarshalRecord when the marshal target is a
 * ContentHandler.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * ContentHandlerRecord contentHandlerRecord = new ContentHandlerRecord();<br>
 * marshalRecord.setContentHandler(myContentHandler);<br>
 * xmlMarshaller.marshal(myObject, contentHandlerRecord);<br>
 * </code></p>
 * <p>If the marshal(ContentHandler) method is called on XMLMarshaller, then the
 * ContentHanlder is automatically wrapped in a ContentHandlerRecord.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * xmlMarshaller.marshal(myObject, contentHandler);<br>
 * </code></p>
 * @see oracle.toplink.ox.XMLMarshaller
 */
public class ContentHandlerRecord extends MarshalRecord {
    private ContentHandler contentHandler;
    private LexicalHandler lexicalHandler;
    private String namespaceURI;
    private XPathFragment xPathFragment;
    private AttributesImpl attributes;
    private static final String CDATA = "CDATA";

    // bug#5035551 - content handler record will act more like writer 
    // record in that startElement is called with any attributes that
    // are to be written to the element.  So, instead of calling 
    // openStartElement > attribute > closeStartElement, we'll gather
    // any required attributes and make a single call to openAndCloseStartElement.
    // This is necessary as the contentHandler.startElement() call results in
    // a completed element the we cannot add attributes to after the fact.
    protected boolean isStartElementOpen = false;

    /**
     * Return the ContentHandler that the object will be marshalled to.
     * @return The marshal target.
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Set the ContentHandler that the object will be marshalled to.
     * @param contentHandler The marshal target.
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }
    
    /**
     * Set the LexicalHandler to recieve CDATA related events
     */
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * INTERNAL:
     */
    public void startDocument(String encoding, String version) {
        try {
            contentHandler.startDocument();
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void endDocument() {
        try {
            contentHandler.endDocument();
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void startPrefixMapping(String prefix, String namespaceURI) {
        try {
            contentHandler.startPrefixMapping(prefix, namespaceURI);
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void endPrefixMapping(String prefix) {
        try {
            contentHandler.endPrefixMapping(prefix);
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     * 
     * Create a start element tag - this call results in a complete start element, 
     * i.e. closeStartElement() does not need to be called after a call to this 
     * method.
     * 
     */
    private void openAndCloseStartElement() {
    	try {
            contentHandler.startElement(namespaceURI, xPathFragment.getLocalName(), xPathFragment.getShortName(), attributes);
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }
    
    /**
     * INTERNAL:
     */
    public void openStartElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        super.openStartElement(xPathFragment, namespaceResolver);
        if (isStartElementOpen) {
        	openAndCloseStartElement();
        }
        isStartElementOpen = true;
        this.namespaceURI = resolveNamespacePrefix(xPathFragment, namespaceResolver);
        this.xPathFragment = xPathFragment;
        this.attributes = new AttributesImpl();
        
    }

    /**
     * INTERNAL:
     */
    public void element(String namespaceURI, String localName, String qName) {
        if (isStartElementOpen) {
        	openAndCloseStartElement();
            isStartElementOpen = false;
        }
        try {
            this.attributes = new AttributesImpl();
            contentHandler.startElement(namespaceURI, localName, qName, attributes);
            contentHandler.endElement(namespaceURI, localName, qName);
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void attribute(XPathFragment xPathFragment, NamespaceResolver namespaceResolver, String value) {
        String namespaceURI = resolveNamespacePrefix(xPathFragment, namespaceResolver);
        attributes.addAttribute(namespaceURI, xPathFragment.getLocalName(), xPathFragment.getShortName(), CDATA, value);
    }

    /**
     * INTERNAL:
     */
    public void attribute(String namespaceURI, String localName, String qName, String value) {
        attributes.addAttribute(namespaceURI, localName, qName, CDATA, value);
    }
    
    /**
     * INTERNAL:
     */
    public void closeStartElement() {
    	// do nothing - the openAndCloseStartElement call results in a 
    	// complete start element
    }

    /**
     * INTERNAL:
     */
    public void endElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        if (isStartElementOpen) {
        	openAndCloseStartElement();
            isStartElementOpen = false;
        }
        try {
            String namespaceURI = resolveNamespacePrefix(xPathFragment, namespaceResolver);
            contentHandler.endElement(namespaceURI, xPathFragment.getLocalName(), xPathFragment.getShortName());
            isStartElementOpen = false;
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void characters(String value) {
        if (isStartElementOpen) {
        	openAndCloseStartElement();
            isStartElementOpen = false;
        }
        try {
            contentHandler.characters(value.toCharArray(), 0, value.length());
        } catch (SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }
    
    /**
     * INTERNAL:
     */
    public void cdata(String value) {
        //No specific support for CDATA in a ContentHandler. Just treat as regular
        //Character data as a SAX parser would.
        if (isStartElementOpen) {
            openAndCloseStartElement();
            isStartElementOpen = false;
        }
        try {
            if(lexicalHandler != null) {
                lexicalHandler.startCDATA();
            }
            characters(value);
            if(lexicalHandler != null) {
                lexicalHandler.endCDATA();
            }
        } catch(SAXException ex) {
            throw XMLMarshalException.marshalException(ex);
        }
    }
    
    /**
     * Receive notification of a node.
     * @param node The Node to be added to the document
     * @param namespaceResolver The NamespaceResolver can be used to resolve the
     * namespace URI/prefix of the node
     */
    public void node(Node node, NamespaceResolver namespaceResolver) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr attr = (Attr) node;
            String resolverPfx = null;
            if (namespaceResolver != null) {
                resolverPfx = namespaceResolver.resolveNamespaceURI(attr.getNamespaceURI());
            }
            // If the namespace resolver contains a prefix for the attribute's URI,
            // use it instead of what is set on the attribute
            if (resolverPfx != null) {
                attribute(attr.getNamespaceURI(), "", resolverPfx+":"+attr.getLocalName(), attr.getNodeValue());
            } else {
                attribute(attr.getNamespaceURI(), "", attr.getName(), attr.getNodeValue());
                // May need to declare the URI locally
                if (attr.getNamespaceURI() != null) {
                    attribute(XMLConstants.XMLNS_URL, "",XMLConstants.XMLNS + ":" + attr.getPrefix(), attr.getNamespaceURI());
                }
            }
        } else {
            if (isStartElementOpen) {
                openAndCloseStartElement();
                isStartElementOpen = false;
            }
            if (node.getNodeType() == Node.TEXT_NODE) {
                characters(node.getNodeValue());
            } else {
                XMLFragmentReader xfragReader = new XMLFragmentReader(namespaceResolver);
                xfragReader.setContentHandler(contentHandler);
                try {
                    xfragReader.parse(node);
                } catch (SAXException sex) {
                    throw XMLMarshalException.marshalException(sex);
                }
            }
        }
    }
        
    public String resolveNamespacePrefix(XPathFragment frag, NamespaceResolver resolver) {
        String resolved = super.resolveNamespacePrefix(frag, resolver);
        if (resolved == null) {
            return "";
        }
        return resolved;
    }

    public String resolveNamespacePrefix(String s) {
        String resolved = super.resolveNamespacePrefix(s);
        if (resolved == null) {
            return "";
        }
        return resolved;
    }
}
