// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * Exception used for any problem detected while interacting with the "data store".
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKDataStoreException extends oracle.toplink.exceptions.DatabaseException {
    public static final int UNSUPPORTED = 17001;
    public static final int INCORRECT_LOGIN_INSTANCE_PROVIDED = 17002;
    public static final int INVALID_CALL = 17003;
    public static final int IE_WHEN_INSTANTIATING_ACCESSOR = 17004;
    public static final int IAE_WHEN_INSTANTIATING_ACCESSOR = 17005;
    public static final int SDK_PLATFORM_DOES_SUPPORT_SEQUENCES = 17006;

    protected SDKDataStoreException(Exception exception) {
        this(exception, exception.toString());
    }

    protected SDKDataStoreException(Exception exception, String message) {
        this(message);
        this.setInternalException(exception);
    }

    protected SDKDataStoreException(String message) {
        super(message);
    }

    public static SDKDataStoreException illegalAccessExceptionWhenInstantiatingAccessor(IllegalAccessException iae, Class accessorClass) {
        Object[] args = { accessorClass.getName() };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, IAE_WHEN_INSTANTIATING_ACCESSOR, args));
        exception.setErrorCode(IAE_WHEN_INSTANTIATING_ACCESSOR);
        exception.setInternalException(iae);
        return exception;
    }

    public static SDKDataStoreException incorrectLoginInstanceProvided(Class loginClass) {
        Object[] args = { loginClass };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, INCORRECT_LOGIN_INSTANCE_PROVIDED, args));
        exception.setErrorCode(INCORRECT_LOGIN_INSTANCE_PROVIDED);
        return exception;
    }

    public static SDKDataStoreException instantiationExceptionWhenInstantiatingAccessor(InstantiationException ie, Class accessorClass) {
        Object[] args = { accessorClass.getName() };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, IE_WHEN_INSTANTIATING_ACCESSOR, args));
        exception.setErrorCode(IE_WHEN_INSTANTIATING_ACCESSOR);
        exception.setInternalException(ie);
        return exception;
    }

    public static SDKDataStoreException invalidCall(oracle.toplink.queryframework.DatabaseQuery query) {
        Object[] args = { query };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, INVALID_CALL, args));
        exception.setErrorCode(INVALID_CALL);
        return exception;
    }

    public static SDKDataStoreException sdkPlatformDoesNotSupportSequences() {
        Object[] args = {  };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, SDK_PLATFORM_DOES_SUPPORT_SEQUENCES, args));
        exception.setErrorCode(SDK_PLATFORM_DOES_SUPPORT_SEQUENCES);
        return exception;
    }

    public static SDKDataStoreException unsupported(String feature) {
        Object[] args = { feature };

        SDKDataStoreException exception = new SDKDataStoreException(ExceptionMessageGenerator.buildMessage(SDKDataStoreException.class, UNSUPPORTED, args));
        exception.setErrorCode(UNSUPPORTED);
        return exception;
    }
}