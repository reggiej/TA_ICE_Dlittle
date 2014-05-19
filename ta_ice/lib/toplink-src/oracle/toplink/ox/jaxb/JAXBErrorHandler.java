// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;

public class JAXBErrorHandler implements ErrorHandler {
    private ValidationEventHandler eventHandler;

    public JAXBErrorHandler(ValidationEventHandler validationEventHandler) {
        super();
        eventHandler = validationEventHandler;
    }

    public void warning(SAXParseException exception) throws SAXException {
        ValidationEventLocator eventLocator = new ValidationEventLocatorImpl(exception);
        ValidationEvent event = new ValidationEventImpl(ValidationEvent.WARNING, null, eventLocator, exception);
        if (!eventHandler.handleEvent(event)) {
            throw exception;
        }
    }

    public void error(SAXParseException exception) throws SAXException {
        ValidationEventLocator eventLocator = new ValidationEventLocatorImpl(exception);
        ValidationEvent event = new ValidationEventImpl(ValidationEvent.ERROR, null, eventLocator, exception);
        if (!eventHandler.handleEvent(event)) {
            throw exception;
        }
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        ValidationEventLocator eventLocator = new ValidationEventLocatorImpl(exception);
        ValidationEvent event = new ValidationEventImpl(ValidationEvent.FATAL_ERROR, null, eventLocator, exception);
        if (!eventHandler.handleEvent(event)) {
            throw exception;
        }
    }
}