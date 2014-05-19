// Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved. 
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <b>Purpose:</b>
 * <ul><li>This class provides an implementation of EclipseLinkException specific to the EclipseLink JAXB implementation</li>
 * </ul>
 * <p/>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Return a JAXBException that can be thrown around input parameters.
 * <li>Return a JAXBException that wraps an existing exception with additional input parameters.
 * </ul>
 * @since Oracle EclipseLink 1.0
 */
public class JAXBException extends TopLinkException {

    public static final int NO_OBJECT_FACTORY_OR_JAXB_INDEX_IN_PATH = 50000;
    
    protected JAXBException(String message) {
        super(message);
    }

    protected JAXBException(String message, Exception internalException) {
        super(message, internalException);
    }

    
    public static JAXBException noObjectFactoryOrJaxbIndexInPath(String path) {
        Object[] args = { path };
        JAXBException exception = new JAXBException(ExceptionMessageGenerator.buildMessage(JAXBException.class, NO_OBJECT_FACTORY_OR_JAXB_INDEX_IN_PATH, args));
        exception.setErrorCode(NO_OBJECT_FACTORY_OR_JAXB_INDEX_IN_PATH);
        return exception;
    }
}
