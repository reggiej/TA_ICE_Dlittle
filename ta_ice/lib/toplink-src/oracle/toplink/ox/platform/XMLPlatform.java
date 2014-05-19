// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.platform;

import oracle.toplink.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.ox.record.PlatformUnmarshaller;
import oracle.toplink.ox.XMLUnmarshaller;

public abstract class XMLPlatform extends DatasourcePlatform {
    public ConversionManager getConversionManager() {
        // Lazy init for serialization.
        if (conversionManager == null) {
            //Clone the default to allow customers to easily override the conversion manager
            conversionManager = (XMLConversionManager)XMLConversionManager.getDefaultXMLManager().clone();
        }
        return conversionManager;
    }

    /**
     * INTERNAL:
     */
    public abstract PlatformUnmarshaller newPlatformUnmarshaller(XMLUnmarshaller xmlUnmarshaller);
}
