// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.attachment;

import javax.activation.DataHandler;

public interface XMLAttachmentUnmarshaller {
    public DataHandler getAttachmentAsDataHandler(String id);

    public byte[] getAttachmentAsByteArray(String id);
    
    public boolean isXOPPackage();
}
