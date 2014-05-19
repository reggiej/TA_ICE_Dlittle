// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.services.mbean;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.services.RuntimeServices;

/**
 * <p>
 * <b>Purpose</b>: Provide a dynamic interface into the TopLink Session.
 * <p>
 * <b>Description</b>: This class is ment to provide a framework for gaining access to configuration
 * of the TopLink Session during runtime.  It will provide the basis for developement
 * of a JMX service and possibly other frameworks.
 * <ul>
 * <li>
 * </ul>
 *
 * @deprecated Will be replaced by a server-specific equivalent for oracle.toplink.services.oc4j.Oc4jRuntimeServices
 * @see oracle.toplink.services.oc4j.Oc4jRuntimeServices
 */
public class MBeanRuntimeServices extends RuntimeServices implements MBeanRuntimeServicesMBean {
    public MBeanRuntimeServices(AbstractSession session) {
        super(session);
    }
}