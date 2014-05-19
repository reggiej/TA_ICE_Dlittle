// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server.oc4j;

import java.lang.reflect.Field;

import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.internal.localization.ToStringLocalization;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.sessions.DatabaseSession;

/**
 * PUBLIC: This is the concrete subclass responsible for representing Oc4j
 * version 10.1.3 specific behaviour.
 */
public class Oc4j_10_1_3_Platform extends Oc4jPlatform {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public Oc4j_10_1_3_Platform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL: initializeServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     */
    protected void initializeServerNameAndVersion() {
        this.serverNameAndVersion = ToStringLocalization.buildMessage("unknown");
        try {
            Class cls = Class.forName("com.evermind.server.OC4JServer", true, this.getClass().getClassLoader());
            Field field = cls.getField("INFO");
            this.serverNameAndVersion = (String)field.get(null);
        } catch (Exception ex) {
            ((DatabaseSessionImpl)getDatabaseSession()).log(SessionLog.WARNING, SessionLog.SERVER, "cannot_get_server_name_and_version", ex);
            super.initializeServerNameAndVersion();
        }
    }
}
