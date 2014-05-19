// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import java.io.*;
import java.net.*;// only for testing in main method
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Hashtable;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import oracle.toplink.exceptions.EJBJarXMLException;
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformFactory;

/**
 * INTERNAL:
 * This class is designed to handle the simple
 * parsing of an XML file.
 *
 * An example of how to call this is:
 * <PRE>
 *     doc = XMLParser.parseXML("ejb-jar.xml");
 * </PRE>
 */
public class XMLParser {
    private static boolean allowNonEjb_2_0_DocType = false;

    //XML schema support
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    /** Validation mode for parsing */
    public static final int OLD_VALIDATION = -2;
    public static final int NONVALIDATING = oracle.toplink.platform.xml.XMLParser.NONVALIDATING;
    public static final int SCHEMA_VALIDATION = oracle.toplink.platform.xml.XMLParser.SCHEMA_VALIDATION;
    public static final int DTD_VALIDATION = oracle.toplink.platform.xml.XMLParser.DTD_VALIDATION;

    public static void allowNonEjb_2_0_DocType() {
        allowNonEjb_2_0_DocType = true;
    }

    public static void doNotAllowNonEjb_2_0_DocType() {
        allowNonEjb_2_0_DocType = false;
    }

    /**
     * Opens and parses an xml document and returns the root element,
     * or throws an error. The parser validates the document.
     *
     * @param filename the XML file to open
     * @return Document the document object containing the XML data
     * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
    * @deprecated see parseXML(org.xml.sax.InputSource inputSource, int validationMode)
     */
    public static Document parseXML(String filename) throws IOException, EJBJarXMLException {
        return parseXML(filename, true);
    }

    /**
     * Opens and parses an xml document and returns the root element,
     * or throws an error.
     *
     * @param filename the XML file to open
     * @param isValidating should a parser validate the document.
     * @return Document the document object containing the XML data
     * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
    * @deprecated see parseXML(org.xml.sax.InputSource inputSource, int validationMode)
     */
    public static Document parseXML(String filename, boolean isValidating) throws IOException, EJBJarXMLException {
        File parseFile = new File(filename);
        if (parseFile.exists()) {
            //Bug2631348  Only UTF-8 is supported
            FileInputStream fio = new FileInputStream(parseFile);
            Document doc = parseXML(new org.xml.sax.InputSource(new InputStreamReader(fio, "UTF-8")), isValidating);
            fio.close();
            return doc;
        } else {
            throw new FileNotFoundException(filename);
        }
    }

    /**
     * Opens and parses an xml document and returns the root element,
     * or throws an error.
     *
     * @param filename the XML file to open
     * @param validationMode should a parser validate the document against DTD or schema. The modes are OLD_VALIDATION,
      * NONVALIDATING, SCHEMA_VALIDATION, and DTD_VALIDATION.
     * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
     */
    public static Document parseXML(String filename, int validationMode) throws IOException, EJBJarXMLException {
        File parseFile = new File(filename);
        if (parseFile.exists()) {
            //Bug2631348  Only UTF-8 is supported
            FileInputStream fio = new FileInputStream(parseFile);
            Document doc = parseXML(new org.xml.sax.InputSource(new InputStreamReader(fio, "UTF-8")), validationMode);
            fio.close();
            return doc;
        } else {
            throw new FileNotFoundException(filename);
        }
    }

    /**
      *
      * Opens and parses an xml document and returns the root element,
      * or throws an error. The parser validates the document.
      *
      * @param inputSource the Sax inputsource
      * @return Document the document object containing the XML data
      * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
    * @deprecated see parseXML(org.xml.sax.InputSource inputSource, int validationMode)
      */
    public static Document parseXML(org.xml.sax.InputSource inputSource) throws IOException, EJBJarXMLException {
        return parseXML(inputSource, true);
    }

    /**
      *
      * Opens and parses an xml document and returns the root element,
      * or throws an error.
      *
      * @param inputSource the Sax inputsource
      * @param isValidating should a parser validate the document.
      * @return Document the document object containing the XML data
      * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
    * @deprecated see parseXML(org.xml.sax.InputSource inputSource, int validationMode)
      */
    public static Document parseXML(org.xml.sax.InputSource inputSource, boolean isValidating) throws IOException, EJBJarXMLException {
        return parseXML(inputSource, OLD_VALIDATION);
    }

