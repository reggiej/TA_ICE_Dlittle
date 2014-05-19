// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.unmapped;

import oracle.toplink.ox.record.UnmarshalRecord;
import org.xml.sax.ContentHandler;

/**
 * <p><b>Purpose:</b>Provide an interface that can be implemented for handling
 * unmapped content during unmarshal operations with SAXPlatform.
 */
public interface UnmappedContentHandler extends ContentHandler {

    /**
     * Set the UnmarshalRecord which gives access to mechanisms used during the
     * unmarshal process such as an XMLUnmarshaller and a Session.
     * @param unmarshalRecord
     */
    void setUnmarshalRecord(UnmarshalRecord unmarshalRecord);
}