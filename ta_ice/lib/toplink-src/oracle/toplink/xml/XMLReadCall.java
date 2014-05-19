// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;
import oracle.toplink.mappings.OneToOneMapping;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.localization.TraceLocalization;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * XMLReadCall can perform one of two types of read:<ol>
 * <li>Read the XML document for a given primary key.
 * <li>Read the XML document for a given foreign key,
 * specified by a 1:1 mapping relationship.
 * </ol>
 *
 * @see oracle.toplink.mappings.OneToOneMapping
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLReadCall extends XMLCall {

    /** If this is present, read only the data for the relationship. */
    private OneToOneMapping mapping;

    /**
     * Default constructor.
     */
    public XMLReadCall() {
        super();
    }

    /**
     * Constructor. Specify the associated mapping.
     */
    public XMLReadCall(OneToOneMapping mapping) {
        this();
        this.initialize(mapping);
    }

    /**
     * Read and return the necessary rows of data.
     * If the mapping is missing,
     * the translation row holds the primary key for the data.
     * If the mapping is present,
     * the translation row holds a foreign key for the data.
     */
    public Object execute(AbstractRecord translationRow, Accessor accessor) throws XMLDataStoreException {
        Reader stream = null;
        if (this.getMapping() == null) {
            stream = this.getReadStream(accessor, translationRow, this.getOrderedPrimaryKeyElements());
        } else {
            stream = this.getReadStream(accessor, translationRow, this.getOrderedForeignKeyElements());
        }
        if (stream == null) {
            return null;
        }
        return this.getFieldTranslator().translateForRead(this.getXMLTranslator().read(stream));
    }

    /**
     * Return the mapping the call fetches data for.
     */
    protected OneToOneMapping getMapping() {
        return mapping;
    }

    /**
     * Convenience method.
     * Return the appropriate foreign key elements,
     * in the same order as they are stored in the
     * descriptor.
     */
    protected Vector getOrderedForeignKeyElements() {
        return this.getMapping().getOrderedForeignKeyFields();
    }

    /**
     * Return the appropriate stream.
     */
    protected Reader getReadStream(Accessor accessor, AbstractRecord translationRow, Vector orderedKeyElements) throws XMLDataStoreException {
        return this.getStreamPolicy().getReadStream(this.getRootElementName(), translationRow, orderedKeyElements, accessor);
    }

    protected void initialize(OneToOneMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Set the mapping the call fetches data for.
     */
    public void setMapping(OneToOneMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Append a string describing the call to the specified writer.
     */
    protected void writeLogDescription(PrintWriter writer) {
        writer.write(TraceLocalization.buildMessage("XML_read", (Object[])null));
        if (this.getMapping() != null) {
            writer.write("(");
            writer.print(this.getMapping());
            writer.write(")");
        }
    }
}