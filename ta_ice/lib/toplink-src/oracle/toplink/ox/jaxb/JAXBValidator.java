// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;
import javax.xml.bind.Validator;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import oracle.toplink.ox.XMLValidator;

/**
 * Facilitates JAXBValidation.
 */
public class JAXBValidator implements Validator {
    private ValidationEventHandler validationEventHandler;
    private XMLValidator xmlValidator;

    /**
     * This constructor creates a
     * DefaultValidationEventHandlervalidation instance, and sets the
     * XMLMarshaller instance to the one provided.
     *
     * @param newXMLMarshaller
     */
    public JAXBValidator(XMLValidator newValidator) {
        super();
        validationEventHandler = new DefaultValidationEventHandler();
        xmlValidator = newValidator;
    }

    /**
     * Validate a root object against a schema.
     *
     * @param rootObject - the root object to be validated
     * @return true if a valid root object, false otherwise
     * @throws JAXBException
     */
    public boolean validateRoot(Object rootObject) throws JAXBException {
        if (rootObject == null) {
            throw new IllegalArgumentException();
        }

        try {
            return xmlValidator.validateRoot(rootObject);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Validate a non-root object against a schema.
     *
     * @param object - the object to be validated
     * @return true if a valid object, false otherwise
     * @throws JAXBException
     */
    public boolean validate(Object object) throws JAXBException {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        try {
            return xmlValidator.validate(object);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public void setEventHandler(ValidationEventHandler newValidationEventHandler) throws JAXBException {
        if (null == newValidationEventHandler) {
            validationEventHandler = new DefaultValidationEventHandler();
        } else {
            validationEventHandler = newValidationEventHandler;
        }
        xmlValidator.setErrorHandler(new JAXBErrorHandler(validationEventHandler));
    }

    public ValidationEventHandler getEventHandler() throws JAXBException {
        return validationEventHandler;
    }

    public void setProperty(String key, Object value) throws PropertyException {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        throw new PropertyException(key, value);
    }

    public Object getProperty(String key) throws PropertyException {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        throw new PropertyException("Unsupported Property");
    }
}