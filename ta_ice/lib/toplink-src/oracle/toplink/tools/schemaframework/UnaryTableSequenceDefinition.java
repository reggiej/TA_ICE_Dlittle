// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.util.Vector;
import java.io.*;
import java.math.BigDecimal;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.sequencing.Sequence;
import oracle.toplink.sequencing.DefaultSequence;
import oracle.toplink.sequencing.UnaryTableSequence;

/**
 * <p>
 * <b>Purpose</b>: Creates / drops an unary sequence table:
 * the name of the table is sequence name; its only field is named unarySequenceCounterFieldName
 * <p>
 */
public class UnaryTableSequenceDefinition extends SequenceDefinition {
    /**
     * INTERNAL:
     * Should be a sequence defining unary table sequence in the db:
     * either UnaryTableSequence
     * DefaultSequence (only if case platform.getDefaultSequence() is an UnaryTableSequence).
     */
    public UnaryTableSequenceDefinition(Sequence sequence) {
        super(sequence);
    }

    /**
     * INTERNAL:
     * Return the SQL required to create the unary sequence table.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("INSERT INTO ");
            writer.write(getName());
            writer.write("(" + getSequenceCounterFieldName());
            writer.write(") values ("+Integer.toString(sequence.getInitialValue() - 1)+")");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the SQL to delete the unary sequence table.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("DELETE FROM ");
            writer.write(getName());
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERAL:
     * Verify whether the sequence exists.
     * Assume that the unary sequence table exists.
     */
    public boolean checkIfExist(AbstractSession session) throws DatabaseException {
        Vector results = session.executeSelectingCall(new oracle.toplink.queryframework.SQLCall("SELECT * FROM " + getName()));
        return !results.isEmpty();
    }

    /**
     * PUBLIC:
     * Return the name of the only field of this table
     */
    public String getSequenceCounterFieldName() {
        return getUnaryTableSequence().getCounterFieldName();
    }

    /**
     * INTERNAL:
     * Return a TableDefinition specifying a unary sequence table.
     */
    public TableDefinition buildTableDefinition() {
        TableDefinition definition = new TableDefinition();
        definition.setName(getName());
        definition.addField(getSequenceCounterFieldName(), BigDecimal.class);
        return definition;
    }
    
    protected UnaryTableSequence getUnaryTableSequence() {
        if(sequence instanceof UnaryTableSequence) {
            return (UnaryTableSequence)sequence;
        } else {
            return (UnaryTableSequence)((DefaultSequence)sequence).getDefaultSequence();
        }
    }
}