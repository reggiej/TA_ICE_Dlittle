// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.platform;

import oracle.toplink.internal.ox.record.PlatformUnmarshaller;
import oracle.toplink.internal.ox.record.SAXUnmarshaller;
import oracle.toplink.ox.XMLUnmarshaller;

/**
 *  @version 1.0
 *  @author  mmacivor
 *  @since   10.1.3
 *  This class is used to indicate that SAX parsing should be used to create an XML
 *  Record when appropriate.
 */
public class SAXPlatform extends XMLPlatform {

    /**
     * INTERNAL:
     */
    public PlatformUnmarshaller newPlatformUnmarshaller(XMLUnmarshaller xmlUnmarshaller) {
        return new SAXUnmarshaller(xmlUnmarshaller);
    }
}
