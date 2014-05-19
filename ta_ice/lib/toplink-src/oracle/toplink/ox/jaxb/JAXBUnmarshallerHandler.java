// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.platform.xml.SAXDocumentBuilder;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

public class JAXBUnmarshallerHandler extends SAXDocumentBuilder implements UnmarshallerHandler {
    private XMLUnmarshaller xmlUnmarshaller;
    private boolean endDocumentTriggered;

    public JAXBUnmarshallerHandler(XMLUnmarshaller newXMLUnmarshaller) {
        super();
        xmlUnmarshaller = newXMLUnmarshaller;
    }

    public void endDocument() throws SAXException {
        endDocumentTriggered = true;
        super.endDocument();
    }

    public void startDocument() throws SAXException {
        endDocumentTriggered = false;
        super.startDocument();
    }

    public Object getResult() throws JAXBException, IllegalStateException {
        Document document = getDocument();

        if ((document == null) || !endDocumentTriggered) {
            throw new IllegalStateException();
        }
        return xmlUnmarshaller.unmarshal(document);
    }
}