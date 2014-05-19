// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.schemaframework;

import java.io.Writer;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.sequencing.Sequence;

/**
 * <p>
 * <b>Purpose</b>: Allow a generic way of creating sequences on the different platforms,
 * and allow optional parameters to be specified.
 * <p>
 */
public abstract class SequenceDefinition extends DatabaseObjectDefinition {
    protected Sequence sequence;
    
    public SequenceDefinition(String name) {
        super();
        this.name = name;
    }

    public SequenceDefinition(Sequence sequence) {
        super();
        this.sequence = sequence;
        this.name = sequence.getName();
    }

    /**
     * INTERAL:
     * Verify whether the sequence exists.
     */
    public abstract boolean checkIfExist(AbstractSession session) throws DatabaseException;

    /**
     * INTERNAL:
     * Indicates whether alter is supported
     */
    public boolean isAlterSupported(AbstractSession session) {
        return false;
    }

    /**
     * INTERNAL:
     * By default does nothing.
     */
    public void alterOnDatabase(AbstractSession session) throws TopLinkException {
    }

    /**
     * INTERNAL:
     * Execute the SQL required to alter sequence.
     * By default does nothing.
     */
    public void alter(AbstractSession session, Writer writer) throws ValidationException {
    }

    /**
     * INTERNAL:
     * Creates this sequence definition on the database.  If it already exists, the method will attempt
     * to alter it based on what the platform supports.  
     */
    public void createOnDatabase(AbstractSession session) throws TopLinkException {
        if (checkIfExist(session)) {
            if (this.isAlterSupported(session)) {
                alterOnDatabase(session);
            }
        }else{
            super.createOnDatabase(session);
        }
    }

    /**
     * INTERNAL:
     * Return a TableDefinition
     */
    public TableDefinition buildTableDefinition() {
        return null;
    }
}