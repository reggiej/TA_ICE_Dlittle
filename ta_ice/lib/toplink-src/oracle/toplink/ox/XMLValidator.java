// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox;

import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.platform.xml.XMLParser;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformException;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;

/**
 * <p>Class used to validate XML.  This is used to check if the XML created during
 * a marshal operation would be valid XML before the marshal operation is performed.
 * <p>Create an XMLValidator from an XMLContext.<br>
 * <em>Code Sample</em><br>
 *  <code>
 *  XMLContext context = new XMLContext("mySessionName");<br>
 *  XMLValidator validator = context.createValidator();<br>
 *  <code>
 *
 *  <p>The validateRoot method is used to validate objects which are mapped to global
 *  elements in a schema and which have a default root element specified in the TopLink
 *  project.  The validate method is used to validate all other mapped objects.
 *
 * @see oracle.toplink.ox.XMLContext
 */
public class XMLValidator {
    public static final int NONVALIDATING = XMLParser.NONVALIDATING;
    public static final int SCHEMA_VALIDATION = XMLParser.SCHEMA_VALIDATION;
    public static final int DTD_VALIDATION = XMLParser.DTD_VALIDATION;
    private XMLContext xmlContext;
    private XMLMarshaller marshaller;
    private ErrorHandler errorHandler;

    protected XMLValidator(XMLContext xmlContext) {
        this.xmlContext = xmlContext;
        this.marshaller = new XMLMarshaller(xmlContext);
    }

    /**
      * Validate the given root object.
      * @param rootObject A single root object to validate
      * @return true if this is a valid object, otherwise false
      */
    public boolean validateRoot(Object rootObject) throws XMLMarshalException {
        if (rootObject == null) {
            throw XMLMarshalException.nullArgumentException();
        }

        XMLDescriptor xmlDescriptor = (XMLDescriptor)xmlContext.getSession(rootObject).getDescriptor(rootObject);
        Document document = marshaller.objectToXML(rootObject, xmlDescriptor, false);                

        if (xmlDescriptor.getSchemaReference() == null) {
            throw XMLMarshalException.schemaReferenceNotSet(xmlDescriptor);
        }
        return xmlDescriptor.getSchemaReference().isValid(document, getErrorHandler());
    }

    /**
    * Validate the given object.
    * @param object A single object to validate
    * @return true if this is a valid object, otherwise false
    */
    public boolean validate(Object object) throws XMLMarshalException {
        if (object == null) {
            throw XMLMarshalException.nullArgumentException();
        }

        try {
            // Create a new XML Record using the object's class name (not fully qualified) as the root            
            String name = ((XMLDescriptor)xmlContext.getSession(object).getDescriptor(object)).getDefaultRootElement();
            if (name == null) {
                String qualifiedName = object.getClass().getName();
                int idx = qualifiedName.lastIndexOf('.');
                name = qualifiedName.substring(idx + 1);
            }

            XMLDescriptor descriptor = marshaller.getDescriptor(object);

            XMLRoot root = new XMLRoot();
            root.setObject(object);
            root.setLocalName(name);
            
            XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
            Document doc = xmlPlatform.createDocument();
            marshaller.marshal(root, doc);
            return xmlPlatform.validate(doc.getDocumentElement(), descriptor, getErrorHandler());
        } catch (XMLPlatformException e) {
            throw XMLMarshalException.validateException(e);
        }
    }

    /**
    * Set the error handler to be used during validation
    * @param handler the error handler to be used during validation
    */
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
      * Get the error handler to be used during validation
      * @return the error handler associated with this XMLValidator
      */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}