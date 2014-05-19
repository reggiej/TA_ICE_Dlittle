// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.platform.server.ucp;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.platform.server.NoServerPlatform;
import oracle.toplink.sessions.DatabaseSession;
import oracle.ucp.UniversalPooledConnection;

/**
 * PUBLIC: This is the concrete subclass responsible for representing an application that
 * uses a Universal Connection Pool outside of Oc4j 11.1.1.x.  Within Oc4j 11.1.1 the 
 * correct Oc4j platform should be used.
*     20/08/2008- 11.1.1   Michael OBrien 
*       7278787 : Remove ucp(UniversalPooledConnection) functionality - this class is excluded from compilation
*/
public class UCPPlatform extends NoServerPlatform {
    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public UCPPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL:
     * A call to this method will perform a platform based check on the connection and exception
     * error code to dermine if the connection is still valid or if a communication error has occurred.
     * If a communication error has occurred then the query may be retried.
     * If this platform is unable to determine if the error was communication based it will return
     * false forcing the error to be thrown to the user.
     */
    
    public boolean wasFailureCommunicationBased(SQLException exception, Connection connection, AbstractSession sessionForProfile){
    	if (connection != null && connection instanceof UniversalPooledConnection){
    		if (((UniversalPooledConnection)connection).isValid()){
    			return false;
    		}else{
    			return true;
    		}
    	} else{
    		return getDatabaseSession().getPlatform().wasFailureCommunicationBased(exception, connection, sessionForProfile);
    	}
    }
}
