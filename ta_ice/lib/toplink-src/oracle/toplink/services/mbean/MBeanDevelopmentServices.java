// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.services.mbean;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.services.DevelopmentServices;

/**
 * <p>
 * <b>Purpose</b>: Provide a dynamic interface into the TopLink Identity Map Manager.
 * <p>
 * <b>Description</b>: This class is ment to provide a framework for gaining access to configuration and
 * statistics of the TopLink Cache during runtime.  It will provide the basis for developement
 * of a JMX service and possibly other frameworks.
 * <ul>
 * <li>
 * </ul>
 *
 * @deprecated Will be replaced by a server-specific equivalent for oracle.toplink.services.oc4j.Oc4jRuntimeServices
 * @see oracle.toplink.services.oc4j.Oc4jRuntimeServices
 */
public class MBeanDevelopmentServices extends DevelopmentServices implements MBeanDevelopmentServicesMBean {
    public MBeanDevelopmentServices(AbstractSession session) {
        super(session);
    }
}