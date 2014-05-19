// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.record;

import java.io.Writer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.record.XMLFragmentReader;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>Use this type of MarshalRecord when the marshal target is a Writer and the
 * XML should be not be formatted with carriage returns and indenting.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * WriterRecord writerRecord = new WriterRecord();<br>
 * writerRecord.setWriter(myWriter);<br>
 * xmlMarshaller.marshal(myObject, writerRecord);<br>
 * </code></p>
 * <p>If the marshal(Writer) and setFormattedOutput(false) method is called on
 * XMLMarshaller, then the Writer is automatically wrapped in a
 * WriterRecord.</p>
 * <p><code>
 * XMLContext xmlContext = new XMLContext("session-name");<br>
 * XMLMarshaller xmlMarshaller = xmlContext.createMarshaller();<br>
 * xmlMarshaller xmlMarshaller.setFormattedOutput(false);<br>
 * xmlMarshaller.marshal(myObject, myWriter);<br>
 * </code></p>
 * @see oracle.toplink.ox.XMLMarshaller
 */
public class WriterRecord extends MarshalRecord {
    protected Writer writer;
    protected boolean isStartElementOpen = false;
    protected boolean isProcessingCData = false;

    /**
     * Return the Writer that the object will be marshalled to.
     * @return The marshal target.
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Set the Writer that the object will be marshalled to.
     * @param writer The marshal target.
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * INTERNAL:
     */
    public void startDocument(String encoding, String version) {
        try {
            writer.write("<?xml version=\"");
            writer.write(version);
            writer.write("\" encoding=\"");
            writer.write(encoding);
            writer.write("\"?>");
            writer.write(Helper.cr());
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void endDocument() {}

    /**
     * INTERNAL:
     */
    public void openStartElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        super.openStartElement(xPathFragment, namespaceResolver);
        try {
            if (isStartElementOpen) {
                writer.write('>');
            }
            isStartElementOpen = true;
            writer.write('<');
            writer.write(xPathFragment.getShortName());
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
                writer.write('>');
                isStartElementOpen = false;
            }
            getWriter().write('<');
            getWriter().write(qName);
            getWriter().write('/');
            getWriter().write('>');
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void attribute(XPathFragment xPathFragment, NamespaceResolver namespaceResolver, String value) {
        attribute(null, xPathFragment.getLocalName(), xPathFragment.getShortName(), value);
    }

    /**
     * INTERNAL:
     */
    public void attribute(String namespaceURI, String localName, String qName, String value) {
        try {
            writer.write(' ');
            writer.write(qName);
            writer.write('=');
            writer.write('\"');
            writeValue(value);
            writer.write('\"');
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void closeStartElement() {}

    /**
     * INTERNAL:
     */
    public void endElement(XPathFragment xPathFragment, NamespaceResolver namespaceResolver) {
        try {
            if (isStartElementOpen) {
                writer.write('/');
                writer.write('>');
                isStartElementOpen = false;
            } else {
                writer.write('<');
                writer.write('/');
                writer.write(xPathFragment.getShortName());
                writer.write('>');
            }
            isStartElementOpen = false;
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
     * INTERNAL:
     */
    public void characters(String value) {
        try {
            if (isStartElementOpen) {
                isStartElementOpen = false;
                writer.write('>');
            }
            writeValue(value);
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }
    
    /**
     * INTERNAL:
     */
    public void cdata(String value) {
        try {
            if(isStartElementOpen) {
                isStartElementOpen = false;
                writer.write('>');
            }
            String valueToWrite = "<![CDATA[" + value + "]]>";
            writer.write(valueToWrite);
        } catch(IOException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }
    
    /**
     * INTERNAL:
     */
    protected void writeValue(String value) {
        try {
            char[] chars = value.toCharArray();
            int charsSize = chars.length;
            char character;
            for (int x = 0; x < charsSize; x++) {
                character = chars[x];
                switch (character) {
                case '&': {
                    writer.write("&amp;");
                    break;
                }
                case '<': {
                    writer.write("&lt;");
                    break;
                }
                default:
                    writer.write(character);
                }
            }
        } catch (IOException e) {
            throw XMLMarshalException.marshalException(e);
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
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            characters(node.getNodeValue());
        } else {
            try {
                WriterRecordContentHandler wrcHandler = new WriterRecordContentHandler();
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
     */
    protected class WriterRecordContentHandler implements ContentHandler, LexicalHandler {
        Map<String, String> prefixMappings;
        
        WriterRecordContentHandler() {
            prefixMappings = new HashMap<String, String>();
        }
        
        // --------------------- CONTENTHANDLER METHODS --------------------- //
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            try {
            	if (isStartElementOpen) {
                    getWriter().write('>');
            	}
            	
            	getWriter().write('<');
            	getWriter().write(qName);
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
                if (isStartElementOpen) {
                    getWriter().write('/');
                    getWriter().write('>');
                } else {
                        getWriter().write('<');
                        getWriter().write('/');
                        getWriter().write(qName);
                        getWriter().write('>');
                }
                isStartElementOpen = false;
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            String namespaceUri = getNamespaceResolver().resolveNamespacePrefix(prefix);
            if(namespaceUri == null || !namespaceUri.equals(uri)) {
                prefixMappings.put(prefix, uri);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isProcessingCData) {
            	cdata(new String (ch, start, length));
            	return;
            }
            
            if (isStartElementOpen) {
                try {
                    getWriter().write('>');
                    isStartElementOpen = false;
                } catch (IOException e) {
                    throw XMLMarshalException.marshalException(e);
                }
            }
        	writeValue(new String(ch, start, length));
        }

        // --------------------- LEXICALHANDLER METHODS --------------------- //
		public void comment(char[] ch, int start, int length) throws SAXException {
            try {
            	if (isStartElementOpen) {
	                getWriter().write('>');
                    isStartElementOpen = false;
                }
            	writeComment(ch, start, length);
            } catch (IOException e) {
            	throw XMLMarshalException.marshalException(e);
            }
		}
		
		public void startCDATA() throws SAXException {
			isProcessingCData = true;
		}
		
		public void endCDATA() throws SAXException {
			isProcessingCData = false;
		}
        
        // --------------------- CONVENIENCE METHODS --------------------- //
        protected void writePrefixMappings() {
            try {
                if (!prefixMappings.isEmpty()) {
                    for (java.util.Iterator<String> keys = prefixMappings.keySet().iterator(); keys.hasNext();) {
                        String prefix = keys.next();
                        getWriter().write(' ');
                        getWriter().write(XMLConstants.XMLNS);
                        getWriter().write(":");
                        getWriter().write(prefix);
                        getWriter().write('=');
                        getWriter().write('"');
                        getWriter().write((String) prefixMappings.get(prefix));
                        getWriter().write('"');
                    }
                    prefixMappings.clear();
                }
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }
        
        protected void handleAttributes(Attributes atts) {
            for (int i=0; i<atts.getLength(); i++) {
                if((atts.getQName(i) != null && atts.getQName(i).startsWith(XMLConstants.XMLNS + ":"))) {
                    continue;
                }
                attribute(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getValue(i));
            }
        }
        
        protected void writeComment(char[] chars, int start, int length) {
            try {
                getWriter().write('<');
                getWriter().write('!');
                getWriter().write('-');
                getWriter().write('-');
                for (int x = start; x < length; x++) {
                    getWriter().write(chars[x]);
                }
                getWriter().write('-');
                getWriter().write('-');
                getWriter().write('>');
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }
        
        protected void writeCharacters(char[] chars, int start, int length) {
            try {
                for (int x = start; x < length; x++) {
                    getWriter().write(chars[x]);
                }
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
        }
        // --------------- SATISFY CONTENTHANDLER INTERFACE --------------- //
        public void endPrefixMapping(String prefix) throws SAXException {}
        public void processingInstruction(String target, String data) throws SAXException {}
        public void setDocumentLocator(Locator locator) {}
        public void startDocument() throws SAXException {}
        public void endDocument() throws SAXException {}
        public void skippedEntity(String name) throws SAXException {}
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

        // --------------- SATISFY LEXICALHANDLER INTERFACE --------------- //
        public void startEntity(String name) throws SAXException {}
		public void endEntity(String name) throws SAXException {}
		public void startDTD(String name, String publicId, String systemId) throws SAXException {}
		public void endDTD() throws SAXException {}
    }
}