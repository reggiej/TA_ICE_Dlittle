// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import java.io.File;
import java.io.FileNotFoundException;
import oracle.toplink.exceptions.EJBJarXMLException;

/**
 * INTERNAL:
 * Root object that stores all deployment descriptor
 * info contained in an ejb-jar.xml file.
 * This includes the following:
 * <OL>
 *         <LI> PI (Processing Instructions)
 *         <LI> DOCTYPE declarations
 *         <LI> EjbJar java object
 * </OL>
 */
public class EjbJarXMLDocument {
    private Document document;
    private EjbJar ejbJar;
    private String xmlFileLocation;
    private long lastModifiedTime = -1;

    public EjbJarXMLDocument() {
        ejbJar = new EjbJar();
    }

    /**
      * Constructor
      * @param doc the DOM document
    * @deprecated see EjbJarXMLDocument(String ejbXmlFile, int parsingValidationMode)
      */
    public EjbJarXMLDocument(String ejbXmlFile) throws FileNotFoundException, EJBJarXMLException {
        this(ejbXmlFile, XMLParser.OLD_VALIDATION);
    }

    /**
      * Constructor
      * @param doc the DOM document
    * @param parsingValidationMode should a parser validate the document against DTD or schema. The modes are XMLParser.NONVALIDATING,
    * XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      */
    public EjbJarXMLDocument(String ejbXmlFile, int parsingValidationMode) throws FileNotFoundException, EJBJarXMLException {
        setXmlFileLocation(ejbXmlFile);
        readAndLoad(parsingValidationMode);
    }

    /**
      * @return EjbJar the DOM model representation of ejb-jar.xml file
      */
    public EjbJar getEjbJar() {
        return ejbJar;
    }

    public boolean isSameXmlFile(String xmlFileLocation) {
        if ((this.xmlFileLocation != null) && (xmlFileLocation != null)) {
            return (new File(this.xmlFileLocation).equals(new File(xmlFileLocation)));
        }

        return false;
    }

    public boolean isModified(String xmlFileLocation) {
        if (xmlFileLocation != null) {
            try {
                return isSameXmlFile(xmlFileLocation) && (this.lastModifiedTime != new File(xmlFileLocation).lastModified());
            } catch (SecurityException e) {
                // don't have security access to modified time
                return true;// best to assume that it has been
            }
        }
        return false;
    }

    /**
    * This method prints all subnodes of a node.
    * It is intended to take a document node (EjbJar document Node)
    * Each EjbJar document node contains:
    * <OL>
    * <LI> zero or more COMMENT and PROCESSING INSTRUCTION (PI) nodes
    * <LI> zero or one DOCTYPE node
    * <LI> one root element node
    * </OL>
    * @param node the parent node
    */
    public static void printAllNodes(Node node) {
        NodeList list = node.getChildNodes();
        int length = list.getLength();
        System.out.println("** length is  **" + length);
        for (int i = 0; i < length; i++) {
            Node nextNode = list.item(i);

            // zero or one doctype
            if (nextNode instanceof DocumentType) {
                System.out.println(((DocumentType)nextNode).getName());// returning ejb-jar
                System.out.println(((DocumentType)nextNode).getPublicId());
                System.out.println(((DocumentType)nextNode).getSystemId());
                System.out.println(((DocumentType)nextNode).getInternalSubset());// returning null
                System.out.println(((DocumentType)nextNode).getNodeValue());// returning null    
                if (nextNode.hasChildNodes()) {
                    printAllNodes(nextNode);
                }
            }
            // one root element node
            else if (nextNode instanceof Element) {
                printAllNodes(nextNode);
                // zero or more PI nodes
            } else if (nextNode instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction)nextNode;
                System.out.println("** PI NODE STARTS **");
                System.out.println(pi.getData());
                System.out.println(pi.getNodeName());
                System.out.println(pi.getTarget());
                System.out.println("** PI NODE ENDS **");
            } else {
                System.out.println("Don't know what to do...");
                printNodeType(nextNode);

            }
        }
    }

    /**
    * Given a DOM node, it prints the type of that node.
    * It uses the following node definitions to figure out the node type.
    * <PRE>
    *     public static final short ELEMENT_NODE                = 1;
    *     public static final short ATTRIBUTE_NODE              = 2;
    *     public static final short TEXT_NODE                   = 3;
    *     public static final short CDATA_SECTION_NODE          = 4;
    *     public static final short ENTITY_REFERENCE_NODE       = 5;
    *     public static final short ENTITY_NODE                 = 6;
    *     public static final short PROCESSING_INSTRUCTION_NODE = 7;
    *     public static final short COMMENT_NODE                = 8;
    *     public static final short DOCUMENT_NODE               = 9;
    *     public static final short DOCUMENT_TYPE_NODE          = 10;
    *     public static final short DOCUMENT_FRAGMENT_NODE      = 11;
    *     public static final short NOTATION_NODE               = 12;
    * </PRE>
    * @param node the parent node
    */
    public static void printNodeType(Node node) {
        System.out.println(" Node " + node.getNodeName() + " ->");
        switch (node.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
            System.out.println("The node is an ATTRIBUTE_NODE ");
            break;
        case Node.CDATA_SECTION_NODE:
            System.out.println("The node is a CDATA_SECTION_NODE");
            break;
        case Node.COMMENT_NODE:
            System.out.println("The node is a COMMENT_NODE");
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            System.out.println("The node is a DOCUMENT_FRAGMENT_NODE");
            break;
        case Node.DOCUMENT_NODE:
            System.out.println("The node is a DOCUMENT_NODE");
            break;
        case Node.DOCUMENT_TYPE_NODE:
            System.out.println("The node is a DOCUMENT_TYPE_NODE");
            break;
        case Node.ELEMENT_NODE:
            System.out.println("The node is an ELEMENT_NODE");
            break;
        case Node.ENTITY_NODE:
            System.out.println("The node is an ENTITY_NODE ");
            break;
        case Node.ENTITY_REFERENCE_NODE:
            System.out.println("The node is an ENTITY_REFERENCE_NODE");
            break;
        case Node.NOTATION_NODE:
            System.out.println("The node is a Notation. ");
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            System.out.println("The node is a PROCESSING_INSTRUCTION_NODE");
            break;
        case Node.TEXT_NODE:
            System.out.println("The node is a TEXT_NODE");
            break;
        }
    }

    /**
     * @deprecated see readAndLoad(int parsingValidationMode)
     */
    public void readAndLoad() throws FileNotFoundException, EJBJarXMLException {
        readAndLoad(XMLParser.OLD_VALIDATION);
    }

    /**
     *  @param parsingValidationMode should a parser validate the document against DTD or schema. The modes are XMLParser.NONVALIDATING,
      * XMLParser.SCHEMA_VALIDATION, and XMLParser.DTD_VALIDATION.
      */
    public void readAndLoad(int parsingValidationMode) throws FileNotFoundException, EJBJarXMLException {
        document = XMLManager.readEjbXmlFile(this, xmlFileLocation, parsingValidationMode);
        ejbJar = EjbJar.loadFromDocument(document);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document doc) {
        document = doc;
    }

    public void setXmlFileLocation(String file) {
        xmlFileLocation = file;
        try {
            lastModifiedTime = new File(file).lastModified();
        } catch (SecurityException e) {
            // don't have security access to modified Time
        }
    }
}