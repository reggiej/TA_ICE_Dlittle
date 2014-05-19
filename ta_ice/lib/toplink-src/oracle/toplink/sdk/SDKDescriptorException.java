// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.mappings.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * Exception used for any problem
 * that is detected with an SDK descriptor or mapping.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKDescriptorException extends oracle.toplink.exceptions.DescriptorException {
    public static final int UNSUPPORTED = 19001;
    public static final int CUSTOM_SELECTION_QUERY_REQUIRED = 19002;
    public static final int SIZE_MISMATCH_OF_FIELD_TRANSLATIONS = 19003;

    protected SDKDescriptorException(String message) {
        super(message);
    }

    protected SDKDescriptorException(String message, DatabaseMapping mapping) {
        super(message, mapping);
    }

    protected SDKDescriptorException(String message, ClassDescriptor descriptor) {
        super(message, descriptor);
    }

    protected SDKDescriptorException(String message, ClassDescriptor descriptor, Throwable exception) {
        super(message, descriptor, exception);
    }

    public static SDKDescriptorException customSelectionQueryRequired(ForeignReferenceMapping mapping) {
        Object[] args = {  };

        SDKDescriptorException exception = new SDKDescriptorException(ExceptionMessageGenerator.buildMessage(SDKDescriptorException.class, CUSTOM_SELECTION_QUERY_REQUIRED, args), mapping);
        exception.setErrorCode(CUSTOM_SELECTION_QUERY_REQUIRED);
        return exception;
    }

    public static SDKDescriptorException sizeMismatchOfFieldTranslations() {
        Object[] args = {  };

        SDKDescriptorException exception = new SDKDescriptorException(ExceptionMessageGenerator.buildMessage(SDKDescriptorException.class, SIZE_MISMATCH_OF_FIELD_TRANSLATIONS, args));
        exception.setErrorCode(SIZE_MISMATCH_OF_FIELD_TRANSLATIONS);
        return exception;
    }

    public static SDKDescriptorException unsupported(String feature, DatabaseMapping mapping) {
        Object[] args = { feature };

        SDKDescriptorException exception = new SDKDescriptorException(ExceptionMessageGenerator.buildMessage(SDKDescriptorException.class, UNSUPPORTED, args), mapping);
        exception.setErrorCode(UNSUPPORTED);
        return exception;
    }
}