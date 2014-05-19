/// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import java.util.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Define a unique key constraint for a table.
 */
public class UniqueKeyConstraint implements Serializable {
    protected String name;
    protected Vector<String> sourceFields; // field names

    public UniqueKeyConstraint() {
        this.name = "";
        this.sourceFields = new Vector<String>();
    }

    public UniqueKeyConstraint(String name, String sourceField) {
        this();
        this.name = name;
        sourceFields.addElement(sourceField);
    }

    public UniqueKeyConstraint(String name, String[] sourceFields) {
        this();
        this.name = name;
        for(String sourceField : sourceFields) {
            this.sourceFields.addElement(sourceField);
        }
    }
    
    /**
     * PUBLIC:
     */
    public void addSourceField(String sourceField) {
        getSourceFields().addElement(sourceField);
    }

    /**
     * INTERNAL:
     * Append the database field definition string to the table creation statement.
     */
    public void appendDBString(Writer writer, AbstractSession session) {
        try {
            writer.write("UNIQUE (");
            for (Enumeration sourceEnum = getSourceFields().elements();
                     sourceEnum.hasMoreElements();) {
                writer.write((String)sourceEnum.nextElement());
                if (sourceEnum.hasMoreElements()) {
                    writer.write(", ");
                }
            }
            writer.write(")");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * PUBLIC:
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     */
    public Vector<String> getSourceFields() {
        return sourceFields;
    }

    /**
     * PUBLIC:
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     */
    public void setSourceFields(Vector<String> sourceFields) {
        this.sourceFields = sourceFields;
    }
}

