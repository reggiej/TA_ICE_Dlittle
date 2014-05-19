// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server.was;

import oracle.toplink.platform.server.ServerPlatformBase;
import oracle.toplink.sessions.DatabaseSession;

import java.sql.Connection;
import oracle.toplink.transaction.was.WebSphereTransactionController;

/**
 * PUBLIC:
 *
 * This is the concrete subclass responsible for representing WebSphere-specific server behaviour.
 *
 * This platform has:
 * - No JMX MBean runtime services
 * - transaction controller classes overridden in its subclasses
 */
public class WebSpherePlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public WebSpherePlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
    }
	
    public Connection unwrapConnection(Connection connection) { 
    	if (connection instanceof com.ibm.ws.rsadapter.jdbc.WSJdbcConnection){
            return (Connection)com.ibm.ws.rsadapter.jdbc.WSJdbcUtil.getNativeConnection((com.ibm.ws.rsadapter.jdbc.WSJdbcConnection) connection); 
        }
        return connection;
    }
    
    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of 
     * external transaction controller to use for WebSphere. This is 
     * read-only.
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
    		externalTransactionControllerClass = WebSphereTransactionController.class;
    	}
        return externalTransactionControllerClass;
    }
}
