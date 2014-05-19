// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors.copying;

import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.sessions.*;

/**
 * <p><b>Purpose</b>: Creates a copy through creating a new instance.
 */
public class InstantiationCopyPolicy extends AbstractCopyPolicy {
    public InstantiationCopyPolicy() {
        super();
    }

    public Object buildClone(Object domainObject, Session session) throws DescriptorException {
        return getDescriptor().getObjectBuilder().buildNewInstance();
    }

    public boolean buildsNewInstance() {
        return true;
    }

    public String toString() {
        return Helper.getShortClassName(this) + "()";
    }
}