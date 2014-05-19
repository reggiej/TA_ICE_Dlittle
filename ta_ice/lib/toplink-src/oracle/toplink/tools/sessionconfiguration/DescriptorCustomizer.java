// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import oracle.toplink.descriptors.ClassDescriptor;

/**
 * PUBLIC:
 * This interface is to allow extra customization on a TopLink Session
 */
public interface DescriptorCustomizer {
    public void customize(ClassDescriptor Descriptor) throws Exception;
}
