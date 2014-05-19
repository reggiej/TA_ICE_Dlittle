// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox;

import java.io.*;
import java.rmi.MarshalException;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.descriptors.Namespace;
import oracle.toplink.internal.ox.FragmentContentHandler;
import oracle.toplink.internal.ox.TreeObjectBuilder;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.ox.XMLObjectBuilder;
import oracle.toplink.internal.ox.XPathEngine;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.record.DOMReader;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.attachment.*;
import oracle.toplink.ox.record.ContentHandlerRecord;
import oracle.toplink.ox.record.FormattedWriterRecord;
import oracle.toplink.ox.record.MarshalRecord;
import oracle.toplink.ox.record.NodeRecord;
import oracle.toplink.ox.record.WriterRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.ox.schema.XMLSchemaReference;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformException;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.platform.xml.XMLTransformer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>Class used to marshal object to XML.
 *
 * <p>Create an XMLMarshaller from an XMLContext.<br>
 *  <em>Code Sample</em><br>
 *  <code>
 *  XMLContext context = new XMLContext("mySessionName");<br>
 *  XMLMarshaller marshaller = context.createMarshaller();<br>
 *  <code>
 *
 * <p>Objects can be marshalled to the following outputs:<ul>
 * <li>java.io.OutputStream</li>
 * <li>java.io.Writer</li>
 * <li>javax.xml.transform.Result</li>
 * <li>org.w3c.dom.Node</li>
 * <li>org.xml.sax.ContentHandler</li>
 * </ul>
 *
 * <p>Objects that can be marshalled are those which are mapped in the
 * TopLink project associated with the XMLContext, and which are mapped
 * to an XMLDescriptor that has a default root element specified.
 *
 * @see oracle.toplink.ox.XMLContext
 */
public class XMLMarshaller {
    private final static String DEFAULT_XML_ENCODING = "UTF-8";
    private final static String DEFAULT_XML_VERSION = "1.0";
    private String schemaLocation;
    private String noNamespaceSchemaLocation;
    private XMLTransformer transformer;
    private XMLContext xmlContext;
    private XMLMarshalListener marshalListener;
    private XMLAttachmentMarshaller attachmentMarshaller;
    private Properties marshalProperties;
    /**
    * Create a new XMLMarshaller based on the specified session
    * @param session A single session
    */
    public XMLMarshaller(XMLContext xmlContext) {
        setXMLContext(xmlContext);
        initialize();
    }

    private void initialize() {
        XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        transformer = xmlPlatform.newXMLTransformer();
        setEncoding(DEFAULT_XML_ENCODING);
        setFormattedOutput(true);
        marshalProperties = new Properties();
    }

    /**
     * Return the instance of XMLContext that was used to create this instance
     * of XMLMarshaller.
     */
    public XMLContext getXMLContext() {
        return xmlContext;
    }

    /** 
     * Set the XMLContext used by this instance of XMLMarshaller.
     */
    public void setXMLContext(XMLContext value) {
        xmlContext =  value;
    }    

    /**
    * Returns if this XMLMarshaller should format the XML
    * By default this is set to true and the XML marshalled will be formatted.
    * @return if this XMLMarshaller should format the XML
    */
    public boolean isFormattedOutput() {
        return transformer.isFormattedOutput();
    }

    /**
    * Set if this XMLMarshaller should format the XML
    * By default this is set to true and the XML marshalled will be formatted.
    * @param shouldFormat if this XMLMarshaller should format the XML
    */
    public void setFormattedOutput(boolean shouldFormat) {
        transformer.setFormattedOutput(shouldFormat);
    }

    /**
       * Get the encoding set on this XMLMarshaller
       * If the encoding has not been set the default UTF-8 will be used
       * @return the encoding set on this XMLMarshaller
       */
    public String getEncoding() {
        return transformer.getEncoding();
    }

    /**
       * Set the encoding on this XMLMarshaller
       * If the encoding is not set the default UTF-8 will be used
       * @param newEncoding the encoding to set on this XMLMarshaller
       */
    public void setEncoding(String newEncoding) {
        transformer.setEncoding(newEncoding);
    }

    /**
      * Get the schema location set on this XMLMarshaller
      * @return the schema location specified on this XMLMarshaller
      */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
      * Set the schema location on this XMLMarshaller
      * @param newSchemaLocation the schema location to be seton this XMLMarshaller
      */
    public void setSchemaLocation(String newSchemaLocation) {
        schemaLocation = newSchemaLocation;
    }

