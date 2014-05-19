// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.platform;

import oracle.toplink.internal.ox.record.DOMUnmarshaller;
import oracle.toplink.internal.ox.record.PlatformUnmarshaller;
import oracle.toplink.ox.XMLUnmarshaller;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>This class indicates that DOM parsing should be used when appropriate in an
 *  XML project to create XMLRecords.
 *  <b>Responsibilities:</b><ul>
 *  <li>Extend XMLPlatform</li>
 *  <li>Overrides newPlatformUnmarshaller to return an instance of DOMUnmarshaller</li>
 *  </ul>
 *  
 *  @author  mmacivor
 *  @see oracle.toplink.internal.ox.record.DOMUnmarshaller
 *  @see oracle.toplink.ox.record.DOMRecord
 */
public class DOMPlatform extends XMLPlatform {

    /**
     * INTERNAL:
     */
    public PlatformUnmarshaller newPlatformUnmarshaller(XMLUnmarshaller xmlUnmarshaller) {
        return new DOMUnmarshaller(xmlUnmarshaller);
    }
}
