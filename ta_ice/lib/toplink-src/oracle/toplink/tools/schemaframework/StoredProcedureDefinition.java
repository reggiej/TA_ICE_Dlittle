// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;

/**
 * <b>Purpose</b>: Allow a semi-generic way of creating store procedures.
 */
public class StoredProcedureDefinition extends DatabaseObjectDefinition {
    protected Vector variables;
    protected Vector statements;
    protected Vector arguments;
    protected Vector argumentTypes;
    protected static final Integer IN = new Integer(1);
    protected static final Integer OUT = new Integer(2);
    protected static final Integer INOUT = new Integer(3);

    public StoredProcedureDefinition() {
        this.statements = new Vector();
        this.variables = new Vector();
        this.arguments = new Vector();
        this.argumentTypes = new Vector();
    }

    /**
     * The arguments are the names of the parameters to the procedure.
     */
    public void addArgument(String argumentName, Class type) {
        addArgument(new FieldDefinition(argumentName, type));
    }

    /**
     * The arguments are the names of the parameters to the procedure.
     */
    public void addArgument(String argumentName, Class type, int size) {
        addArgument(new FieldDefinition(argumentName, type, size));
    }

    /**
     * The arguments are the names of the parameters to the procedure.
     */
    public void addArgument(String argumentName, String typeName) {
        addArgument(new FieldDefinition(argumentName, typeName));
    }

    /**
     * The arguments are the names of the parameters to the procedure.
     */
    public void addArgument(FieldDefinition argument) {
        getArguments().addElement(argument);
        getArgumentTypes().addElement(IN);
    }

    /**
     * The output arguments are used to get values back from the proc.
     */
    public void addInOutputArgument(String argumentName, Class type) {
        addInOutputArgument(new FieldDefinition(argumentName, type));
    }

    /**
     * The output arguments are used to get values back from the proc, such as cursors.
     */
    public void addInOutputArgument(FieldDefinition argument) {
        getArguments().addElement(argument);
        getArgumentTypes().addElement(INOUT);
    }

    /**
     * The output arguments are used to get values back from the proc.
     */
    public void addOutputArgument(String argumentName, Class type) {
        addOutputArgument(new FieldDefinition(argumentName, type));
    }

    /**
     * The output arguments are used to get values back from the proc.
     */
    public void addOutputArgument(String argumentName, Class type, int size) {
        addOutputArgument(new FieldDefinition(argumentName, type, size));
    }

    /**
     * The output arguments are used to get values back from the proc, such as cursors.
     */
    public void addOutputArgument(String argumentName, String typeName) {
        addOutputArgument(new FieldDefinition(argumentName, typeName));
    }

    /**
     * The output arguments are used to get values back from the proc, such as cursors.
     */
    public void addOutputArgument(FieldDefinition argument) {
        getArguments().addElement(argument);
        getArgumentTypes().addElement(OUT);
    }

    /**
     * The statements are the SQL lines of code in procedure.
     */
    public void addStatement(String statement) {
        getStatements().addElement(statement);
    }

    /**
     * The variables are the names of the declared variables used in the procedure.
     */
    public void addVariable(String variableName, String typeName) {
        this.addVariable(new FieldDefinition(variableName, typeName));
    }

    /**
     * The variables are the names of the declared variables used in the procedure.
     */
    public void addVariable(FieldDefinition variable) {
        getVariables().addElement(variable);
    }

