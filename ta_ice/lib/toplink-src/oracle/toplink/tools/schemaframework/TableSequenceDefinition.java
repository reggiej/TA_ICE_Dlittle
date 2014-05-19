// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.util.Vector;
import java.io.*;
import java.math.BigDecimal;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.sequencing.Sequence;
import oracle.toplink.sequencing.DefaultSequence;
import oracle.toplink.sequencing.TableSequence;

/**
 * <p>
 * <b>Purpose</b>: Allow a generic way of creating sequences on the different platforms,
 * and allow optional parameters to be specified.
 * <p>
 */
public class TableSequenceDefinition extends SequenceDefinition {

    /**
     * INTERNAL:
     * Should be a sequence defining table sequence in the db:
     * either TableSequence
     * DefaultSequence (only if case platform.getDefaultSequence() is a TableSequence).
     */
    public TableSequenceDefinition(Sequence sequence) {
        super(sequence);
    }

    /**
     * INTERNAL:
     * Return the SQL required to insert the sequence row into the sequence table.
     * Assume that the sequence table exists.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("INSERT INTO ");
            writer.write(getSequenceTableName());
            writer.write("(" + getSequenceNameFieldName());
            writer.write(", " + getSequenceCounterFieldName());
            writer.write(") values (");
            writer.write("'" + getName() + "', "  + Integer.toString(sequence.getInitialValue() - 1) + ")");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the SQL to delete the row from the sequence table.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("DELETE FROM ");
            writer.write(getSequenceTableName());
            writer.write(" WHERE " + getSequenceNameFieldName());
            writer.write(" = '" + getName() + "'");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERAL:
     * Execute the SQL required to insert the sequence row into the sequence table.
     * Assume that the sequence table exists.
     */
    public boolean checkIfExist(AbstractSession session) throws DatabaseException {
        Vector results = session.executeSelectingCall(new oracle.toplink.queryframework.SQLCall("SELECT * FROM " + getSequenceTableName() + " WHERE " + getSequenceNameFieldName() + " = '" + getName() + "'"));
        return !results.isEmpty();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceTableName() {
        return getTableSequence().getTableName();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceCounterFieldName() {
        return getTableSequence().getCounterFieldName();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceNameFieldName() {
        return getTableSequence().getNameFieldName();
    }

    /**
     * INTERNAL:
     * Return a TableDefinition specifying sequence table.
     */
    public TableDefinition buildTableDefinition() {
        TableDefinition definition = new TableDefinition();
        definition.setName(getSequenceTableName());
        definition.addPrimaryKeyField(getSequenceNameFieldName(), String.class, 50);
        definition.addField(getSequenceCounterFieldName(), BigDecimal.class);
        return definition;
    }
    
    protected TableSequence getTableSequence() {
        if(sequence instanceof TableSequence) {
            return (TableSequence)sequence;
        } else {
            return (TableSequence)((DefaultSequence)sequence).getDefaultSequence();
        }
    }
}