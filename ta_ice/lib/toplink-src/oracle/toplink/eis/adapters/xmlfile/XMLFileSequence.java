// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.xmlfile;

import oracle.toplink.queryframework.*;
import oracle.toplink.eis.EISSequence;
import oracle.toplink.eis.interactions.*;

/**
 * Provides sequence support for EISPlatform
 */
public class XMLFileSequence extends EISSequence {

    /**
     * Default constructor.
     */
    public XMLFileSequence() {
        super();
    }

    public XMLFileSequence(String name) {
        super(name);
    }

    public XMLFileSequence(String name, int size) {
        super(name, size);
    }

    public boolean equals(Object obj) {
        if (obj instanceof XMLFileSequence) {
            return equalNameAndSize(this, (XMLFileSequence)obj);
        } else {
            return false;
        }
    }

    /**
     * Support sequencing through sequence file.
     */
    protected ValueReadQuery buildSelectQuery() {
        ValueReadQuery query = new ValueReadQuery();
        query.addArgument("sequence-name");
        XQueryInteraction interaction = new XQueryInteraction();
        interaction.setFunctionName("select-sequence");
        interaction.setProperty("fileName", "sequence.xml");
        interaction.setXQueryString("sequence[sequence-name='#sequence-name']/sequence-count");
        query.setCall(interaction);

        return query;
    }

    /**
     * Support sequencing through sequence file.
     */
    protected DataModifyQuery buildUpdateQuery() {
        DataModifyQuery query = new DataModifyQuery();
        query.addArgument("sequence-name");
        query.addArgument("sequence-count");
        XQueryInteraction interaction = new XQueryInteraction();
        interaction.setFunctionName("update-sequence");
        interaction.setProperty("fileName", "sequence.xml");
        interaction.setXQueryString("sequence[sequence-name='#sequence-name']");
        interaction.setInputRootElementName("sequence");
        interaction.addArgument("sequence-name");
        interaction.addArgument("sequence-count");
        interaction.setOutputResultPath("result");
        query.setCall(interaction);

        return query;
    }
}