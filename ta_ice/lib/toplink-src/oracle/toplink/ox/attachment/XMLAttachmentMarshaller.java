// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.attachment;

import javax.activation.DataHandler;

public interface XMLAttachmentMarshaller {
    public String addMtomAttachment(DataHandler data, String elementName, String namespace);    
    
    public String addSwaRefAttachment(DataHandler data);
    
    public String addMtomAttachment(byte[] data, int start, int length, String mimeType, String elementName, String namespace);    

    public String addSwaRefAttachment(byte[] data, int start, int length);
    
    public boolean isXOPPackage();

}
