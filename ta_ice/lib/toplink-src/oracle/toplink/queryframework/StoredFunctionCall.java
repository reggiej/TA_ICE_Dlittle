// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.exceptions.ValidationException;

/**
 * <b>Purpose</b>: Used to define a platform independent function call.
 * Note that not all platforms support stored functions.
 * This supports output parameters.
 * Functions can also be called through custom SQL.
 */
public class StoredFunctionCall extends StoredProcedureCall {
    public StoredFunctionCall() {
        super();
        addUnamedOutputArgument("");
    }

    /**
     * INTERNAL:
     * Return call header for the call string.
     */
    public String getCallHeader(DatabasePlatform platform) {
        return platform.getFunctionCallHeader();
    }

    /**
     * INTERNAL:
     * Return the first index of parameter to be placed inside brackets
     * in the call string.
     */
    public int getFirstParameterIndexForCallString() {
        return 1;
    }

    /**
     * INTERNAL:
     */
    public boolean isStoredFunctionCall() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public void prepareInternal(AbstractSession session) {
        if (session.getPlatform().supportsStoredFunctions()) {
            super.prepareInternal(session);
        } else {
            throw ValidationException.platformDoesNotSupportStoredFunctions(Helper.getShortClassName(session.getPlatform()));
        }
    }

    /**
     * PUBLIC:
     * Define the field name to be substitute for the function return.
     */
    public void setResult(String name) {
        DatabaseField returnField = (DatabaseField)getParameters().firstElement();
        returnField.setName(name);
    }

    /**
     * PUBLIC:
     * Define the field name to be substitute for the function return.
     * The type is the type of Java class desired back from the function, this is dependent on the type returned from the function.
     */
    public void setResult(String name, Class type) {
        DatabaseField returnField = (DatabaseField)getParameters().firstElement();
        returnField.setName(name);
        returnField.setType(type);
    }
}