    /**
     * INTERNAL: Return the create table statement.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            DatabasePlatform platform = session.getPlatform();
            writer.write(getCreationHeader() + getFullName());
            if (getArguments().size() > getFirstArgumentIndex()) {
                writer.write(" (");
            }
            writer.write("\n");
            for (int i = getFirstArgumentIndex(); i < getArguments().size(); i++) {
                writer.write("\t");
                FieldDefinition argument = (FieldDefinition)getArguments().elementAt(i);
                Integer argumentType = (Integer)getArgumentTypes().elementAt(i);
                if (argumentType == IN) {
                    printArgument(argument, writer, session);
                } else if (argumentType == OUT) {
                    printOutputArgument(argument, writer, session);
                } else if (argumentType == INOUT) {
                    printInOutputArgument(argument, writer, session);
                }
                if (i < (getArguments().size() - 1)) {
                    writer.write(",\n");
                }
            }
            if (getArguments().size() > getFirstArgumentIndex()) {
                writer.write(")");
            }

            printReturn(writer, session);
            writer.write(platform.getProcedureAsString());
            writer.write("\n");
            if (!getVariables().isEmpty()) {
                writer.write("DECLARE\n");
            }

            for (Enumeration variablesEnum = getVariables().elements();
                     variablesEnum.hasMoreElements();) {
                FieldDefinition field = (FieldDefinition)variablesEnum.nextElement();
                writer.write("\t");
                writer.write(field.getName());
                writer.write(" ");
                writer.write(field.getTypeName());
                writer.write(platform.getBatchDelimiterString());
                writer.write("\n");
            }
            writer.write(platform.getProcedureBeginString());
            writer.write("\n");
            for (Enumeration statementsEnum = getStatements().elements();
                     statementsEnum.hasMoreElements();) {
                writer.write((String)statementsEnum.nextElement());
                writer.write(platform.getBatchDelimiterString());
                writer.write("\n");
            }
            writer.write(platform.getProcedureEndString());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL: Return the drop table statement.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write(getDeletionHeader() + getFullName());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * The arguments are the names of the parameters to the procedure.
     */
    public Vector getArguments() {
        return arguments;
    }

    /**
         *
         */
    public String getCreationHeader() {
        return "CREATE PROCEDURE ";
    }

    /**
         *
         */
    public String getDeletionHeader() {
        return "DROP PROCEDURE ";
    }

    /**
         *
         */
    public int getFirstArgumentIndex() {
        return 0;
    }

    /**
         *
         */
    public Vector getArgumentTypes() {
        return argumentTypes;
    }

    /**
     * The statements are the SQL lines of code in procedure.
     */
    public Vector getStatements() {
        return statements;
    }

    /**
     * The variables are the names of the declared variables used in the procedure.
     */
    public Vector getVariables() {
        return variables;
    }

    /**
     * Print the argument and its type.
     */
    protected void printArgument(FieldDefinition argument, Writer writer, AbstractSession session) throws IOException {
        DatabasePlatform platform = session.getPlatform();
        FieldTypeDefinition fieldType;
        if (argument.getType() != null) {
            fieldType = platform.getFieldTypeDefinition(argument.getType());
            if (fieldType == null) {
                throw ValidationException.javaTypeIsNotAValidDatabaseType(argument.getType());
            }
        } else {
            fieldType = new FieldTypeDefinition(argument.getTypeName());
        }
        writer.write(platform.getProcedureArgumentString());
        writer.write(argument.getName());
        writer.write(" ");
        writer.write(fieldType.getName());

        if (fieldType.isSizeAllowed() && platform.allowsSizeInProcedureArguments() && ((argument.getSize() != 0) || (fieldType.isSizeRequired()))) {
            writer.write("(");
            if (argument.getSize() == 0) {
                writer.write(new Integer(fieldType.getDefaultSize()).toString());
            } else {
                writer.write(new Integer(argument.getSize()).toString());
            }
            if (argument.getSubSize() != 0) {
                writer.write(",");
                writer.write(new Integer(argument.getSubSize()).toString());
            } else if (fieldType.getDefaultSubSize() != 0) {
                writer.write(",");
                writer.write(new Integer(fieldType.getDefaultSubSize()).toString());
            }
            writer.write(")");
        }
    }

