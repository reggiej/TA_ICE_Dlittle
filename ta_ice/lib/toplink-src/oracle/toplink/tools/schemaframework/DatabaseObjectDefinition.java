// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.*;

/**
 * <p>
 * <b>Purpose</b>: Define a database object for the purpose of creation and deletion.
 * A database object is an entity such as a table, view, proc, sequence...
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Be able to create and drop the object from the database.
 * </ul>
 */
public abstract class DatabaseObjectDefinition implements Cloneable, Serializable {
    public String name;
    public String qualifier;

    public DatabaseObjectDefinition() {
        this.name = "";
        this.qualifier = "";
    }

    /**
     * INTERNAL:
     * Returns the writer used for creation of this object.
     */
    public abstract Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException;

    /**
     * INTERNAL:
     * Returns the writer used for creation of this object.
     */
    public abstract Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException;

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
     * INTERNAL:
     * Either drop from the database directly or write the statement to a file.
     * Database objects are root level entities such as tables, views, procs, sequences...
     */
    public void createObject(AbstractSession session, Writer schemaWriter) throws TopLinkException {
        if (schemaWriter == null) {
            this.createOnDatabase(session);
        } else {
            this.buildCreationWriter(session, schemaWriter);
        }
    }

    /**
     * INTERNAL:
     * Execute the DDL to create the varray.
     */
    public void createOnDatabase(AbstractSession session) throws TopLinkException {
        session.executeNonSelectingCall(new SQLCall(buildCreationWriter(session, new StringWriter()).toString()));
    }

    /**
     * INTERNAL:
     * Execute the DDL to drop the varray.
     */
    public void dropFromDatabase(AbstractSession session) throws TopLinkException {
        session.executeNonSelectingCall(new SQLCall(buildDeletionWriter(session, new StringWriter()).toString()));
    }

    /**
     * INTERNAL:
     * Execute the DDL to drop the varray.  Either directly from the database
     * of write out the statement to a file.
     */
    public void dropObject(AbstractSession session, Writer schemaWriter) throws TopLinkException {
        if (schemaWriter == null) {
            this.dropFromDatabase(session);
        } else {
            buildDeletionWriter(session, schemaWriter);
        }
    }

    /**
     * INTERNAL:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public String getFullName() {
        if (getQualifier().equals("")) {
            return getName();
        } else {
            return getQualifier() + "." + getName();
        }
    }

    /**
     * PUBLIC:
     * Return the name of the object.
     * i.e. the table name or the sequence name.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * PUBLIC:
     * Set the name of the object.
     * i.e. the table name or the sequence name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getFullName() + ")";
    }
}