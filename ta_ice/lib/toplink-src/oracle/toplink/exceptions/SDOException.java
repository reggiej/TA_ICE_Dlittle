package oracle.toplink.exceptions;

import java.io.IOException;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <b>Purpose:</b>
 * <ul><li>This class provides an implementation of TopLinkException specific to the EclipseLink SDO (Service Data Objects) API.</li>
 * </ul>
 * <p/>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Return an SDOException that can be thrown around input parameters.
 * <li>Return an SDOException that wraps an existing exception with additional input parameters.
 * </ul>
 * @since Oracle TopLink 11.1.1.0.0
 */
public class SDOException extends TopLinkException {
	/*
	 * The following ID's are expanded to Strings in oracle.toplink.exceptions.i18n.SDOExceptionResource
	 * 
	 */
    public static final int NO_ID_SPECIFIED = 45000;
    public static final int ERROR_PROCESSING_IMPORT = 45001;
    public static final int ERROR_PROCESSING_INCLUDE = 45002;
    public static final int REFERENCED_PROPERTY_NOT_FOUND = 45003;
    public static final int OLD_SEQUENCE_NOT_FOUND = 45004;
    public static final int FOUND_SIMPLE_VALUE_FOR_FOR_NON_DATATYPE_PROPERTY = 45005;
    public static final int SEQUENCE_NULL_ON_SEQUENCED_DATAOBJECT = 45006;
    public static final int NO_TYPE_SPECIFIED_FOR_PROPERTY = 45007;
    public static final int IO_EXCEPTION_OCCURRED = 45008;
    public static final int TYPE_NOT_FOUND = 45009;
    public static final int TYPE_NOT_FOUND_FOR_INTERFACE = 45010;
    public static final int ERROR_CREATING_DATAOBJECT_FOR_TYPE = 45011;
    public static final int ERROR_CREATING_DATAOBJECT_FOR_CLASS = 45012;
    public static final int NO_APP_INFO_FOR_NULL = 45013;    
    public static final int ERROR_DEFINING_TYPE = 45014;    
    public static final int ERROR_DEFINING_TYPE_NO_NAME = 45015;    
    public static final int MISSING_REF_ATTRIBUTE = 45016;    
    public static final int ERROR_PROCESSING_XPATH = 45017;    
    public static final int SEQUENCE_DUPLICATE_ADD_NOT_SUPPORTED = 45018;    
    public static final int SEQUENCE_ERROR_PROPERTY_IS_ATTRIBUTE = 45019;    
    public static final int SEQUENCE_ERROR_NO_PATH_FOUND = 45020;    
    public static final int SEQUENCE_ERROR_DATAOBJECT_IS_NULL = 45021;
    public static final int SEQUENCE_NOT_SUPPORTED_FOR_PROPERTY = 45022;
    public static final int WRONG_VALUE_FOR_PROPERTY = 45023;
    public static final int CONVERSION_ERROR = 45024;
    public static final int PROPERTY_NOT_FOUND_AT_INDEX = 45025;
    public static final int CANNOT_PERFORM_OPERATION_ON_NULL_ARGUMENT = 45026;
    public static final int CLASS_NOT_FOUND = 45027;
    public static final int TYPE_CANNOT_BE_OPEN_AND_DATATYPE = 45028;
    public static final int INVALID_INDEX = 45029;
    public static final int JAVA_CLASS_INVOKING_ERROR = 45030;
    public static final int CANNOT_SET_PROPERTY_TYPE_ANNOTATION_IF_TARGET_DATATYPE_TRUE = 45031;
    public static final int XMLMARSHAL_EXCEPTION_OCCURRED = 45032;
    public static final int TYPE_REFERENCED_BUT_NEVER_DEFINED = 45033;
    public static final int OPTIONS_MUST_BE_A_DATAOBJECT = 45034;
    public static final int TYPE_PROPERTY_MUST_BE_A_TYPE = 45035;
    public static final int GLOBAL_PROPERTY_NOT_FOUND = 45036;
    public static final int PREFIX_USED_BUT_NOT_DEFINED = 45037;
    public static final int CANNOT_PERFORM_OPERATION_ON_PROPERTY = 45038;
    
