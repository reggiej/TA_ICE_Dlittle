// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

public class XMLMarshalException extends ValidationException {
    public static final int INVALID_XPATH_STRING = 25001;
    public static final int INVALID_XPATH_INDEX_STRING = 25002;
    public static final int MARSHAL_EXCEPTION = 25003;
    public static final int UNMARSHAL_EXCEPTION = 25004;
    public static final int VALIDATE_EXCEPTION = 25005;
    public static final int DEFAULT_ROOT_ELEMENT_NOT_SPECIFIED = 25006;
    public static final int DESCRIPTOR_NOT_FOUND_IN_PROJECT = 25007;
    public static final int NO_DESCRIPTOR_WITH_MATCHING_ROOT_ELEMENT = 25008;
    public static final int SCHEMA_REFERENCE_NOT_SET = 25010;
    public static final int NULL_ARGUMENT = 25011;
    public static final int ERROR_RESOLVING_XML_SCHEMA = 25012;
    public static final int ERROR_SETTING_SCHEMAS = 25013;
    public static final int ERROR_INSTANTIATING_SCHEMA_PLATFORM = 25014;
    public static final int NAMESPACE_RESOLVER_NOT_SPECIFIED = 25015;
    public static final int NAMESPACE_NOT_FOUND = 25016;
    public static final int ENUM_CLASS_NOT_SPECIFIED = 25017;
    public static final int FROMSTRING_METHOD_ERROR = 25018;
    public static final int INVALID_ENUM_CLASS_SPECIFIED = 25019;
    public static final int ILLEGAL_STATE_XML_UNMARSHALLER_HANDLER = 25020;
    public static final int INVALID_SWA_REF_ATTRIBUTE_TYPE = 25021;
    public static final int NO_ENCODER_FOR_MIME_TYPE = 25022;
    public static final int NO_DESCRIPTOR_FOUND = 25023;
    public static final int ERROR_INSTANTIATING_UNMAPPED_CONTENTHANDLER = 25024;
    public static final int UNMAPPED_CONTENTHANDLER_DOESNT_IMPLEMENT = 25025;

    // ==========================================================================================
    protected XMLMarshalException(String message) {
        super(message);
    }

    protected XMLMarshalException(String message, Exception internalException) {
        super(message, internalException);
    }

