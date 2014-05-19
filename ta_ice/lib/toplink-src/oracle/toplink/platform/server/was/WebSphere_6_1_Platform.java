// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server.was;

import oracle.toplink.sessions.DatabaseSession;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing WebSphere 
 * 6.1-specific server behaviour.
 *
 * This platform has:
 * - No JMX MBean runtime services
 *
 */
public class WebSphere_6_1_Platform extends WebSpherePlatform {
    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public WebSphere_6_1_Platform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }
}
