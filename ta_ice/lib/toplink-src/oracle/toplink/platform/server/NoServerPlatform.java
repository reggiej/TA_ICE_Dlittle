// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server;

import java.sql.Connection;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.logging.DefaultSessionLog;

/**
 *
 * PUBLIC:
 *
 * This platform is used when TopLink is not within any server (Oc4j, WebLogic, ...)
 * This is also the default platform for all newly created DatabaseSessions.
 *
 * This platform has:
 *
 * - No external transaction controller class
 * - No runtime services (JMX/MBean)
 * - No launching of container Threads
 *
 */
public class NoServerPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: Initialize so that runtime services and JTA are disabled.
     */
    public NoServerPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
        this.disableJTA();
    }

    
    /**
     * PUBLIC: getServerNameAndVersion(): Answer null because this does not apply to NoServerPlatform.
     *
     * @return String serverNameAndVersion
     */
    public String getServerNameAndVersion() {
        return null;
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer null because this does not apply.
     *
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
        return null;
    }

    /**
     * INTERNAL: launchContainerThread(Thread thread): Do nothing because container Threads are not launchable
     * in this platform
     *
     * @param Thread thread : the instance of Thread
     * @return void
     */
    public void launchContainerThread(Thread thread) {
    }

    /**
     * INTERNAL: getServerLog(): Return the ServerLog for this platform
     *
     * Return the default ServerLog in the base
     *
     * @return oracle.toplink.logging.SessionLog
     */
    public oracle.toplink.logging.SessionLog getServerLog() {
        return new DefaultSessionLog();
    }    

    /**
     * INTERNAL:
     * When there is no server, the original connection will be returned
     */
    public java.sql.Connection unwrapConnection(java.sql.Connection connection){
        return connection;
    }
    
}