    /**
      *
      * Opens and parses an xml document and returns the root element,
      * or throws an error.
      *
      * @param inputSource the Sax inputsource
      * @param validationMode should a parser validate the document against DTD or schema. The modes are OLD_VALIDATION,
    * NONVALIDATING, SCHEMA_VALIDATION, and DTD_VALIDATION.
      * @return Document the document object containing the XML data
      * @throws IOException, SAXParseException, SAXException, ParserConfigurationException
      */
    public static Document parseXML(org.xml.sax.InputSource inputSource, int validationMode) throws IOException, EJBJarXMLException {
        if (inputSource == null) {
            throw new IOException(ExceptionLocalization.buildMessage("input_source_not_found", (Object[])null));
        }

        //JAXP currently requires a schema properties setting for an XML parser to work on XML Schema validation.
        //but it breaks DTD validation.
        //Also note that currenlt only Xerces-J 2.1+ version supports XML Schema validation, and Oracle
        //Parser does not recognize JAXP1.2 properties until maybe 10iR2 stream.
        XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        oracle.toplink.platform.xml.XMLParser xmlParser = xmlPlatform.newXMLParser();
        xmlParser.setNamespaceAware(true);

        if ((validationMode == SCHEMA_VALIDATION) || (validationMode == DTD_VALIDATION) || (validationMode == NONVALIDATING)) {
            xmlParser.setValidationMode(validationMode);
        } else if (validationMode == OLD_VALIDATION) {
            //use the system properties to turn on the schema validation
            String usesSchemaValidation = System.getProperty("toplink.ejbjar.schemavalidation");
            if ((usesSchemaValidation != null) && (usesSchemaValidation.equalsIgnoreCase("true"))) {
                xmlParser.setValidationMode(oracle.toplink.platform.xml.XMLParser.SCHEMA_VALIDATION);
            } else {
                xmlParser.setValidationMode(oracle.toplink.platform.xml.XMLParser.DTD_VALIDATION);
            }
        }

        CustomDefaultHandler customHandler = new CustomDefaultHandler(validationMode);
        xmlParser.setErrorHandler(customHandler);
        xmlParser.setEntityResolver(customHandler);
        Document doc = xmlParser.parse(inputSource);
        if (!allowNonEjb_2_0_DocType && !isEJB20DocType(doc)) {
            throw EJBJarXMLException.nonEJB_2_0_DocType();
        }
        return doc;
    }

    /*
     * Return true if the ejb-jar xml fiel is ejb20 doctype
     */
    public static boolean isEJB20DocType(Document doc) {
        DocumentType docType = doc.getDoctype();
        if (docType == null) {//could be use xml schema
            String namespace = doc.getDocumentElement().getAttribute(XMLManager.XMLNS);
            if (namespace == null) {
                //neither defined
                return false;
            } else {
                return namespace.equals(XMLManager.XML_NAMESPACE);
            }
        } else {
            return docType.getPublicId().equals(XMLManager.EJB_JAR_2_0_DOCTYPE_DESC);
        }
    }

    /*
     * Main method to test XMLParser.  This also shows how to use this class.
     * Anuj Jain's test
     */
    public static void main(String[] args) {
        XMLParser parser = new XMLParser();
        Hashtable hashTable = new Hashtable();
        URL url = parser.getClass().getResource("ejb-jar_2_0.dtd");
        if (url != null) {
            hashTable.put(XMLManager.EJB_JAR_2_0_DTD_URL, url.toString());
            System.out.println("Put hashTable");
        } else {
            System.out.println("Man! could not put hashTable");
        }
        try {
            URL url_ = parser.getClass().getResource("ejb-jar.xml");
            Document doc = XMLParser.parseXML(url_.getFile());
        } catch (Exception e) {
            System.out.println("Error - AJ " + e.getMessage());
        }
    }

    static class CustomDefaultHandler extends DefaultHandler {
        private int validationMode;
        
        public CustomDefaultHandler(int validationMode) {
            this.validationMode = validationMode; 
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;// new SAXParseException("Error parsing " + e.getMessage());
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;//new SAXParseException("Fatal Error parsing " + e.getMessage());
        }

        public void warning(SAXParseException e) throws SAXException {
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            //bug 3989411: if disable validating ejb-jar.xml, return empty string reader input source to bypass dtd url access.
            if(validationMode == NONVALIDATING) {
                return new InputSource(new StringReader("")); 
                
            }
            // the following was done in order to take care of 
            // clients who do not have TCP/IP connectivity - 
            // we now always check against our local dtd
            // and we always ignore the publicId and the systemId
            // (be sure to use the right classloader...)
            ClassLoader classLoader = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedGetClassLoaderForClass(getClass()));
                }catch (PrivilegedActionException ex){
                    throw (RuntimeException)ex.getCause();
                }
            }else{
                classLoader = PrivilegedAccessHelper.getClassLoaderForClass(getClass());
            }
            InputStream localDtdStream = classLoader.getResourceAsStream(XMLManager.EJB_JAR_DTD_2_0_LOCAL_RESOURCE);

            // add loading "ejb-jar_1_1.dtd" from classpath
            if (allowNonEjb_2_0_DocType) {
                localDtdStream = classLoader.getResourceAsStream(XMLManager.EJB_JAR_1_1_DTD_FILE_NAME);
            }

            if (localDtdStream != null) {
                return new InputSource(localDtdStream);
            }

            // not looking for ejb-jar.xml dtd or we couldn't find it.
            return null;
        }
    }
}