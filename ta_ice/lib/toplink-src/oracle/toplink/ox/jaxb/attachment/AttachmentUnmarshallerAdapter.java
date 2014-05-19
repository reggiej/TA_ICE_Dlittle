// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.attachment;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide an implementation of the TopLink OX XMLAttachmentUnmarshaller
 * interface that wraps an implementation of the JAXB AttachmentUnmarshaller interface. 
 * <p><b>Responsibilities:</b><ul>
 * <li>Implement the XMLAttachmentUnmarshaller interface</li>
 * <li>Adapt events from the TopLink OX Attachment API to the JAXB 2.0 Attachment API</li>
 * </ul>
 * <p>This class allows TopLink OXM to do attachment unmarshalling callback events to a JAXB
 * 2.0 Listener without adding a dependancy on JAXB 2.0 into core TopLink. The Adapter class
 * wraps a javax.xml.bin.attachment.AttachmentUnmarshaller and passes on the events as they're raised
 * 
 * @see javax.xml.bind.attachment.AttachmentUnmarshaller
 * @see oracle.toplink.ox.attachment.XMLAttachmentUnmarshaller
 * @since Oracle TopLink 11.1.1.0.0
 * @author mmacivor
 *
 */
public class AttachmentUnmarshallerAdapter implements oracle.toplink.ox.attachment.XMLAttachmentUnmarshaller {

    private AttachmentUnmarshaller attachmentUnmarshaller;
    
    public AttachmentUnmarshallerAdapter(AttachmentUnmarshaller at) {
        this.attachmentUnmarshaller = at;
    }
    public byte[] getAttachmentAsByteArray(String id) {
        return attachmentUnmarshaller.getAttachmentAsByteArray(id);
    }
    
    public DataHandler getAttachmentAsDataHandler(String id) {
        return attachmentUnmarshaller.getAttachmentAsDataHandler(id);
    }
    
    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return attachmentUnmarshaller;
    }
    
    public boolean isXOPPackage() {
        return attachmentUnmarshaller.isXOPPackage();
    }

}
