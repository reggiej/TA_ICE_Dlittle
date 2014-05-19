// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.blackbox;

import javax.resource.cci.*;
import com.sun.connector.cciblackbox.CciInteractionSpec;
import oracle.toplink.eis.*;
import oracle.toplink.eis.interactions.*;

/**
 * Defines the blackbox platform.
 * This generates blackbox interaction specs.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class BlackboxPlatform extends EISPlatform {

    /** Blackbox interaction spec properties. */
    public static String CATALOG = "catalog";
    public static String SCHEMA = "schema";

    /**
     * Default constructor.
     */
    public BlackboxPlatform() {
        super();
        this.isMappedRecordSupported = false;
        this.isIndexedRecordSupported = true;
    }

    /**
     * Allow the platform to build the interaction spec based on properties defined in the interaction.
     */
    public InteractionSpec buildInteractionSpec(EISInteraction interaction) {
        CciInteractionSpec spec = (CciInteractionSpec)interaction.getInteractionSpec();
        if (spec == null) {
            String funtionName = interaction.getFunctionName();
            String schema = (String)interaction.getProperty(SCHEMA);
            String catalog = (String)interaction.getProperty(CATALOG);
            spec = new CciInteractionSpec();
            spec.setFunctionName(funtionName);
            spec.setCatalog(catalog);
            spec.setSchema(schema);
        }
        return spec;
    }
}