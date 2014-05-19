// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: Allow for creation of object varray type.
 * <p>
 */
public class ObjectVarrayDefinition extends VarrayDefinition {
    protected boolean isNullAllowed;

    public ObjectVarrayDefinition() {
        super();
        this.isNullAllowed = false;
    }

    /**
     * INTERNAL:
     * Append the type.
     */
    public void appendTypeString(Writer writer, AbstractSession session) throws ValidationException {
        try {
            FieldTypeDefinition fieldType;
            if (getType() == null) {
                throw ValidationException.oracleObjectTypeIsNotDefined(getTypeName());
            } else if (getTypeName() == "") {
                throw ValidationException.oracleObjectTypeNameIsNotDefined(getType());
            } else {
                fieldType = new FieldTypeDefinition(getTypeName());
            }
            writer.write(fieldType.getName());
            if (!isNullAllowed) {
                writer.write(" NOT NULL");
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * PUBLIC:
     * Return if the varray collection is allowed NULL or not
     */
    public boolean isNullAllowed() {
        return isNullAllowed;
    }

    /**
     * PUBLIC:
     * Set if the varray collection is allowed NULL or not
     */
    public void setIsNullAllowed(boolean isNullAllowed) {
        this.isNullAllowed = isNullAllowed;
    }
}