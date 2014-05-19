// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import java.util.Hashtable;

import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: Define a database field definition for creation within a table.
 * This differs from DatabaseField in that it is used only table creation not a runtime.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Store the name, java type, size and sub-size.
 * The sizes are optional and the name of the java class is used for the type.
 * </ul>
 */
public class FieldDefinition implements Serializable, Cloneable {
    protected String name;
    /** 
     * Java type class for the field.
     * Particular database type is generated based on platform from this.
     */
    protected Class type;
    /**
     * Generic database type name for the field, which can be used instead of the Java class 'type'.
     * This is translated to a particular database type based on platform.
     */
    protected String typeName; 
    /** 
     * Database-specific complete type definition like "VARCHAR2(50) UNIQUE NOT NULL".  
     * If this is given, other additional type constraint fields(size, unique, null) are meaningless.  
     */
    protected String typeDefinition;
    protected int size;
    protected int subSize;
    protected boolean shouldAllowNull;
    protected boolean isIdentity;
    protected boolean isPrimaryKey;
    protected boolean isUnique;
    protected String additional;
    protected String constraint;
    protected String foreignKeyFieldName;

    public FieldDefinition() {
        this.name = "";
        this.size = 0;
        this.subSize = 0;
        this.shouldAllowNull = true;
        this.isIdentity = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
    }

    public FieldDefinition(String name, Class type) {
        this.name = name;
        this.type = type;
        this.size = 0;
        this.subSize = 0;
        shouldAllowNull = true;
        isIdentity = false;
        isPrimaryKey = false;
        isUnique = false;
    }

