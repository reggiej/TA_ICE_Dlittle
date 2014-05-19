// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox;

import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.platform.xml.SAXDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <p>Class used to unmarshal SAX events to objects.
 *
 * <p>Create an XMLUnmarshallerHandler from an XMLUnmarshaller.<br>
 *  <em>Code Sample</em><br>
 *  <code>
 *  XMLContext context = new XMLContext("mySessionName");<br>
 *  XMLUnmarshaller unmarshaller = context.createUnmarshaller();<br>
 *  XMLUnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();<br>
 *  <code>
 *
 * <p>Use the UnmarshallerHandler with an XMLReader<br>
 *  <em>Code Sample</em><br>
 *  <code>
 *  SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();<br>
 *  saxParserFactory.setNamespaceAware(true);<br>
 *  SAXParser saxParser = saxParserFactory.newSAXParser();<br>
 *  XMLReader xmlReader = saxParser.getXMLReader();<br>
 *  xmlReader.setContentHandler(xmlUnmarshallerHandler);<br>
 *  FileInputStream inputStream = new FileInputStream("MyFile.xml");<br>
 *  InputSource inputSource = new InputSource(inputStream);<br>
 *  xmlReader.parse(inputSource);<br>
 *  Object result = xmlUnmarshallerHandler.getResult();<br>
 *  <code>
 *
 * <p>XML that can be unmarshalled is XML which has a root tag that corresponds
 * to a default root element on an XMLDescriptor in the TopLink project associated
 * with the XMLContext.
 *
 * @see oracle.toplink.ox.XMLUnmarshaller
 */
public class XMLUnmarshallerHandler extends SAXDocumentBuilder {
    private XMLUnmarshaller xmlUnmarshaller;
    private boolean endDocumentTriggered;

    XMLUnmarshallerHandler(XMLUnmarshaller xmlUnmarshaller) {
        super();
        this.xmlUnmarshaller = xmlUnmarshaller;
    }

    public void endDocument() throws SAXException {
        endDocumentTriggered = true;
        super.endDocument();
    }

    public void startDocument() throws SAXException {
        endDocumentTriggered = false;
        super.startDocument();
    }

    /**
     * Returns the object that was unmarshalled from the SAX events.
     * @return the resulting object
     * @throws XMLMarshalException if an error occurred during unmarshalling
     */
    public Object getResult() {
        Document document = getDocument();

        if ((document == null) || !endDocumentTriggered) {
            throw XMLMarshalException.illegalStateXMLUnmarshallerHandler();
        }
        return xmlUnmarshaller.unmarshal(document);
    }
}