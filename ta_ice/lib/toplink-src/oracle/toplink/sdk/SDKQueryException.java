// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.queryframework.*;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * Exception used for SDK query problems.
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKQueryException extends oracle.toplink.exceptions.QueryException {
    public static final int INVALID_SDK_CALL = 20001;
    public static final int INVALID_MECHANISM_STATE = 20002;
    public static final int INVALID_SDK_ACCESSOR = 20003;
    public static final int INVALID_ACCESSOR_CLASS = 20004;

    protected SDKQueryException(String message) {
        super(message);
    }

    protected SDKQueryException(String message, DatabaseQuery query) {
        super(message, query);
    }

    public static SDKQueryException invalidAccessorClass(Class expected, Class actual) {
        Object[] args = { expected.getName(), actual.getName(), CR };

        SDKQueryException exception = new SDKQueryException(ExceptionMessageGenerator.buildMessage(SDKQueryException.class, INVALID_ACCESSOR_CLASS, args));
        exception.setErrorCode(INVALID_ACCESSOR_CLASS);
        return exception;
    }

    public static SDKQueryException invalidMechanismState(DatabaseQuery query) {
        Object[] args = {  };

        SDKQueryException exception = new SDKQueryException(ExceptionMessageGenerator.buildMessage(SDKQueryException.class, INVALID_MECHANISM_STATE, args), query);
        exception.setErrorCode(INVALID_MECHANISM_STATE);
        return exception;
    }

    public static SDKQueryException invalidSDKAccessor(oracle.toplink.internal.databaseaccess.Accessor accessor) {
        Object[] args = { accessor };

        SDKQueryException exception = new SDKQueryException(ExceptionMessageGenerator.buildMessage(SDKQueryException.class, INVALID_SDK_ACCESSOR, args));
        exception.setErrorCode(INVALID_SDK_ACCESSOR);
        return exception;
    }

    public static SDKQueryException invalidSDKCall(Call call) {
        Object[] args = { call };

        SDKQueryException exception = new SDKQueryException(ExceptionMessageGenerator.buildMessage(SDKQueryException.class, INVALID_SDK_CALL, args));
        exception.setErrorCode(INVALID_SDK_CALL);
        return exception;
    }
}