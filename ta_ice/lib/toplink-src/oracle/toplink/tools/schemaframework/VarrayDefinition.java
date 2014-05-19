// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: Allow for creation of varray type.
 * <p>
 */
public class VarrayDefinition extends DatabaseObjectDefinition {
    protected int typeSize;
    protected int size;
    protected Class type;
    protected String typeName;

    public VarrayDefinition() {
        super();
    }

    /**
     * INTERNAL:
     * Append the type.
     */
    public void appendTypeString(Writer writer, AbstractSession session) throws ValidationException {
        try {
            FieldTypeDefinition fieldType;
            if (getType() != null) {
                fieldType = session.getPlatform().getFieldTypeDefinition(getType());
                if (fieldType == null) {
                    throw ValidationException.javaTypeIsNotAValidDatabaseType(getType());
                }
            } else {
                fieldType = new FieldTypeDefinition(getTypeName());
            }
            writer.write(fieldType.getName());
            if ((fieldType.isSizeAllowed()) && ((getTypeSize() != 0) || (fieldType.isSizeRequired()))) {
                writer.write("(");
                if (getTypeSize() == 0) {
                    writer.write(new Integer(fieldType.getDefaultSize()).toString());
                } else {
                    writer.write(new Integer(getTypeSize()).toString());
                }
                writer.write(")");
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * INTERNAL:
     * Return the DDL to create the varray.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("CREATE TYPE ");
            writer.write(getFullName());
            writer.write(" AS VARRAY(");

            //when defining a VARRAY type, a maximum size MUST be specified
            if (getSize() < 1) {
                throw ValidationException.oracleVarrayMaximumSizeNotDefined(getFullName());
            }

            writer.write(new Integer(getSize()).toString());
            writer.write(") OF ");
            appendTypeString(writer, session);
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the DDL to drop the varray.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("DROP TYPE " + getFullName());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * PUBLIC:
     * Return the maximum size of the array.
     */
    public int getSize() {
        return size;
    }

    /**
     * PUBLIC:
     * Return the type of the field.
     * This should be set to a java class, such as String.class, Integer.class or Date.class.
     */
    public Class getType() {
        return type;
    }

    /**
     * PUBLIC:
     * Return the type of the field.
     * This is the exact DB type name, which can be used instead of the Java class.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * PUBLIC:
     * Return the size of the element field, this is only required for some field types.
     */
    public int getTypeSize() {
        return typeSize;
    }

    /**
     * PUBLIC:
     * Set the maximum size of the array.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * PUBLIC:
     * Set the type of the field.
     * This should be set to a java class, such as String.class, Integer.class or Date.class.
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * PUBLIC:
     * Set the type of the field.
     * This is the exact DB type name, which can be used instead of the Java class.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * PUBLIC:
     * Set the size of the element field, this is only required for some field types.
     */
    public void setTypeSize(int typeSize) {
        this.typeSize = typeSize;
    }
}