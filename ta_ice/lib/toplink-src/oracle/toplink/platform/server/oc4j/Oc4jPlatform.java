// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.server.oc4j;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.transaction.oc4j.Oc4jTransactionController;
import oracle.toplink.platform.database.oracle.OraclePlatform;
import oracle.toplink.platform.server.ServerPlatformBase;

import oracle.toplink.internal.databaseaccess.Platform;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing Oc4j-specific server behaviour.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use an Oc4j-specific controller class
 * initializeServerNameAndVersion(): to call an Oc4j library for this information
 *
 */
public class Oc4jPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public Oc4jPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * for Oc4j. This is read-only.
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
    		externalTransactionControllerClass = Oc4jTransactionController.class;
    	}
        return externalTransactionControllerClass;
    }

    /**
     * INTERNAL:  This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * Oracle Specific support. (ie TIMESTAMPTZ)
     */
    public java.sql.Connection unwrapConnection(java.sql.Connection connection){
        Platform platform = getDatabaseSession().getDatasourceLogin().getDatasourcePlatform();
        if(platform.isOracle() && ((OraclePlatform)platform).canUnwrapOracleConnection()) {
            return ((OraclePlatform)platform).unwrapOracleConnection(connection);
        } else {
            return super.unwrapConnection(connection);
        }
    }
}