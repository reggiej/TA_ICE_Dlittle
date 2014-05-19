// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.xmlfile;

import javax.resource.cci.*;
import oracle.toplink.ox.NamespaceResolver;
import org.w3c.dom.*;
import oracle.toplink.eis.*;
import oracle.toplink.eis.interactions.*;
import oracle.toplink.internal.eis.adapters.xmlfile.*;
import oracle.toplink.sequencing.Sequence;

/**
 * Platform for XML file emulated JCA adapter.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class XMLFilePlatform extends EISPlatform {

    /** XML file interaction spec properties. */
    public static String FILE_NAME = "fileName";
    public static String XPATH = "xPath";
    public static String XQUERY = "xQuery";
    public static String DOM = "dom";

    /**
     * Default constructor.
     */
    public XMLFilePlatform() {
        super();
        this.shouldConvertDataToStrings = true;
        this.isMappedRecordSupported = false;
        this.isIndexedRecordSupported = false;
        this.isDOMRecordSupported = true;
        this.supportsLocalTransactions = true;
    }

    /**
     * Allow the platform to build the interaction spec based on properties defined in the interaction.
     */
    public InteractionSpec buildInteractionSpec(EISInteraction interaction) {
        InteractionSpec spec = (InteractionSpec)interaction.getInteractionSpec();
        if (spec == null) {
            NamespaceResolver namespaceResolver = null;
            try {
                namespaceResolver = ((EISDescriptor)interaction.getQuery().getDescriptor()).getNamespaceResolver();
            } catch (Exception e) {
                //do nothing, the namespaceResolver will just be null
            }
            XMLFileInteractionSpec fileSpec = new XMLFileInteractionSpec(namespaceResolver);
            fileSpec.setFileName((String)interaction.getProperty(FILE_NAME));
            fileSpec.setXPath((String)interaction.getProperty(XPATH));
            fileSpec.setXQuery((String)interaction.getProperty(XQUERY));
            fileSpec.setDOM((Element)interaction.getProperty(DOM));
            if (interaction.getQuery().isDeleteObjectQuery()) {
                fileSpec.setInteractionType(XMLFileInteractionSpec.DELETE);
            } else if (interaction.getQuery().isInsertObjectQuery()) {
                fileSpec.setInteractionType(XMLFileInteractionSpec.INSERT);
            } else if (interaction.getQuery().isModifyQuery()) {
                fileSpec.setInteractionType(XMLFileInteractionSpec.UPDATE);
            } else if (interaction.getQuery().isReadQuery()) {
                fileSpec.setInteractionType(XMLFileInteractionSpec.READ);
            }
            if (interaction instanceof XQueryInteraction) {
                fileSpec.setXPath(((XQueryInteraction)interaction).getXQueryString());
            }
            spec = fileSpec;
        }
        return spec;
    }

    /**
     * INTERNAL:
     * Create platform-default Sequence
     */
    protected Sequence createPlatformDefaultSequence() {
        return new XMLFileSequence();
    }
}