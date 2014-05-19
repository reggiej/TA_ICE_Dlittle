// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map;
import oracle.toplink.internal.ox.StrBuffer;
import oracle.toplink.ox.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * <p><b>Purpose</b>:  Build a DOM from SAX events.</p>
 */

public class SAXDocumentBuilder implements ContentHandler {
    protected Document document;
    protected Stack nodes;
    protected XMLPlatform xmlPlatform;
    protected Map namespaceDeclarations;
    protected StrBuffer stringBuffer;

    public SAXDocumentBuilder() {
        super();
        nodes = new Stack();
        xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        stringBuffer = new StrBuffer();
        namespaceDeclarations = new HashMap();
    }

    public Document getDocument() {
        return document;
    }

    public Document getInitializedDocument() throws SAXException {
        if (document == null) {
            try {
                document = xmlPlatform.createDocument();
                nodes.push(document);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
        return document;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
        try {
            document = xmlPlatform.createDocument();
            nodes.push(document);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void endDocument() throws SAXException {
        nodes.pop();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // avoid adding an empty prefix and uri pair to the map
        if (prefix.equals("") && uri.equals("")) {
            return;
        }
        
        if (namespaceDeclarations == null) {
            namespaceDeclarations = new HashMap();
        }
        namespaceDeclarations.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if ((null != namespaceURI) && ("".equals(namespaceURI))) {
            namespaceURI = null;
        }
        Element element = getInitializedDocument().createElementNS(namespaceURI, qName);
        Node parentNode = (Node)nodes.peek();

        if ((stringBuffer.length() > 0) && !(nodes.size() == 1)) {
            Text text = getInitializedDocument().createTextNode(stringBuffer.toString());
            parentNode.appendChild(text);
            stringBuffer.reset();
        }
        appendChildNode(parentNode, element);
        nodes.push(element);

        if (namespaceDeclarations != null) {
            Iterator namespacePrefixes = namespaceDeclarations.keySet().iterator();
            String prefix;
            String uri;
            while (namespacePrefixes.hasNext()) {
                prefix = (String)namespacePrefixes.next();
                uri = (String)namespaceDeclarations.get(prefix);
                addNamespaceDeclaration(element, prefix, uri);
            }
            namespaceDeclarations = null;
        }

        int numberOfAttributes = atts.getLength();
        String attributeNamespaceURI;
        for (int x = 0; x < numberOfAttributes; x++) {
            attributeNamespaceURI = atts.getURI(x); 
            if ((null != attributeNamespaceURI) && ("".equals(attributeNamespaceURI))) {
                attributeNamespaceURI = null;
            }
            element.setAttributeNS(attributeNamespaceURI, atts.getQName(x), atts.getValue(x));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        Element endedElement = (Element)nodes.pop();
        if (stringBuffer.length() > 0) {
            Text text = getInitializedDocument().createTextNode(stringBuffer.toString());
            endedElement.appendChild(text);
            stringBuffer.reset();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        stringBuffer.append(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
        ProcessingInstruction pi = getInitializedDocument().createProcessingInstruction(target, data);
        Node parentNode = (Node)nodes.peek();
        parentNode.appendChild(pi);
    }

    public void skippedEntity(String name) throws SAXException {
    }

    protected void addNamespaceDeclaration(Element parentElement, String prefix, String uri) {
        if (prefix.equals("")) {
            //handle default/target namespaces
            parentElement.setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS, uri);
        } else {
            parentElement.setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + prefix, uri);
        }
    }
    
    public void appendChildNode(Node parentNode, Node childNode) {
        parentNode.appendChild(childNode);
    }
}