    // ==========================================================================================
    public static XMLMarshalException invalidXPathString(String xpathString, Exception nestedException) {
        Object[] args = { xpathString };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, INVALID_XPATH_STRING, args), nestedException);
        exception.setErrorCode(INVALID_XPATH_STRING);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException invalidXPathIndexString(String xpathString) {
        Object[] args = { xpathString };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, INVALID_XPATH_INDEX_STRING, args));
        exception.setErrorCode(INVALID_XPATH_INDEX_STRING);
        return exception;
    }

    public static XMLMarshalException marshalException(Exception nestedException) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, MARSHAL_EXCEPTION, args), nestedException);
        exception.setErrorCode(MARSHAL_EXCEPTION);
        return exception;
    }

    public static XMLMarshalException unmarshalException() {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, UNMARSHAL_EXCEPTION, args));
        exception.setErrorCode(UNMARSHAL_EXCEPTION);
        return exception;
    }

    public static XMLMarshalException unmarshalException(Exception nestedException) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, UNMARSHAL_EXCEPTION, args), nestedException);
        exception.setErrorCode(UNMARSHAL_EXCEPTION);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException validateException(Exception nestedException) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, VALIDATE_EXCEPTION, args), nestedException);
        exception.setErrorCode(VALIDATE_EXCEPTION);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException defaultRootElementNotSpecified(XMLDescriptor descriptor) {
        Object[] args = { descriptor.getJavaClassName() };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, DEFAULT_ROOT_ELEMENT_NOT_SPECIFIED, args));
        exception.setErrorCode(DEFAULT_ROOT_ELEMENT_NOT_SPECIFIED);
        return exception;
    }

    public static XMLMarshalException descriptorNotFoundInProject(String className) {
        Object[] args = { className };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, DESCRIPTOR_NOT_FOUND_IN_PROJECT, args));
        exception.setErrorCode(DESCRIPTOR_NOT_FOUND_IN_PROJECT);
        return exception;
    }

    public static XMLMarshalException noDescriptorWithMatchingRootElement(String rootElementName) {
        Object[] args = { rootElementName };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NO_DESCRIPTOR_WITH_MATCHING_ROOT_ELEMENT, args));
        exception.setErrorCode(NO_DESCRIPTOR_WITH_MATCHING_ROOT_ELEMENT);
        return exception;
    }

    public static XMLMarshalException schemaReferenceNotSet(XMLDescriptor descriptor) {
        Object[] args = { descriptor.getJavaClassName() };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, SCHEMA_REFERENCE_NOT_SET, args));
        exception.setErrorCode(SCHEMA_REFERENCE_NOT_SET);
        return exception;
    }

    public static XMLMarshalException nullArgumentException() {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NULL_ARGUMENT, args));
        exception.setErrorCode(NULL_ARGUMENT);
        return exception;
    }

    public static XMLMarshalException errorResolvingXMLSchema(Exception nestedException) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ERROR_RESOLVING_XML_SCHEMA, args), nestedException);
        exception.setErrorCode(ERROR_RESOLVING_XML_SCHEMA);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException errorSettingSchemas(Exception nestedException, Object[] schemas) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ERROR_RESOLVING_XML_SCHEMA, args), nestedException);
        exception.setErrorCode(ERROR_SETTING_SCHEMAS);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException errorInstantiatingSchemaPlatform(Exception nestedException) {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ERROR_INSTANTIATING_SCHEMA_PLATFORM, args), nestedException);
        exception.setErrorCode(ERROR_INSTANTIATING_SCHEMA_PLATFORM);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException namespaceResolverNotSpecified(String localName) {
        Object[] args = { localName };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NAMESPACE_RESOLVER_NOT_SPECIFIED, args));
        exception.setErrorCode(NAMESPACE_RESOLVER_NOT_SPECIFIED);
        return exception;
    }

    public static XMLMarshalException namespaceNotFound(String prefix) {
        Object[] args = { prefix };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NAMESPACE_NOT_FOUND, args));
        exception.setErrorCode(NAMESPACE_NOT_FOUND);
        return exception;
    }

    public static XMLMarshalException enumClassNotSpecified() {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ENUM_CLASS_NOT_SPECIFIED, args));
        exception.setErrorCode(ENUM_CLASS_NOT_SPECIFIED);
        return exception;
    }

    public static XMLMarshalException errorInvokingFromStringMethod(Exception nestedException, String className) {
        Object[] args = { className };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, FROMSTRING_METHOD_ERROR, args), nestedException);
        exception.setErrorCode(FROMSTRING_METHOD_ERROR);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException invalidEnumClassSpecified(Exception nestedException, String className) {
        Object[] args = { className };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, INVALID_ENUM_CLASS_SPECIFIED, args), nestedException);
        exception.setErrorCode(INVALID_ENUM_CLASS_SPECIFIED);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException illegalStateXMLUnmarshallerHandler() {
        Object[] args = {  };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ILLEGAL_STATE_XML_UNMARSHALLER_HANDLER, args));
        exception.setErrorCode(ILLEGAL_STATE_XML_UNMARSHALLER_HANDLER);
        return exception;
    }

    public static XMLMarshalException invalidSwaRefAttribute(String attributeClassification) {
        Object[] args = { attributeClassification };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, INVALID_SWA_REF_ATTRIBUTE_TYPE, args));
        exception.setErrorCode(INVALID_SWA_REF_ATTRIBUTE_TYPE);

        return exception;
    }

    public static XMLMarshalException noEncoderForMimeType(String mimeType) {
        Object[] args = { mimeType };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NO_ENCODER_FOR_MIME_TYPE, args));
        exception.setErrorCode(NO_ENCODER_FOR_MIME_TYPE);

        return exception;
    }

    public static XMLMarshalException noDescriptorFound(DatabaseMapping mapping) {
        Object[] args = { mapping.getAttributeName() };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, NO_DESCRIPTOR_FOUND, args));
        exception.setErrorCode(NO_DESCRIPTOR_FOUND);
        return exception;
    }

    public static XMLMarshalException errorInstantiatingUnmappedContentHandler(Exception nestedException, String className) {
        Object[] args = { className };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, ERROR_INSTANTIATING_UNMAPPED_CONTENTHANDLER, args), nestedException);
        exception.setErrorCode(ERROR_INSTANTIATING_UNMAPPED_CONTENTHANDLER);
        exception.setInternalException(nestedException);
        return exception;
    }

    public static XMLMarshalException unmappedContentHandlerDoesntImplement(Exception nestedException, String className) {
        Object[] args = { className };

        XMLMarshalException exception = new XMLMarshalException(ExceptionMessageGenerator.buildMessage(XMLMarshalException.class, UNMAPPED_CONTENTHANDLER_DOESNT_IMPLEMENT, args), nestedException);
        exception.setErrorCode(UNMAPPED_CONTENTHANDLER_DOESNT_IMPLEMENT);
        exception.setInternalException(nestedException);
        return exception;
    }
}
