// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: Allow a semi-generic way of creating packages.
 * <p>
 */
public class PackageDefinition extends DatabaseObjectDefinition {
    protected Vector statements;
    protected Vector procedures;

    public PackageDefinition() {
        this.statements = new Vector();
        this.procedures = new Vector();
    }

    /**
     * Packages can contain sets of procedures.
     */
    public void addProcedures(StoredProcedureDefinition procedure) {
        getProcedures().addElement(procedure);
    }

    /**
     * The statements are the SQL lines of code.
     */
    public void addStatement(String statement) {
        getStatements().addElement(statement);
    }

    /**
     * INTERNAL:
     * Return the create table statement.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            DatabasePlatform platform = session.getPlatform();
            writer.write("CREATE PACKAGE " + getFullName());
            writer.write(" AS");
            writer.write("\n");
            for (Enumeration statementsEnum = getStatements().elements();
                     statementsEnum.hasMoreElements();) {
                writer.write((String)statementsEnum.nextElement());
                writer.write(platform.getBatchDelimiterString());
                writer.write("\n");
            }
            for (Enumeration proceduresEnum = getProcedures().elements();
                     proceduresEnum.hasMoreElements();) {
                writer.write("\n");
                String procedureString = ((StoredProcedureDefinition)proceduresEnum.nextElement()).buildCreationWriter(session, writer).toString();
                writer.write(procedureString.substring(7, procedureString.length()));
                writer.write("\n");
            }
            writer.write(platform.getBatchEndString());
            writer.write("\n" + session.getPlatform().getStoredProcedureTerminationToken());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the drop table statement.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("DROP PACKAGE " + getFullName());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * Packages can contain sets of procedures.
     */
    public Vector getProcedures() {
        return procedures;
    }

    /**
     * The statements are the SQL lines of code.
     */
    public Vector getStatements() {
        return statements;
    }

    /**
     * Packages can contain sets of procedures.
     */
    public void setProcedures(Vector procedures) {
        this.procedures = procedures;
    }

    /**
     * The statements are the SQL lines of code.
     */
    public void setStatements(Vector statements) {
        this.statements = statements;
    }
}