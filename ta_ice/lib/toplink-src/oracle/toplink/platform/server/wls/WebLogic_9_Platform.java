// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server.wls;

import oracle.toplink.sessions.DatabaseSession;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing WebLogic9 specific behaviour.
 *
 */
public class WebLogic_9_Platform extends WebLogicPlatform {
    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public WebLogic_9_Platform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }
}
