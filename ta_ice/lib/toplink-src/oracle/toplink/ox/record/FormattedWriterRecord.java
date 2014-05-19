// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.record;

import java.io.IOException;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.record.XMLFragmentReader;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>Use this type of MarshalRecord when the marshal target is a Writer and the
 * XML should be formatted with carriage returns and indenting.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * FormattedWriterRecord formattedWriterRecord = new FormattedWriterRecord();<br>
 * formattedWriterRecord.setWriter(myWriter);<br>
 * xmlMarshaller.marshal(myObject, formattedWriterRecord);<br>
 * </code></p>
 * <p>If the marshal(Writer) and setFormattedOutput(true) method is called on
 * XMLMarshaller, then the Writer is automatically wrapped in a
 * FormattedWriterRecord.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * xmlMarshaller xmlMarshaller.setFormattedOutput(true);<br>
 * xmlMarshaller.marshal(myObject, myWriter);<br>
 * </code></p>
 * @see oracle.toplink.ox.XMLMarshaller
 */
public class FormattedWriterRecord extends WriterRecord {
    private static final char[] TAB = "   ".toCharArray();
    private int numberOfTabs;
    private boolean complexType;

    public FormattedWriterRecord() {
        super();
        numberOfTabs = 0;
        complexType = true;
    }

    /**
     * INTERNAL:
     */
    public void startDocument(String encoding, String version) {
        try {
            getWriter().write("<?xml version=\"");
            getWriter().write(version);
            getWriter().write("\" encoding=\"");
            getWriter().write(encoding);
            getWriter().write("\"?>");
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void endDocument() {
        try {
          getWriter().write(Helper.cr());    
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void openStartElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        this.addPositionalNodes(xPathFragment, namespaceResolver);
        try {
            if (isStartElementOpen) {
                getWriter().write('>');
            }
            getWriter().write(Helper.cr());
            isStartElementOpen = true;
            for (int x = 0; x < numberOfTabs; x++) {
                getWriter().write(TAB);
            }
            getWriter().write('<');
            getWriter().write(xPathFragment.getShortName());
            numberOfTabs++;
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void element(String namespaceURI, String localName, String qName) {
        try {
            if (isStartElementOpen) {
                getWriter().write('>');
                isStartElementOpen = false;
            }
            getWriter().write(Helper.cr());
            for (int x = 0; x < numberOfTabs; x++) {
                getWriter().write(TAB);
            }
            super.element(namespaceURI, localName, qName);
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void endElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        try {
            numberOfTabs--;
            if (isStartElementOpen) {
                getWriter().write('/');
                getWriter().write('>');
                isStartElementOpen = false;
                return;
            }
            if (complexType) {
                getWriter().write(Helper.cr());
                for (int x = 0; x < numberOfTabs; x++) {
                    getWriter().write(TAB);
                }
            } else {
                complexType = true;
            }
            super.endElement(xPathFragment, namespaceResolver);
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void characters(String value) {
        super.characters(value);
        complexType = false;
    }
    
    /**
     * INTERNAL:
     */
    public void cdata(String value) {
        //Format the CDATA on it's own line
        try {
            if(isStartElementOpen) {
                getWriter().write('>');
                isStartElementOpen = false;
            }
            getWriter().write(Helper.cr());
            for (int x = 0; x < numberOfTabs; x++) {
                getWriter().write(TAB);
            }
            super.cdata(value);
            complexType=true;
        }catch(IOException ex) {
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
            try {
                FormattedWriterRecordContentHandler wrcHandler = new FormattedWriterRecordContentHandler();
                XMLFragmentReader xfragReader = new XMLFragmentReader(namespaceResolver);
                xfragReader.setContentHandler(wrcHandler);
                xfragReader.setProperty("http://xml.org/sax/properties/lexical-handler", wrcHandler);
                xfragReader.parse(node);
            } catch (SAXException sex) {
                throw XMLMarshalException.marshalException(sex);
            }
        }
    }
    
    /**
     * This class will typically be used in conjunction with an XMLFragmentReader.
     * The XMLFragmentReader will walk a given XMLFragment node and report events
     * to this class - the event's data is then written to the enclosing class' 
     * writer.
     * 
     * @see oracle.toplink.internal.ox.record.XMLFragmentReader
     * @see oracle.toplink.ox.record.WriterRecord.WriterRecordContentHandler
     */
    private class FormattedWriterRecordContentHandler extends WriterRecordContentHandler {
        // --------------------- CONTENTHANDLER METHODS --------------------- //
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            try {
            	if (isStartElementOpen) {
                    getWriter().write('>');
            	}
                getWriter().write(Helper.cr());
                for (int x = 0; x < numberOfTabs; x++) {
                    getWriter().write(TAB);
                }
                getWriter().write('<');
                getWriter().write(qName);
                numberOfTabs++;
                isStartElementOpen = true;
                
                // Handle attributes
                handleAttributes(atts);
                // Handle prefix mappings
                writePrefixMappings();
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }

        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            try {
                numberOfTabs--;
                if (isStartElementOpen) {
                    getWriter().write('/');
                    getWriter().write('>');
                    isStartElementOpen = false;
                    return;
                }
                if (complexType) {
                    getWriter().write(Helper.cr());
                    for (int x = 0; x < numberOfTabs; x++) {
                        getWriter().write(TAB);
                    }
                } else {
                    complexType = true;
                }
                super.endElement(namespaceURI, localName, qName);
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
        	if (isProcessingCData) {
            	cdata(new String (ch, start, length));
        		return;
        	}
        	if (new String(ch).trim().length() == 0) {
        		return;
        	}        	
            super.characters(ch, start, length);
            complexType = false;
        }
        
        // --------------------- LEXICALHANDLER METHODS --------------------- //
	public void comment(char[] ch, int start, int length) throws SAXException {
            try {
            	if (isStartElementOpen) {
	                getWriter().write('>');
	                getWriter().write(Helper.cr());
                    isStartElementOpen = false;
                }
            	writeComment(ch, start, length);
                complexType = false;
            } catch (IOException e) {
            	throw XMLMarshalException.marshalException(e);
            }
	}
    }
}