    public FieldDefinition(String name, Class type, int size) {
        this();
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public FieldDefinition(String name, Class type, int size, int subSize) {
        this();
        this.name = name;
        this.type = type;
        this.size = size;
        this.subSize = subSize;
    }

    public FieldDefinition(String name, String typeName) {
        this();
        this.name = name;
        this.typeName = typeName;
    }

    /**
     * INTERNAL:
     * Append the database field definition string to the table creation statement.
     */
    public void appendDBString(Writer writer, AbstractSession session, TableDefinition table) throws ValidationException {
        try {
            writer.write(getName());
            writer.write(" ");

            if (getTypeDefinition() != null) { //apply user-defined complete type definition
                writer.write(getTypeDefinition());

            } else {
                // compose type definition - type name, size, unique, identity, constraints...
                FieldTypeDefinition fieldType;
                
                if (getType() != null) { //translate Java 'type'
                    fieldType = session.getPlatform().getFieldTypeDefinition(getType());
                    if (fieldType == null) {
                        throw ValidationException.javaTypeIsNotAValidDatabaseType(getType());
                    }
                } else if (getTypeName() != null) { //translate generic type name
                    Hashtable fieldTypes = session.getPlatform().getClassTypes();
                    Class type = (Class)fieldTypes.get(getTypeName());
                    if (type == null) { // if unknown type name, use as it is
                        fieldType = new FieldTypeDefinition(getTypeName());
                    } else {
                        fieldType = session.getPlatform().getFieldTypeDefinition(type);
                        if (fieldType == null) {
                            throw ValidationException.javaTypeIsNotAValidDatabaseType(type);
                        }
                    }
                } else {
                    // both type and typeName is null
                    throw ValidationException.javaTypeIsNotAValidDatabaseType(null);
                }

                String qualifiedName = table.getFullName() + '.' + getName();
                boolean shouldPrintFieldIdentityClause = isIdentity() && session.getPlatform().shouldPrintFieldIdentityClause(session, qualifiedName);
                session.getPlatform().printFieldTypeSize(writer, this, fieldType, shouldPrintFieldIdentityClause);
                if(isUnique()) {
                    session.getPlatform().printFieldUnique(writer, shouldPrintFieldIdentityClause);
                }
                if (shouldPrintFieldIdentityClause) {
                    session.getPlatform().printFieldIdentityClause(writer);
                }
                if (shouldAllowNull() && fieldType.shouldAllowNull()) {
                    session.getPlatform().printFieldNullClause(writer);
                } else {
                    session.getPlatform().printFieldNotNullClause(writer);
                }
                if (getConstraint() != null) {
                    writer.write(" " + getConstraint());
                }
                if (getAdditional() != null) {
                    writer.write(" " + getAdditional());
                }
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }


    /**
     * INTERNAL:
     * Append the database field definition string to the type creation statement.
     * Types do not support constraints.
     */
    public void appendTypeString(Writer writer, AbstractSession session) throws ValidationException {
        FieldTypeDefinition fieldType;
        if (getType() != null) {
            fieldType = session.getPlatform().getFieldTypeDefinition(getType());
            if (fieldType == null) {
                throw ValidationException.javaTypeIsNotAValidDatabaseType(getType());
            }
        } else {
            fieldType = new FieldTypeDefinition(getTypeName());
        }
        try {
            writer.write(getName());
            writer.write(" ");
            writer.write(fieldType.getName());
            if ((fieldType.isSizeAllowed()) && ((getSize() != 0) || (fieldType.isSizeRequired()))) {
                writer.write("(");
                if (getSize() == 0) {
                    writer.write(new Integer(fieldType.getDefaultSize()).toString());
                } else {
                    writer.write(new Integer(getSize()).toString());
                }
                if (getSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(getSubSize()).toString());
                } else if (fieldType.getDefaultSubSize() != 0) {
                    writer.write(",");
                    writer.write(new Integer(fieldType.getDefaultSubSize()).toString());
                }
                writer.write(")");
            }
            if (getAdditional() != null) {
                writer.write(" " + getAdditional());
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * PUBLIC:
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException impossible) {
            return null;
        }
    }

    /**
     * PUBLIC:
     * Return any additional information about this field to be given when the table is created.
     */
    public String getAdditional() {
        return additional;
    }

    /**
     * PUBLIC:
     * Return any constraint of this field.
     * i.e. "BETWEEN 0 AND 1000000".
     */
    public String getConstraint() {
        return constraint;
    }

    public String getForeignKeyFieldName() {
        return foreignKeyFieldName;
    }

    /**
     * PUBLIC:
     * Return the name of the field.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Return the size of the field, this is only required for some field types.
     */
    public int getSize() {
        return size;
    }

    /**
     * PUBLIC:
     * Return the sub-size of the field.
     * This is used as the decimal precision for numeric values only.
     */
    public int getSubSize() {
        return subSize;
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
     * Return the type name of the field.
     * This is the generic database type name, which can be used instead of the Java class 'type'.
     * This is translated to a particular database type based on platform.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * PUBLIC:
     * Return the type definition of the field.
     * This is database-specific complete type definition like "VARCHAR2(50) UNIQUE NOT NULL".  
     * If this is given, other additional type constraint fields(size, unique, null) are meaningless.  
     */
    public String getTypeDefinition() {
        return typeDefinition;
    }

    /**
     * PUBLIC:
     * Answer whether the receiver is an identity field.
     * Identity fields are Sybase specific,
     * they insure that on insert a unique sequencial value is store in the row.
     */
    public boolean isIdentity() {
        return isIdentity;
    }

    /**
     * PUBLIC:
     * Answer whether the receiver is a primary key.
     * If the table has a multipart primary key this should be set in each field.
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * PUBLIC:
     * Answer whether the receiver is a unique constraint field.
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * PUBLIC:
     * Set any additional information about this field to be given when the table is created.
     */
    public void setAdditional(String string) {
        additional = string;
    }

    /**
     * PUBLIC:
     * Set any constraint of this field.
     * i.e. "BETWEEN 0 AND 1000000".
     */
    public void setConstraint(String string) {
        constraint = string;
    }

    public void setForeignKeyFieldName(String foreignKeyFieldName) {
        this.foreignKeyFieldName = foreignKeyFieldName;
    }

    /**
     * PUBLIC:
     * Set whether the receiver is an identity field.
     * Identity fields are Sybase specific,
     * they insure that on insert a unique sequencial value is store in the row.
     */
    public void setIsIdentity(boolean value) {
        isIdentity = value;
        if (value) {
            setShouldAllowNull(false);
        }
    }

    /**
     * PUBLIC:
     * Set whether the receiver is a primary key.
     * If the table has a multipart primary key this should be set in each field.
     */
    public void setIsPrimaryKey(boolean value) {
        isPrimaryKey = value;
        if (value) {
            setShouldAllowNull(false);
        }
    }

    /**
     * PUBLIC:
     * Set the name of the field.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Set whether the receiver should allow null values.
     */
    public void setShouldAllowNull(boolean value) {
        shouldAllowNull = value;
    }

    /**
     * PUBLIC:
     * Set the size of the field, this is only required for some field types.
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * PUBLIC:
     * Set the sub-size of the field.
     * This is used as the decimal precision for numeric values only.
     */
    public void setSubSize(int subSize) {
        this.subSize = subSize;
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
     * Set the type name of the field.
     * This is the generic database type name, which can be used instead of the Java class 'type'.
     * This is translated to a particular database type based on platform.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * PUBLIC:
     * Set the type definition of the field.
     * This is database-specific complete type definition like "VARCHAR2(50) UNIQUE NOT NULL".  
     * If this is given, other additional type constraint fields(size, unique, null) are meaningless.  
     */
    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    /**
     * PUBLIC:
     * Set whether the receiver is a unique constraint field.
     */
    public void setUnique(boolean value) {
        isUnique = value;
    }

    /**
     * PUBLIC:
     * Return whether the receiver should allow null values.
     */
    public boolean shouldAllowNull() {
        return shouldAllowNull;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getName() + "(" + getType() + "))";
    }
}