    /**
     * Print the argument and its type.
     */
    protected void printInOutputArgument(FieldDefinition argument, Writer writer, AbstractSession session) throws ValidationException {
        try {
            DatabasePlatform platform = session.getPlatform();
            FieldTypeDefinition fieldType;
            if (argument.getType() != null) {
                fieldType = platform.getFieldTypeDefinition(argument.getType());
                if (fieldType == null) {
                    throw ValidationException.javaTypeIsNotAValidDatabaseType(argument.getType());
                }
            } else {
                fieldType = new FieldTypeDefinition(argument.getTypeName());
            }
            writer.write(platform.getProcedureArgumentString());            
            if (platform.shouldPrintOutputTokenAtStart()) {
                writer.write(" " + platform.getCreationInOutputProcedureToken() + " ");
            }
            writer.write(argument.getName());
            if ((!platform.shouldPrintOutputTokenAtStart()) && platform.shouldPrintOutputTokenBeforeType()) {
                writer.write(" " + platform.getCreationInOutputProcedureToken());
            }
            writer.write(" " + fieldType.getName());
            if (fieldType.isSizeAllowed() && platform.allowsSizeInProcedureArguments() && ((argument.getSize() != 0) || (fieldType.isSizeRequired()))) {
                writer.write("(");
                if (argument.getSize() == 0) {
                    writer.write(new Integer(fieldType.getDefaultSize()).toString());
                } else {
                    writer.write(new Integer(argument.getSize()).toString());
                }
                if (argument.getSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(argument.getSubSize()).toString());
                } else if (fieldType.getDefaultSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(fieldType.getDefaultSubSize()).toString());
                }
                writer.write(")");
            }
            if ((!platform.shouldPrintOutputTokenAtStart()) && (!platform.shouldPrintOutputTokenBeforeType())) {
                writer.write(" " + platform.getCreationInOutputProcedureToken());
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * Print the argument and its type.
     */
    protected void printOutputArgument(FieldDefinition argument, Writer writer, AbstractSession session) throws ValidationException {
        try {
            DatabasePlatform platform = session.getPlatform();
            FieldTypeDefinition fieldType;
            if (argument.getType() != null) {
                fieldType = platform.getFieldTypeDefinition(argument.getType());
                if (fieldType == null) {
                    throw ValidationException.javaTypeIsNotAValidDatabaseType(argument.getType());
                }
            } else {
                fieldType = new FieldTypeDefinition(argument.getTypeName());
            }
            writer.write(platform.getProcedureArgumentString());
            if (platform.shouldPrintOutputTokenAtStart()) {
                writer.write(" " + platform.getCreationOutputProcedureToken() + " ");
            }
            writer.write(argument.getName());
            if ((!platform.shouldPrintOutputTokenAtStart()) && platform.shouldPrintOutputTokenBeforeType()) {
                writer.write(" " + platform.getCreationOutputProcedureToken());
            }
            writer.write(" " + fieldType.getName());
            if (fieldType.isSizeAllowed() && platform.allowsSizeInProcedureArguments() && ((argument.getSize() != 0) || (fieldType.isSizeRequired()))) {
                writer.write("(");
                if (argument.getSize() == 0) {
                    writer.write(new Integer(fieldType.getDefaultSize()).toString());
                } else {
                    writer.write(new Integer(argument.getSize()).toString());
                }
                if (argument.getSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(argument.getSubSize()).toString());
                } else if (fieldType.getDefaultSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(fieldType.getDefaultSubSize()).toString());
                }
                writer.write(")");
            }
            if ((!platform.shouldPrintOutputTokenAtStart()) && !platform.shouldPrintOutputTokenBeforeType()) {
                writer.write(" " + platform.getCreationOutputProcedureToken());
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * Prints return for stored function, nothing to do for stored procedure
     */
    protected void printReturn(Writer writer, AbstractSession session) throws ValidationException {
    }

    /**
     * The arguments are the field defs of the parameters names and types to the procedure.
     */
    public void setArguments(Vector arguments) {
        this.arguments = arguments;
    }

    /**
     * The statements are the SQL lines of code in procedure.
     */
    public void setStatements(Vector statements) {
        this.statements = statements;
    }

    /**
     * The variables are the field defs of the declared variables used in the procedure.
     */
    public void setVariables(Vector variables) {
        this.variables = variables;
    }
}