    protected SDOException(String message) {
        super(message);
    }

    protected SDOException(String message, Exception internalException) {
        super(message, internalException);
    }

    /**
     * INTERNAL:
     * Exception when building an ObjectReferenceMapping and referenced object does not have an id property
     */
    public static SDOException noTargetIdSpecified(String uri, String name) {
        Object[] args = { uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, NO_ID_SPECIFIED, args));
        exception.setErrorCode(NO_ID_SPECIFIED);
        return exception;
    }

    /**
      * INTERNAL:
      * Exception when processing an import during xsdhelper.define
      */
    public static SDOException errorProcessingImport(String schemaLocation, String namespace, Exception nestedException) {
        Object[] args = { schemaLocation, namespace };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_PROCESSING_IMPORT, args), nestedException);
        exception.setErrorCode(ERROR_PROCESSING_IMPORT);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when processing an include during xsdhelper.define
     */
    public static SDOException errorProcessingInclude(String schemaLocation, Exception nestedException) {
        Object[] args = { schemaLocation };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_PROCESSING_INCLUDE, args), nestedException);
        exception.setErrorCode(ERROR_PROCESSING_INCLUDE);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when trying to find a referenced property during xsdhelper.define
     */
    public static SDOException referencedPropertyNotFound(String uri, String localName) {
        Object[] args = { uri, localName };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, REFERENCED_PROPERTY_NOT_FOUND, args));
        exception.setErrorCode(REFERENCED_PROPERTY_NOT_FOUND);
        return exception;
    }
    
    /**
     * INTERNAL:
     * Exception when trying to find a global property during an unmarshal
     */
    public static SDOException globalPropertyNotFound() {
        Object[] args = { };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, GLOBAL_PROPERTY_NOT_FOUND, args));        
        exception.setErrorCode(GLOBAL_PROPERTY_NOT_FOUND);
        return exception;
    }
        
    /**
     * INTERNAL:
     * Exception when old sequence is not found in the changesummary
     */
    public static SDOException oldSequenceNotFound() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, OLD_SEQUENCE_NOT_FOUND, args));
        exception.setErrorCode(OLD_SEQUENCE_NOT_FOUND);
        return exception;
    }

    /**
      * INTERNAL:
      * Exception when the value should be a dataObject and it's not
      */
    public static SDOException foundSimpleValueForNonDataTypeProperty(String propertyName) {
        Object[] args = { propertyName };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, FOUND_SIMPLE_VALUE_FOR_FOR_NON_DATATYPE_PROPERTY, args));
        exception.setErrorCode(FOUND_SIMPLE_VALUE_FOR_FOR_NON_DATATYPE_PROPERTY);
        return exception;
    }

    /**
    * INTERNAL:
     * Exception when sequence is null on a dataObject with sequenced set to true
     */
    public static SDOException sequenceNullOnSequencedDataObject() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_NULL_ON_SEQUENCED_DATAOBJECT, args));
        exception.setErrorCode(SEQUENCE_NULL_ON_SEQUENCED_DATAOBJECT);
        return exception;
    }

    /**
      * INTERNAL:
      * Exception when a property does not have a type specified
      */
    public static SDOException noTypeSpecifiedForProperty(String propertyName) {
        Object[] args = { propertyName };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, NO_TYPE_SPECIFIED_FOR_PROPERTY, args));
        exception.setErrorCode(NO_TYPE_SPECIFIED_FOR_PROPERTY);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when an ioException happens
     */
    public static SDOException ioExceptionOccurred(IOException nestedException) {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, IO_EXCEPTION_OCCURRED, args), nestedException);
        exception.setErrorCode(IO_EXCEPTION_OCCURRED);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when an XMLMarshalException happens
     */
    public static SDOException xmlMarshalExceptionOccurred(XMLMarshalException nestedException, String rootElementURI, String rootElementName) {
        Object[] args = { nestedException.getLocalizedMessage(), rootElementURI, rootElementName};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, XMLMARSHAL_EXCEPTION_OCCURRED, args), nestedException);
        exception.setErrorCode(XMLMARSHAL_EXCEPTION_OCCURRED);
        return exception;
    }
    
    /**
      * INTERNAL:
      * Exception trying to lookup a type with the given uri and name
      */
    public static SDOException typeNotFound(String uri, String name) {
        Object[] args = { uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, TYPE_NOT_FOUND, args));
        exception.setErrorCode(TYPE_NOT_FOUND);
        return exception;
    }

    /**
      * INTERNAL:
      * Exception trying to lookup a type with the given interface
      */
    public static SDOException typeNotFoundForInterface(String className) {
        Object[] args = { className };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, TYPE_NOT_FOUND_FOR_INTERFACE, args));
        exception.setErrorCode(TYPE_NOT_FOUND_FOR_INTERFACE);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception trying to create a dataObject with a type with the given uri and name
     */
    public static SDOException errorCreatingDataObjectForType(String uri, String name) {
        Object[] args = { uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_CREATING_DATAOBJECT_FOR_TYPE, args));
        exception.setErrorCode(ERROR_CREATING_DATAOBJECT_FOR_TYPE);
        return exception;
    }

    /**
      * INTERNAL:
      * Exception trying to create a dataObject with a type with the given interface
      */
    public static SDOException errorCreatingDataObjectForClass(Exception nestedException, String className, String uri, String name) {
        Object[] args = { className, uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_CREATING_DATAOBJECT_FOR_CLASS, args), nestedException);
        exception.setErrorCode(ERROR_CREATING_DATAOBJECT_FOR_CLASS);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception trying to lookup an appinfo with a null property or type
     */
    public static SDOException noAppInfoForNull() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, NO_APP_INFO_FOR_NULL, args));
        exception.setErrorCode(NO_APP_INFO_FOR_NULL);
        return exception;
    }

    /**
    * INTERNAL:
    * Exception trying to define a type
    */
    public static SDOException errorDefiningType() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_DEFINING_TYPE, args));
        exception.setErrorCode(ERROR_DEFINING_TYPE);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception trying to create a type without a name
     */
    public static SDOException errorDefiningTypeNoName() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_DEFINING_TYPE_NO_NAME, args));
        exception.setErrorCode(ERROR_DEFINING_TYPE_NO_NAME);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when an sdo:ref attribute is missing during xmlhelper.load
     */
    public static SDOException missingRefAttribute() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, MISSING_REF_ATTRIBUTE, args));
        exception.setErrorCode(MISSING_REF_ATTRIBUTE);
        return exception;
    }

    /**
    * INTERNAL:
    * Exception processing an xpath during xmlhelper.load
    */
    public static SDOException errorProcessingXPath(String xpath) {
        Object[] args = { xpath };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, ERROR_PROCESSING_XPATH, args));
        exception.setErrorCode(ERROR_PROCESSING_XPATH);
        return exception;
    }

    /**
     * INTERNAL:
     * Return an exception when attempting to add a setting to a sequene that already has
     * an existing entry.  The existing entry will not be updated or moved to the end of the sequence.
     * This exception occurs only for complex single types.
     */
    public static SDOException sequenceDuplicateSettingNotSupportedForComplexSingleObject(int index, String settingPropertyName) {
        Object[] args = { new Integer(index), settingPropertyName };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_DUPLICATE_ADD_NOT_SUPPORTED, args));
        exception.setErrorCode(SEQUENCE_DUPLICATE_ADD_NOT_SUPPORTED);
        return exception;
    }

    /**
     * INTERNAL:
     * Error when we attempt to add an attribute property to a sequence
     */
    public static SDOException sequenceAttributePropertyNotSupported(String settingPropertyName) {
        Object[] args = {settingPropertyName};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_ERROR_PROPERTY_IS_ATTRIBUTE, args));
        exception.setErrorCode(SEQUENCE_ERROR_PROPERTY_IS_ATTRIBUTE);
        return exception;
    }

    /**
     * INTERNAL:
     * Error when we attempt find a sequence by path
     */
    public static SDOException sequenceNotFoundForPath(String path) {
        Object[] args = { path };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_ERROR_NO_PATH_FOUND, args));
        exception.setErrorCode(SEQUENCE_ERROR_NO_PATH_FOUND);
        return exception;
    }

    /**
     * INTERNAL:
     * Error passing in a null dataObject into the sequence constructor
     */
    public static SDOException sequenceDataObjectInstanceFieldIsNull() {
        Object[] args = {  };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_ERROR_DATAOBJECT_IS_NULL, args));
        exception.setErrorCode(SEQUENCE_ERROR_DATAOBJECT_IS_NULL);
        return exception;
    }

    /**
     * INTERNAL:
     * Error passing in a null dataObject into the sequence constructor
     */
    public static SDOException sequenceNotSupportedForProperty(String propertyName) {
        Object[] args = { propertyName };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, SEQUENCE_NOT_SUPPORTED_FOR_PROPERTY, args));
        exception.setErrorCode(SEQUENCE_NOT_SUPPORTED_FOR_PROPERTY);
        return exception;
    }

    /**
     * INTERNAL:
     * Exception when trying to set a property to value that is an unsupported conversion
     */
    public static SDOException wrongValueForProperty(String typeUri, String typeName, Class valueClass) {
        Object[] args = { typeUri, typeName, valueClass.getName() };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, WRONG_VALUE_FOR_PROPERTY, args) );
        exception.setErrorCode(WRONG_VALUE_FOR_PROPERTY);
        return exception;
    }

    /**
    * INTERNAL:
    * Exception when trying to set a property to value that is an unsupported conversion
    */
    public static SDOException conversionError(Exception e) {
        Object[] args = {  };
        SDOException exception = null;
        if (e != null) {
            exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, CONVERSION_ERROR, args));
        } else {
            exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, CONVERSION_ERROR, args), e);
        }
        exception.setErrorCode(CONVERSION_ERROR);
        return exception;
    }
    
   /**
    * INTERNAL:
    * Exception when trying to find a property at an invalid index
    */
    public static SDOException propertyNotFoundAtIndex(Exception e, int propIndex) {
        Object[] args = { new Integer(propIndex) };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, PROPERTY_NOT_FOUND_AT_INDEX, args), e);        
        exception.setErrorCode(PROPERTY_NOT_FOUND_AT_INDEX);
        return exception;
    }
    
    /**
    * INTERNAL:
    * Exception when trying to perform an operation with a null argument
    */
    public static SDOException cannotPerformOperationOnNullArgument(String methodName) {
        Object[] args = { methodName};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, CANNOT_PERFORM_OPERATION_ON_NULL_ARGUMENT, args));        
        exception.setErrorCode(CANNOT_PERFORM_OPERATION_ON_NULL_ARGUMENT);
        return exception;
    }
 
    /**
     * INTERNAL:
     * Exception when trying to set a property via path based access.
     */
     public static SDOException cannotPerformOperationOnProperty(String propertyName, String path) {
         Object[] args = { propertyName, path };
         SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, CANNOT_PERFORM_OPERATION_ON_PROPERTY, args));        
         exception.setErrorCode(CANNOT_PERFORM_OPERATION_ON_PROPERTY);
         return exception;
     }
     
   /**
     * INTERNAL:
     * Exception trying to load the instance class for a given type
     */
    public static SDOException classNotFound(Exception nestedException, String uri, String name) {
        Object[] args = { uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, CLASS_NOT_FOUND, args), nestedException);
        exception.setErrorCode(CLASS_NOT_FOUND);
        return exception;
    }
    
     
   /**
     * INTERNAL:
     * Exception trying to set a type to be both open and dataType
     */
    public static SDOException typeCannotBeOpenAndDataType(String uri, String name) {
        Object[] args = { uri, name };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, TYPE_CANNOT_BE_OPEN_AND_DATATYPE, args));
        exception.setErrorCode(TYPE_CANNOT_BE_OPEN_AND_DATATYPE);
        return exception;
    }
    
    /**
     * INTERNAL:
     * Exception trying to pass an invalid index to a method
     */
    public static SDOException invalidIndex(IndexOutOfBoundsException nestedException, int index) {
        Object[] args = {new Integer(index) };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, INVALID_INDEX, args),nestedException);
        exception.setErrorCode(INVALID_INDEX);
        return exception;
    }
    
    /**
     * INTERNAL:
     * thrown from InstanceClassConverter 
     */
     public static SDOException noConstructorWithString(Exception nestedException, String className) {
        Object[] args = {className };
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, JAVA_CLASS_INVOKING_ERROR, args), nestedException);
        exception.setErrorCode(JAVA_CLASS_INVOKING_ERROR);
        return exception;
    }

     /**
      * INTERNAL:
      * Exception when trying to resolve an sdo xml [propertyType] annotation on a target Type of dataType==true.<br/>
      * See Sect 9.2 (1) of the SDO Specification.
      * thrown from SDOTypesGenerator.postProcessing()
      */
      public static SDOException propertyTypeAnnotationTargetCannotBeDataTypeTrue(String targetTypeName, String sourcePropertyName) {
          Object[] args = { targetTypeName, sourcePropertyName};
          SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(//
        		  SDOException.class, CANNOT_SET_PROPERTY_TYPE_ANNOTATION_IF_TARGET_DATATYPE_TRUE, args));        
          exception.setErrorCode(CANNOT_SET_PROPERTY_TYPE_ANNOTATION_IF_TARGET_DATATYPE_TRUE);
          return exception;
      }
      
      public static SDOException typeReferencedButNotDefined(String namespaceUri, String typeName) {
          Object[] args = {namespaceUri, typeName};
          SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(
                  SDOException.class, TYPE_REFERENCED_BUT_NEVER_DEFINED, args));
          exception.setErrorCode(TYPE_REFERENCED_BUT_NEVER_DEFINED);
          return exception;
      }
      
    /**
     * INTERNAL:
     * thrown from SDOXMLHelperDelegate 
     */
     public static SDOException optionsMustBeADataObject(Exception nestedException, String uri, String name) {
        Object[] args = {uri, name};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, OPTIONS_MUST_BE_A_DATAOBJECT, args), nestedException);
        exception.setErrorCode(OPTIONS_MUST_BE_A_DATAOBJECT);
        return exception;
    }
    
      /**
     * INTERNAL:
     * thrown from SDOXMLHelperDelegate 
     */
     public static SDOException typePropertyMustBeAType(Exception nestedException) {
        Object[] args = {};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, TYPE_PROPERTY_MUST_BE_A_TYPE, args), nestedException);
        exception.setErrorCode(TYPE_PROPERTY_MUST_BE_A_TYPE);
        return exception;
    }
    
    /**
     * INTERNAL:
     * thrown from SDOTypesGenerator 
     */
     public static SDOException prefixUsedButNotDefined(String prefix) {
        Object[] args = {prefix};
        SDOException exception = new SDOException(ExceptionMessageGenerator.buildMessage(SDOException.class, PREFIX_USED_BUT_NOT_DEFINED, args));
        exception.setErrorCode(PREFIX_USED_BUT_NOT_DEFINED);
        return exception;
    }
}
