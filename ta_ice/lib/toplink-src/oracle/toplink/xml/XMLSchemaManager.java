// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import oracle.toplink.tools.schemaframework.*;
import oracle.toplink.sequencing.*;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;

/**
 * This class extends the base TOPLink <code>SchemaManager</code>
 * to create XML stream sources and sequences for XML Projects.
 *
 * @author Les Davis
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLSchemaManager extends SchemaManager {

    /**
     * Construct a schema manager for the specified session.
     */
    public XMLSchemaManager(DatabaseSessionImpl session) {
        super(session);
    }

    /**
     * Construct a schema manager for the specified session.
     */
    public XMLSchemaManager(oracle.toplink.sessions.DatabaseSession session) {
        super(session);
    }

    protected SequenceDefinition buildSequenceDefinition(Sequence sequence) {
        if (sequence instanceof DefaultSequence) {
            String name = sequence.getName();
            int size = sequence.getPreallocationSize();
            sequence = getSession().getDatasourcePlatform().getDefaultSequence();
            if (sequence instanceof XMLSequence) {
                XMLSequence xmlSequence = (XMLSequence)sequence;
                return new XMLSequenceDefinition(name, xmlSequence);
            } else {
                return null;
            }
        } else if (sequence instanceof XMLSequence) {
            XMLSequence xmlSequence = (XMLSequence)sequence;
            return new XMLSequenceDefinition(xmlSequence);
        } else {
            return null;
        }
    }

    public void createObject(DatabaseObjectDefinition databaseObjectDefinition) throws TopLinkException {
        if (shouldWriteToDatabase()) {
            if (databaseObjectDefinition instanceof TableDefinition) {
                this.createStreamSource((TableDefinition)databaseObjectDefinition);
            } else if (databaseObjectDefinition instanceof XMLSequenceDefinition) {
                databaseObjectDefinition.createOnDatabase(getSession());
            }
        }
    }

    public void dropObject(DatabaseObjectDefinition databaseObjectDefinition) throws TopLinkException {
        if (shouldWriteToDatabase()) {
            if (databaseObjectDefinition instanceof TableDefinition) {
                this.dropStreamSource((TableDefinition)databaseObjectDefinition);
            }
        }
    }

    /**
     * Delegate to the XML accessor.
     */
    protected void createStreamSource(TableDefinition tableDefinition) throws TopLinkException {
        XMLAccessor accessor = (XMLAccessor)this.getSession().getAccessor();
        accessor.createStreamSource(tableDefinition.getName());
    }

    /**
     * Delete all sequence files and the sequence directory.
     */
    protected void dropStreamSource(TableDefinition tableDefinition) {
        XMLAccessor accessor = (XMLAccessor)this.getSession().getAccessor();
        accessor.dropStreamSource(tableDefinition.getName());
    }
}