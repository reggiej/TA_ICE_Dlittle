// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.server.wls;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.platform.server.ServerPlatformBase;
import oracle.toplink.transaction.wls.WebLogicTransactionController;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing WebLogic-specific server behaviour.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use the WebLogic-specific controller class
 * initializeServerNameAndVersion(): to call the WebLogic library for this information
 */
public class WebLogicPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public WebLogicPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
    }

    /**
     * INTERNAL: initializeServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     */
    protected void initializeServerNameAndVersion() {
        this.serverNameAndVersion = weblogic.version.getBuildVersion();
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * for WebLogic. This is read-only.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.transaction.JTATransactionController
     * @see ServerPlatformBase.isJTAEnabled()
     * @see ServerPlatformBase.disableJTA()
     * @see ServerPlatformBase.initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
    	if (externalTransactionControllerClass == null){
    		externalTransactionControllerClass = WebLogicTransactionController.class;
    	}
        return externalTransactionControllerClass;
    }

    /**
     * INTERNAL: getServerLog(): Return the correct ServerLog for this platform
     *
     * Return a WlsOutputLog
     *
     * @return oracle.toplink.logging.SessionLog
     */
    public oracle.toplink.logging.SessionLog getServerLog() {
        return new WlsLog();
    }

    /**
     * INTERNAL:  This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * Oracle Specific support. (ie TIMESTAMPTZ)
     */
    public java.sql.Connection unwrapConnection(java.sql.Connection connection){
        try {
            if (connection instanceof weblogic.jdbc.wrapper.Connection){
                return ((weblogic.jdbc.wrapper.Connection)connection).getVendorConnection();
            }
            return connection;
        } catch (java.sql.SQLException e) {
            ((DatabaseSessionImpl)getDatabaseSession()).log(SessionLog.WARNING, SessionLog.CONNECTION, "cannot_unwrap_connection", e);
            return connection;            
        }
    }
}