    /**
      * Get the no namespace schema location set on this XMLMarshaller
      * @return the no namespace schema location specified on this XMLMarshaller
      */
    public String getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }

    /**
     * Return a properties object for a given instance of the
     * XMLMarshaller.
     *
     * @return
     */
    public Properties getProperties() {
        return marshalProperties;
    }
    /**
     * Return the property for a given key, if one exists.
     *
     * @parm key
     * @return
     */
    public Object getProperty(Object key) {
        return marshalProperties.get(key);
    }
    /**
      * Set the no namespace schema location on this XMLMarshaller
      * @param newNoNamespaceSchemaLocation no namespace schema location to be seton this XMLMarshaller
      */
    public void setNoNamespaceSchemaLocation(String newNoNamespaceSchemaLocation) {
        noNamespaceSchemaLocation = newNoNamespaceSchemaLocation;
    }

    public void setXMLMarshalHandler(XMLMarshalListener marshalListener) {
        this.marshalListener = marshalListener;
    }

    public XMLMarshalListener getMarshalListener() {
        return this.marshalListener;
    }

    public void setMarshalListener(XMLMarshalListener listener) {
        this.marshalListener = listener;
    }

    /**
    * PUBLIC:
    * Convert the given object to XML and update the given result with that XML Document
    * @param object the object to marshal
    * @param result the result to marshal the object to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public void marshal(Object object, Result result) throws XMLMarshalException {
        if ((object == null) || (result == null)) {
            throw XMLMarshalException.nullArgumentException();
        }

        boolean isXMLRoot = (object instanceof XMLRoot);
        XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);

        AbstractSession session = xmlContext.getSession(xmlDescriptor);
        //if this is a simple xml root, the session and descriptor will be null
        if ((session == null) || !xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            if (result instanceof DOMResult) {
                DOMResult domResult = (DOMResult)result;
                marshal(object, domResult.getNode());
            } else if (result instanceof SAXResult) {
                SAXResult saxResult = (SAXResult)result;
                marshal(object, saxResult.getHandler());
            } else if (result instanceof StreamResult) {
                StreamResult streamResult = (StreamResult)result;
                Writer writer = streamResult.getWriter();
                if (null == writer) {
                    marshal(object, streamResult.getOutputStream());
                } else {
                    marshal(object, writer);
                }
            } else {
            	java.io.StringWriter writer = new java.io.StringWriter();
            	marshal(object, writer);
            	System.out.println(writer.toString());
            	javax.xml.transform.stream.StreamSource source = new javax.xml.transform.stream.StreamSource(new java.io.StringReader(writer.toString()));
            	transformer.transform(source, result);
            }
            return;
        }
        try {
            Document document = objectToXML(object, xmlDescriptor, isXMLRoot);
            if ((result instanceof SAXResult) && (isFragment())) {
                FragmentContentHandler fragmentHandler = new FragmentContentHandler(((SAXResult)result).getHandler());
                if(isXMLRoot) {
                    String oldEncoding = transformer.getEncoding();
                    String oldVersion = transformer.getVersion();
                    if(((XMLRoot)object).getEncoding() != null) {
                        transformer.setEncoding(((XMLRoot)object).getEncoding());
                    }
                    if(((XMLRoot)object).getXMLVersion() != null) {
                        transformer.setVersion(((XMLRoot)object).getXMLVersion());
                    }
                    transformer.transform(document, fragmentHandler);
                    transformer.setEncoding(oldEncoding);
                    transformer.setVersion(oldVersion);
                } else {
                    transformer.transform(document, fragmentHandler);
                    
                }   
              } else {
                  if(isXMLRoot) {
                      String oldEncoding = transformer.getEncoding();
                      String oldVersion = transformer.getVersion();
                      if(((XMLRoot)object).getEncoding() != null) {
                          transformer.setEncoding(((XMLRoot)object).getEncoding());
                      }
                      if(((XMLRoot)object).getXMLVersion() != null) {
                          transformer.setVersion(((XMLRoot)object).getXMLVersion());
                      }
                      transformer.transform(document, result);
                      transformer.setEncoding(oldEncoding);
                      transformer.setVersion(oldVersion);
                  } else {
                      transformer.transform(document, result);
                  }            
              }
        } catch (XMLPlatformException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
    * PUBLIC:
    * Convert the given object to XML and update the given outputStream with that XML Document
    * @param object the object to marshal
    * @param outputStream the outputStream to marshal the object to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public void marshal(Object object, OutputStream outputStream) throws XMLMarshalException {
        if ((object == null) || (outputStream == null)) {
            throw XMLMarshalException.nullArgumentException();
        }
        try {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, getEncoding());
            marshal(object, writer);
            writer.flush();
        } catch (UnsupportedEncodingException exception) {
            throw XMLMarshalException.marshalException(exception);
        } catch (Exception ex) {
            throw XMLMarshalException.marshalException(ex);
        }
    }

    /**
    * PUBLIC:
    * Convert the given object to XML and update the given writer with that XML Document
    * @param object the object to marshal
    * @param writer the writer to marshal the object to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public void marshal(Object object, Writer writer) throws XMLMarshalException {
        if ((object == null) || (writer == null)) {
            throw XMLMarshalException.nullArgumentException();
        }
        boolean isXMLRoot = false;
        String version = DEFAULT_XML_VERSION;
        String encoding = getEncoding();
        if (object instanceof XMLRoot) {
            isXMLRoot = true;
            XMLRoot xroot = (XMLRoot) object;
            version  = xroot.getXMLVersion()  != null ? xroot.getXMLVersion()  : version;
            encoding = xroot.getEncoding() != null ? xroot.getEncoding() : encoding;
        }

        XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);
        AbstractSession session = xmlContext.getSession(xmlDescriptor);
        
        WriterRecord writerRecord;
        if (isFormattedOutput()) {
            writerRecord = new FormattedWriterRecord();
        } else {
            writerRecord = new WriterRecord();
        }
        writerRecord.setMarshaller(this);
        writerRecord.setWriter(writer);
        
        //if this is a simple xml root, the session and descriptor will be null
        if (session == null || !xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            marshal(object, writerRecord, xmlDescriptor, isXMLRoot);
            try {
                writer.flush();
            } catch (IOException e) {
                throw XMLMarshalException.marshalException(e);
            }
            return;
        }

        try {
            Node xmlDocument = objectToXMLNode(object, xmlDescriptor, isXMLRoot);
            if (isFragment()) {
                writerRecord.node(xmlDocument, xmlDescriptor.getNamespaceResolver());
            } else {
	        writerRecord.startDocument(encoding, version);
	        writerRecord.node(xmlDocument, writerRecord.getNamespaceResolver());
	        writerRecord.endDocument();
            }        
        } catch (XMLPlatformException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
    * PUBLIC:
    * Convert the given object to XML and update the given contentHandler with that XML Document
    * @param object the object to marshal
    * @param contentHandler the contentHandler which the specified object should be marshalled to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public void marshal(Object object, ContentHandler contentHandler) throws XMLMarshalException {
        marshal(object, contentHandler, null);
    }

    /**
     * PUBLIC:
     * Convert the given object to XML and update the given contentHandler with that XML Document
     * @param object the object to marshal
     * @param contentHandler the contentHandler which the specified object should be marshalled to
     * @throws XMLMarshalException if an error occurred during marshalling
     */
    public void marshal(Object object, ContentHandler contentHandler, LexicalHandler lexicalHandler) throws XMLMarshalException {
        if ((object == null) || (contentHandler == null)) {
            throw XMLMarshalException.nullArgumentException();
        }

        boolean isXMLRoot = (object instanceof XMLRoot);
        XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);

        AbstractSession session = xmlContext.getSession(xmlDescriptor);

        //if it's a simple xml root then session and descriptor will be null
        if ((session == null) || !xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            ContentHandlerRecord contentHandlerRecord = new ContentHandlerRecord();
            contentHandlerRecord.setMarshaller(this);
            contentHandlerRecord.setContentHandler(contentHandler);
            contentHandlerRecord.setLexicalHandler(lexicalHandler);
            marshal(object, contentHandlerRecord, xmlDescriptor, isXMLRoot);
            return;
        }

        try {
            Document xmlDocument = objectToXML(object, xmlDescriptor, isXMLRoot);
            DOMReader reader = new DOMReader();
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler);
            if (isFragment()) {
                FragmentContentHandler fragmentHandler = new FragmentContentHandler(contentHandler);
                reader.setContentHandler(fragmentHandler);
            } else {
                reader.setContentHandler(contentHandler);
            }
            reader.parse(xmlDocument);
        } catch (XMLPlatformException e) {
            throw XMLMarshalException.marshalException(e);
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            //won't be thrown
        } catch (SAXNotSupportedException e) {
            //won't be thrown
        } catch (org.xml.sax.SAXException e) {
            throw XMLMarshalException.marshalException(e);
        }
    }

    /**
    * PUBLIC:
    * Convert the given object to XML and update the given node with that XML Document
    * @param object the object to marshal
    * @param node the node which the specified object should be marshalled to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public void marshal(Object object, Node node) throws XMLMarshalException {
        if ((object == null) || (node == null)) {
            throw XMLMarshalException.nullArgumentException();
        }
        try {
            boolean isXMLRoot = (object instanceof XMLRoot);
            XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);

            AbstractSession session = xmlContext.getSession(xmlDescriptor);
            //if this is a simple xml root, descriptor and session will be null
            if ((session == null) || !xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
                NodeRecord nodeRecord = new NodeRecord();
                nodeRecord.setMarshaller(this);
                nodeRecord.setDOM(node);

                if (!isXMLRoot) {
                    if ((null == xmlDescriptor.getDefaultRootElement()) && (node.getNodeType() == Node.ELEMENT_NODE) && (xmlDescriptor.getSchemaReference() != null) && (xmlDescriptor.getSchemaReference().getType() == XMLSchemaReference.COMPLEX_TYPE)) {
                        Attr typeAttr = ((Element)node).getAttributeNodeNS(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE);
                        if (typeAttr == null) {
                            NamespaceResolver namespaceResolver = xmlDescriptor.getNamespaceResolver();
                            String xsiPrefix = null;
                            if (null == namespaceResolver) {
                                namespaceResolver = new NamespaceResolver();
                                xmlDescriptor.setNamespaceResolver(namespaceResolver);
                            } else {
                                xsiPrefix = namespaceResolver.resolveNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
                            }

                            if (null == xsiPrefix) {
                                xsiPrefix = namespaceResolver.generatePrefix(XMLConstants.SCHEMA_INSTANCE_PREFIX);
                            }

                            String value = xmlDescriptor.getSchemaReference().getSchemaContext();

                            ((Element)node).setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + xsiPrefix, XMLConstants.SCHEMA_INSTANCE_URL);
                            ((Element)node).setAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, xsiPrefix + ":" + XMLConstants.SCHEMA_TYPE_ATTRIBUTE, value);

                        } else {
                            String value = xmlDescriptor.getSchemaReference().getSchemaContext();
                            typeAttr.setValue(value);
                        }
                    }
                }
                marshal(object, nodeRecord, xmlDescriptor, isXMLRoot);
                return;
            }

            //If preserving document, may return the cached doc. Need to 
            //Copy contents of the cached doc to the supplied node. 
            Node doc = objectToXMLNode(object, node, xmlDescriptor, isXMLRoot);
            DOMResult result = new DOMResult(node);
            if(isXMLRoot) {
                String oldEncoding = transformer.getEncoding();
                String oldVersion = transformer.getVersion();
                if(((XMLRoot)object).getEncoding() != null) {
                    transformer.setEncoding(((XMLRoot)object).getEncoding());
                }
                if(((XMLRoot)object).getXMLVersion() != null) {
                    transformer.setVersion(((XMLRoot)object).getXMLVersion());
                }
                transformer.transform(doc, result);
                transformer.setEncoding(oldEncoding);
                transformer.setVersion(oldVersion);
            } else {
                transformer.transform(doc, result);
            }
        } catch (Exception exception) {
            if (exception instanceof XMLMarshalException) {
                throw (XMLMarshalException)exception;
            } else {
                throw XMLMarshalException.marshalException(exception);
            }
        }
    }

    /**
     * Convert the given object to XML and update the given marshal record with
     * that XML Document.
     * @param object the object to marshal
     * @param marshalRecord the marshalRecord to marshal the object to
     */
    public void marshal(Object object, MarshalRecord marshalRecord) {
        boolean isXMLRoot = (object instanceof XMLRoot);
        XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);
        marshal(object, marshalRecord, xmlDescriptor, isXMLRoot);
    }

    /**
     * Convert the given object to XML and update the given marshal record with
     * that XML Document.
     * @param object the object to marshal
     * @param marshalRecord the marshalRecord to marshal the object to
     * @param descriptor the XMLDescriptor for the object being marshalled
     */
    private void marshal(Object object, MarshalRecord marshalRecord, XMLDescriptor descriptor, boolean isXMLRoot) {
        addDescriptorNamespacesToXMLRecord(descriptor, marshalRecord);        
        NamespaceResolver nr = marshalRecord.getNamespaceResolver();
        
        if (getMarshalListener() != null) {
            getMarshalListener().beforeMarshal(object);
        }
        if (!isFragment()) {
            String encoding = getEncoding();
            String version = DEFAULT_XML_VERSION;
            if (!isXMLRoot) {
                marshalRecord.setLeafElementType(descriptor.getDefaultRootElementType());
            } else {
                XMLRoot root = (XMLRoot)object;
                if(root.getEncoding() != null) {
                    encoding = root.getEncoding();
                }
                if(root.getXMLVersion() != null) {
                    version = root.getXMLVersion();
                }
            }
            marshalRecord.startDocument(encoding, version);
        }
        XPathFragment rootFragment = buildRootFragment(object, descriptor, isXMLRoot, marshalRecord);        

        boolean shouldWriteTypeAttribute = shouldWriteTypeAttribute(object, descriptor, isXMLRoot);

        String schemaLocation = getSchemaLocation();
        String noNsSchemaLocation = getNoNamespaceSchemaLocation();
        if (isXMLRoot) {
            XMLRoot root = (XMLRoot)object;
            object = root.getObject();
            if(root.getSchemaLocation() != null) {
                schemaLocation = root.getSchemaLocation();
            }
            if(root.getNoNamespaceSchemaLocation() != null) {
                noNsSchemaLocation = root.getNoNamespaceSchemaLocation();
            }
        }

        String xsiPrefix = null;
        if ((null != getSchemaLocation()) || (null != getNoNamespaceSchemaLocation()) || shouldWriteTypeAttribute) {
            xsiPrefix = nr.resolveNamespaceURI(XMLConstants.SCHEMA_INSTANCE_URL);
            if (null == xsiPrefix) {
                xsiPrefix = XMLConstants.SCHEMA_INSTANCE_PREFIX;
                nr.put(XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);
            }
        }
        TreeObjectBuilder treeObjectBuilder = null;
        AbstractSession session = null;
        if (descriptor != null) {
            session = xmlContext.getSession(object);
            treeObjectBuilder = (TreeObjectBuilder)descriptor.getObjectBuilder();
        }
        
        if (null != rootFragment) {
            marshalRecord.startPrefixMappings(nr);
            if(!isXMLRoot && descriptor.getNamespaceResolver() == null && rootFragment.hasNamespace()){
              // throw an exception if the name has a : in it but the namespaceresolver is null
              throw XMLMarshalException.namespaceResolverNotSpecified(rootFragment.getShortName());
            }
            marshalRecord.openStartElement(rootFragment, nr);
            if (null != schemaLocation) {
                marshalRecord.attribute(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_LOCATION, xsiPrefix + ":" + XMLConstants.SCHEMA_LOCATION, schemaLocation);
            }
            if (null != noNsSchemaLocation) {
                marshalRecord.attribute(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.NO_NS_SCHEMA_LOCATION, xsiPrefix + ":" + XMLConstants.NO_NS_SCHEMA_LOCATION, noNsSchemaLocation);
            }

            if (descriptor != null) {
                if (shouldWriteTypeAttribute) {
                    writeTypeAttribute(marshalRecord, descriptor, xsiPrefix);
                }
                treeObjectBuilder.marshalAttributes(marshalRecord, object, session);
            }

            marshalRecord.namespaceDeclarations(nr);
            marshalRecord.closeStartElement();
        }        
        if (treeObjectBuilder != null) {
            treeObjectBuilder.buildRow(marshalRecord, object, (AbstractSession)session, this);
        } else if (isXMLRoot) {
            String value = (String)XMLConversionManager.getDefaultXMLManager().convertObject(object, String.class);
            marshalRecord.characters(value);
        }

        if (null != rootFragment) {
            marshalRecord.endElement(rootFragment, nr);
            marshalRecord.endPrefixMappings(nr);
        }
        if (!isFragment()) {
            marshalRecord.endDocument();
        }
        if (getMarshalListener() != null) {
            getMarshalListener().afterMarshal(object);
        }
    }

    private XPathFragment buildRootFragment(Object object, XMLDescriptor descriptor, boolean isXMLRoot,MarshalRecord marshalRecord) {
        XPathFragment rootFragment = null;
        if (isXMLRoot) {
            rootFragment = ((XMLRoot)object).getRootFragment();
            String xmlRootUri = ((XMLRoot)object).getNamespaceURI();
            String xmlRootLocalName = ((XMLRoot)object).getLocalName();
            if (xmlRootUri != null) {
                if (descriptor != null) {
                    String xmlRootPrefix = marshalRecord.getNamespaceResolver().resolveNamespaceURI(xmlRootUri);
                    if (xmlRootPrefix == null) {
                        xmlRootPrefix = marshalRecord.getNamespaceResolver().generatePrefix();
                        marshalRecord.getNamespaceResolver().put(xmlRootPrefix, xmlRootUri);
                    }
                    rootFragment.setXPath(xmlRootPrefix + ":" + xmlRootLocalName);
                } else {
                    String xmlRootPrefix = "ns0";
                    marshalRecord.getNamespaceResolver().put(xmlRootPrefix, xmlRootUri);                    
                    rootFragment.setXPath(xmlRootPrefix + ":" + xmlRootLocalName);
                }
            }
        } else {
            String rootName = descriptor.getDefaultRootElement();
            if (null != rootName) {
                rootFragment = new XPathFragment(rootName);
                NamespaceResolver descriptorNamespaceResolver = descriptor.getNamespaceResolver();
                if(rootFragment.getPrefix() != null && (rootFragment.getNamespaceURI() == null)) {
                    String uri = null;
                    if(null == descriptorNamespaceResolver) {
                        throw XMLMarshalException.namespaceResolverNotSpecified(null);
                    }
                    uri = descriptor.getNamespaceResolver().resolveNamespacePrefix(rootFragment.getPrefix());
                
                    if(uri == null) {
                        throw XMLMarshalException.namespaceNotFound(rootFragment.getPrefix());
                    }
                    rootFragment.setNamespaceURI(uri);
                } else {
                    if(descriptorNamespaceResolver != null) {
                        rootFragment.setNamespaceURI(descriptorNamespaceResolver.getDefaultNamespaceURI());
                    }
                }
            }
        }
        return rootFragment;
    }   

    private void writeTypeAttribute(MarshalRecord marshalRecord, XMLDescriptor descriptor, String xsiPrefix) {
        //xsi:type=schemacontext               
        String typeValue = descriptor.getSchemaReference().getSchemaContext();
        typeValue = typeValue.substring(1);
        marshalRecord.attribute(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE, xsiPrefix + ":" + XMLConstants.SCHEMA_TYPE_ATTRIBUTE, typeValue);
    }

    private boolean isSimpleXMLRoot(XMLRoot xmlRoot) {
        Class xmlRootObjectClass = xmlRoot.getObject().getClass();
        if (XMLConversionManager.getDefaultXMLManager().getDefaultJavaTypes().get(xmlRootObjectClass) != null) {
            return true;
        }
        return false;
    }

    /**
    * PUBLIC:
    * Convert the given object to an XML Document
    * @param object the object to marshal
    * @return the document which the specified object has been marshalled to
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    public Document objectToXML(Object object) throws XMLMarshalException {
        boolean isXMLRoot = (object instanceof XMLRoot);
        XMLDescriptor xmlDescriptor = getDescriptor(object, isXMLRoot);
        return objectToXML(object, xmlDescriptor, isXMLRoot);
    }

    /**
    * INTERNAL:
    * Convert the given object to an XML Document
    * @param object the object to marshal
    * @return the document which the specified object has been marshalled to
    * @param descriptor the XMLDescriptor for the object being marshalled
    * @throws XMLMarshalException if an error occurred during marshalling
    */
    protected Document objectToXML(Object object, XMLDescriptor descriptor, boolean isXMLRoot) throws XMLMarshalException {
        AbstractSession session = xmlContext.getSession(descriptor);
        if ((session != null) && xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            XMLRecord xmlRow = null;
            if (!isXMLRoot) {
                xmlRow = (XMLRecord)((XMLObjectBuilder)descriptor.getObjectBuilder()).createRecordFor(object, xmlContext.getDocumentPreservationPolicy(session));
                xmlRow.setMarshaller(this);
                addDescriptorNamespacesToXMLRecord(descriptor, xmlRow);
            }
            return objectToXML(object, descriptor, xmlRow, isXMLRoot);
        } else {
            MarshalRecord marshalRecord = new NodeRecord();
            marshalRecord.setMarshaller(this);
            marshal(object, marshalRecord, descriptor, isXMLRoot);
            return marshalRecord.getDocument();
        }
    }

    /**
     * INTERNAL:
     * Like ObjectToXML but is may also return a document fragment instead of a document in the
     * case of a non-root object.
     */
    protected Node objectToXMLNode(Object object, XMLDescriptor descriptor, boolean isXMLRoot) throws XMLMarshalException {
        return objectToXMLNode(object, null, descriptor, isXMLRoot);
    }

    protected Node objectToXMLNode(Object object, Node rootNode, XMLDescriptor descriptor, boolean isXMLRoot) throws XMLMarshalException {
        AbstractSession session = xmlContext.getSession(descriptor);
        if ((session != null) && xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            XMLRecord xmlRow = null;
            if (!isXMLRoot) {
                xmlRow = (XMLRecord)((XMLObjectBuilder)descriptor.getObjectBuilder()).createRecordFor(object, xmlContext.getDocumentPreservationPolicy(session));
                xmlRow.setMarshaller(this);
                if (xmlRow.getDOM().getNodeType() == Node.ELEMENT_NODE) {
                    addDescriptorNamespacesToXMLRecord(descriptor, xmlRow);
                }
            }
            Document doc = objectToXML(object, rootNode, descriptor, xmlRow, isXMLRoot);
            if ((xmlRow != null) && (xmlRow.getDOM().getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)) {
                return xmlRow.getDOM();
            } else {
                return doc;
            }
        } else {
            MarshalRecord marshalRecord = new NodeRecord();
            marshalRecord.setMarshaller(this);
            marshalRecord.getNamespaceResolver().setDOM(rootNode);
            marshal(object, marshalRecord, descriptor, isXMLRoot);
            return marshalRecord.getDocument();
        }
    }

    private void addDescriptorNamespacesToXMLRecord(XMLDescriptor xmlDescriptor, XMLRecord record) {
        if(null == xmlDescriptor) {
            return;
        }
        copyNamespaces(xmlDescriptor.getNamespaceResolver(), record.getNamespaceResolver());
    }
    
    private void copyNamespaces(NamespaceResolver source, NamespaceResolver target) {
        if(null != source && null != target) {
            List namespaces = source.getNamespaces();
            for (int i = 0; i < namespaces.size(); i++) {
                Namespace next = (Namespace)namespaces.get(i);
                target.put(next.getPrefix(), next.getNamespaceURI());
            }
            target.setDefaultNamespaceURI(source.getDefaultNamespaceURI());
        }
    }

    /**
    * PUBLIC:
    * Convert the given object to descendants of the parent element
    * @param object the object to marshal
    * @param parent the node to marshal the object to
    * @return the document which the specified object has been marshalled to
    * @throws XMLMarshalException if an error occurred during marshalling
    * @deprecated
    */
    public Document objectToXML(Object object, Node parent) throws XMLMarshalException {
        boolean isXMLRoot = (object instanceof XMLRoot);
        XMLDescriptor descriptor = getDescriptor(object, isXMLRoot);

        String localRootName = descriptor.getDefaultRootElement();
        if (null == localRootName) {
            throw XMLMarshalException.defaultRootElementNotSpecified(descriptor);
        }

        AbstractSession session = xmlContext.getSession(descriptor);
        if ((session != null) && xmlContext.getDocumentPreservationPolicy(session).shouldPreserveDocument()) {
            XMLRecord xmlRow = (XMLRecord)((XMLObjectBuilder)descriptor.getObjectBuilder()).createRecord(localRootName, parent);
            xmlRow.setMarshaller(this);
            return objectToXML(object, descriptor, xmlRow, isXMLRoot);
        } else {
            MarshalRecord marshalRecord = new NodeRecord(localRootName, parent);
            marshalRecord.setMarshaller(this);
            marshal(object, marshalRecord, descriptor, isXMLRoot);
            return marshalRecord.getDocument();
        }
    }

    /**
    * INTERNAL:
    * Convert the given object to an XML Document
    */
    public Document objectToXML(Object object, XMLDescriptor descriptor, XMLRecord xmlRow, boolean isXMLRoot) {
        return objectToXML(object, null, descriptor, xmlRow, isXMLRoot);
    }
    
    
    private Document objectToXML(Object object, Node rootNode, XMLDescriptor descriptor, XMLRecord xmlRow, boolean isXMLRoot) {
        Document document = null;
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setDOM(rootNode);
        this.copyNamespaces(descriptor.getNamespaceResolver(), resolver);
        boolean shouldCallSetAttributeNS = false;
        boolean isRootDocumentFragment = false;
        if (xmlRow != null) {
            isRootDocumentFragment = (xmlRow.getDOM().getNodeType() == Node.DOCUMENT_FRAGMENT_NODE);
        }
        if (isXMLRoot) {
            String xmlRootUri = ((XMLRoot)object).getNamespaceURI();
            String xmlRootPrefix = null;
            if (xmlRow == null) {
                String recordName = ((XMLRoot)object).getLocalName();
                if (xmlRootUri != null) {
                    xmlRootPrefix = resolver.resolveNamespaceURI(xmlRootUri);
                    if (xmlRootPrefix == null) {
                        xmlRootPrefix = resolver.generatePrefix();
                        resolver.put(xmlRootPrefix, xmlRootUri);
                        shouldCallSetAttributeNS = true;
                    }
                    recordName = xmlRootPrefix + ":" + recordName;
                }
                xmlRow = (XMLRecord)((XMLObjectBuilder)descriptor.getObjectBuilder()).createRecordFor(((XMLRoot)object).getObject(), xmlContext.getDocumentPreservationPolicy(xmlContext.getSession(descriptor)), recordName, xmlRootUri);
                xmlRow.setMarshaller(this);
                if (!isRootDocumentFragment) {
                    if (shouldCallSetAttributeNS) {
                        if (xmlRootPrefix != null) {
                            ((Element)xmlRow.getDOM()).setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + xmlRootPrefix, xmlRootUri);
                        }
                        shouldCallSetAttributeNS = false;
                    }
                }
            }

            copyNamespaces(resolver, xmlRow.getNamespaceResolver());
            document = xmlRow.getDocument();
            Element docElement = document.getDocumentElement();
            xmlRow.getNamespaceResolver().put(XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);

            boolean writeTypeAttribute = shouldWriteTypeAttribute(object, descriptor, isXMLRoot);
            if (writeTypeAttribute && (descriptor.getSchemaReference() != null) && (descriptor.getSchemaReference().getSchemaContext() != null)) {
                ((Element)xmlRow.getDOM()).setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);
                String typeValue = descriptor.getSchemaReference().getSchemaContext();
                typeValue = typeValue.substring(1);

                // xsi:type="typevalue"
                XMLField xsiTypefield = new XMLField("@xsi:type");                
                xsiTypefield.setNamespaceResolver(xmlRow.getNamespaceResolver());
                XPathEngine.getInstance().create(xsiTypefield, docElement, typeValue);

            }
            object = ((XMLRoot)object).getObject();
        }

        XMLObjectBuilder bldr = (XMLObjectBuilder)descriptor.getObjectBuilder();
        xmlRow = (XMLRecord)bldr.buildRow(xmlRow, object, (AbstractSession)xmlContext.getSession(object), isXMLRoot);
        xmlRow.setMarshaller(this);
        if (shouldCallSetAttributeNS && !isRootDocumentFragment) {
            ((Element)xmlRow.getDOM()).setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);
        }
        document = xmlRow.getDocument();

        addSchemaLocations(document);
        return document;
    }

    private void addSchemaLocations(Document document) {
        Element docElement = document.getDocumentElement();

        NamespaceResolver resolver = new NamespaceResolver();
        resolver.put(XMLConstants.XMLNS, XMLConstants.XMLNS_URL);
        resolver.put(XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);

        if ((getSchemaLocation() != null) || (getNoNamespaceSchemaLocation() != null)) {
            XMLField field = new XMLField("@xmlns:xsi");
            field.setNamespaceResolver(resolver);
            XPathEngine.getInstance().create(field, docElement, XMLConstants.SCHEMA_INSTANCE_URL);
        }
        if (getSchemaLocation() != null) {
            XMLField field = new XMLField("@xsi:" + XMLConstants.SCHEMA_LOCATION);
            field.setNamespaceResolver(resolver);
            XPathEngine.getInstance().create(field, docElement, getSchemaLocation());
        }
        if (getNoNamespaceSchemaLocation() != null) {
            XMLField field = new XMLField("@xsi:" + XMLConstants.NO_NS_SCHEMA_LOCATION);
            field.setNamespaceResolver(resolver);
            XPathEngine.getInstance().create(field, docElement, getNoNamespaceSchemaLocation());
        }
    }

    /**
     * INTERNAL:
     * return if an xsi:type attribute should be added for the given XMLRootObject
     */
    public boolean shouldWriteTypeAttribute(Object object, XMLDescriptor descriptor, boolean isXMLRoot) {
        boolean writeTypeAttribute = false;

        if (isXMLRoot && (descriptor != null)) {
            if (descriptor.hasInheritance()) {
                XMLField classIndicatorField = (XMLField)descriptor.getInheritancePolicy().getClassIndicatorField();
                String classIndicatorUri = null;
                String classIndicatorLocalName = classIndicatorField.getXPathFragment().getLocalName();
                String classIndicatorPrefix = classIndicatorField.getXPathFragment().getPrefix();
                if (classIndicatorPrefix != null) {
                    classIndicatorUri = descriptor.getNamespaceResolver().resolveNamespacePrefix(classIndicatorPrefix);
                }

                if ((classIndicatorLocalName != null) && classIndicatorLocalName.equals(XMLConstants.SCHEMA_TYPE_ATTRIBUTE) && (classIndicatorUri != null) && classIndicatorUri.equals(XMLConstants.SCHEMA_INSTANCE_URL)) {
                    return false;
                }
            }
            String xmlRootLocalName = ((XMLRoot)object).getLocalName();
            String xmlRootUri = ((XMLRoot)object).getNamespaceURI();

            writeTypeAttribute = true;
            if(descriptor.getSchemaReference() == null){
              return false;
            }
            for (int i = 0; i < descriptor.getTableNames().size(); i++) {
                if (!writeTypeAttribute) {
                    break;
                }
                String defaultRootQualifiedName = (String)descriptor.getTableNames().get(i);
                if (defaultRootQualifiedName != null) {
                    String defaultRootLocalName = null;
                    String defaultRootUri = null;

                    int colonIndex = defaultRootQualifiedName.indexOf(':');
                    if (colonIndex > 0) {
                        String defaultRootPrefix = defaultRootQualifiedName.substring(0, colonIndex);

                        //use this prefix to declare xmlns 
                        //xmlns=xmlRootUri
                        //xmlns:xsi  
                        defaultRootLocalName = defaultRootQualifiedName.substring(colonIndex + 1);
                        if (descriptor.getNamespaceResolver() != null) {
                            defaultRootUri = descriptor.getNamespaceResolver().resolveNamespacePrefix(defaultRootPrefix);
                        }
                    } else {
                        defaultRootLocalName = defaultRootQualifiedName;
                    }

                    if (xmlRootLocalName != null) {
                        if ((((defaultRootLocalName == null) && (xmlRootLocalName == null)) || (defaultRootLocalName.equals(xmlRootLocalName))) && (((defaultRootUri == null) && (xmlRootUri == null)) || ((xmlRootUri != null) && (defaultRootUri != null) && (defaultRootUri.equals(xmlRootUri))))) {
                            //if both local name and uris are equal then don't need to write type attribute
                            writeTypeAttribute = false;
                        }
                    } else {
                        //this means there was a default root element
                        //but xmlRoot.getName was null
                        //writeTypeAttribute = true
                    }
                } else {
                    //then no default rootElement was set
                    //if xmlRootName = null then writeTypeAttribute = false
                    //not really valid because xmlRootLocalName shouldn't ever be null
                    if (xmlRootLocalName == null) {
                        writeTypeAttribute = false;
                    }
                }
            }
        }
        return writeTypeAttribute;
    }

    /**
    * INTERNAL:
    * Return the descriptor for the root object.
    */
    protected XMLDescriptor getDescriptor(Object object) throws XMLMarshalException {
        XMLDescriptor descriptor = (XMLDescriptor)xmlContext.getSession(object).getDescriptor(object);
        if (descriptor == null) {
            throw XMLMarshalException.descriptorNotFoundInProject(object.getClass().getName());
        }

        return descriptor;
    }

    protected XMLDescriptor getDescriptor(Object object, boolean isXMLRoot) {
        if (isXMLRoot) {
            return getDescriptor((XMLRoot)object);
        } else {
            return getDescriptor(object);
        }
    }

    protected XMLDescriptor getDescriptor(XMLRoot object) throws XMLMarshalException {
        XMLDescriptor descriptor = null;

        try {
            descriptor = (XMLDescriptor)xmlContext.getSession(((XMLRoot)object).getObject()).getDescriptor(((XMLRoot)object).getObject());
        } catch (XMLMarshalException marshalException) {
            if ((descriptor == null) && isSimpleXMLRoot((XMLRoot)object)) {
                return null;
            }
            throw marshalException;
        }

        if (descriptor == null) {
            throw XMLMarshalException.descriptorNotFoundInProject(object.getClass().getName());
        }

        return descriptor;
    }

    /**
    * PUBLIC:
    * Set if this should marshal to a fragment.  If true an XML header string is not written out.
    * @param fragment if this should marshal to a fragment or not
    */
    public void setFragment(boolean fragment) {
        transformer.setFragment(fragment);
    }

    /**
    * PUBLIC:
    * Returns if this should marshal to a fragment.  If true an XML header string is not written out.
    * @return if this should marshal to a fragment or not
    */
    public boolean isFragment() {
        return transformer.isFragment();
    }

    public void setAttachmentMarshaller(XMLAttachmentMarshaller atm) {
        this.attachmentMarshaller = atm;
    }
    public XMLAttachmentMarshaller getAttachmentMarshaller() {
        return this.attachmentMarshaller;
    }
    /**
     * INTERNAL
     * @return the transformer instance for this marshaller
     */
    public XMLTransformer getTransformer() {
        return transformer;
    }
}
