// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Allow a semi-generic way of creating store function.
 * Note that stored functions supported only on Oracle platform
 * <p>
 */
public class StoredFunctionDefinition extends StoredProcedureDefinition {
    public StoredFunctionDefinition() {
        super();
        this.addOutputArgument(new FieldDefinition());
    }

    /**
     * INTERNAL:
     * Return the create statement.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        if (session.getPlatform().supportsStoredFunctions()) {
            super.buildCreationWriter(session, writer);
        } else {
            throw ValidationException.platformDoesNotSupportStoredFunctions(Helper.getShortClassName(session.getPlatform()));
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the drop statement.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        if (session.getPlatform().supportsStoredFunctions()) {
            super.buildDeletionWriter(session, writer);
        } else {
            throw ValidationException.platformDoesNotSupportStoredFunctions(Helper.getShortClassName(session.getPlatform()));
        }
        return writer;
    }

    /**
     *
     */
    public String getCreationHeader() {
        return "CREATE FUNCTION ";
    }

    /**
     *
     */
    public String getDeletionHeader() {
        return "DROP FUNCTION ";
    }

    /**
     *
     */
    public int getFirstArgumentIndex() {
        return 1;
    }

    /**
     * Prints return for stored function
     */
    public void setReturnType(Class type) {
        FieldDefinition argument = (FieldDefinition)getArguments().firstElement();
        argument.setType(type);
    }

    /**
     * Prints return for stored function
     */
    protected void printReturn(Writer writer, AbstractSession session) throws ValidationException {
        try {
            writer.write("\n\t RETURN ");
            FieldDefinition argument = (FieldDefinition)getArguments().firstElement();

            // argumentType should be OUT: getArgumentTypes().firstElement() == OUT;
            // but should be printed as IN
            printArgument(argument, writer, session);
            writer.write("\n");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }
}