// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml.jaxp;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import oracle.toplink.platform.xml.XMLPlatformException;
import oracle.toplink.platform.xml.XMLTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * <p><b>Purpose</b>:  An implementation of XMLTransformer using JAXP 1.3 APIs.</p>
 */

public class JAXPTransformer implements XMLTransformer {
    private boolean fragment;
    private static final String NO = "no";
    private static final String YES = "yes";
    private Transformer transformer;

    public JAXPTransformer() {
        super();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public String getEncoding() {
        return transformer.getOutputProperty(OutputKeys.ENCODING);
    }

    public void setEncoding(String encoding) {
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
    }

    public boolean isFormattedOutput() {
        return transformer.getOutputProperty(OutputKeys.INDENT).equals(YES);
    }

    public void setFormattedOutput(boolean shouldFormat) {
        if (shouldFormat) {
            transformer.setOutputProperty(OutputKeys.INDENT, YES);
        } else {
            transformer.setOutputProperty(OutputKeys.INDENT, NO);
        }
    }

    public String getVersion() {
        return transformer.getOutputProperty(OutputKeys.VERSION);
    }

    public void setVersion(String version) {
        transformer.setOutputProperty(OutputKeys.VERSION, version);
    }

    public void transform(Node sourceNode, OutputStream resultOutputStream) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        StreamResult result = new StreamResult(resultOutputStream);
        if (isFragment()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transform(source, result);
    }

    public void transform(Node sourceNode, ContentHandler resultContentHandler) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        SAXResult result = new SAXResult(resultContentHandler);

        transform(source, result);
    }

    public void transform(Node sourceNode, Result result) throws XMLPlatformException {
        DOMSource source = null;
        if ((isFragment()) && (result instanceof SAXResult)) {
            if (sourceNode instanceof Document) {
                source = new DOMSource(((Document)sourceNode).getDocumentElement());
            }
        } else {
            source = new DOMSource(sourceNode);
        }
        transform(source, result);
    }

    public void transform(Node sourceNode, Writer resultWriter) throws XMLPlatformException {
        DOMSource source = new DOMSource(sourceNode);
        StreamResult result = new StreamResult(resultWriter);

        if (isFragment()) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transform(source, result);
    }

    public void transform(Source source, Result result) throws XMLPlatformException {
        try {
            if ((result instanceof StreamResult) && (isFragment())) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public void transform(Document sourceDocument, Node resultParentNode, URL stylesheet) throws XMLPlatformException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            StreamSource stylesheetSource = new StreamSource(stylesheet.openStream());
            Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
            DOMSource source = new DOMSource(sourceDocument);
            DOMResult result = new DOMResult(resultParentNode);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw XMLPlatformException.xmlPlatformTransformException(e);
        }
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public boolean isFragment() {
        return fragment;
    }
}