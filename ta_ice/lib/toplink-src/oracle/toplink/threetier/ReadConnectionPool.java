// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.threetier;

import java.util.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.sessions.Login;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>:The read connection pool is used for read access through the server session.
 * Any of the connection pools can be used for the read pool however this is the default.
 * This pool allows for concurrent reads against the same JDBC connection and requires that
 * the JDBC connection support concurrent read access.
 */
public class ReadConnectionPool extends ConnectionPool {

    /**
     * PUBLIC:
     * Build a new read connection pool.
     */
    public ReadConnectionPool() {
        super();
    }

    /**
     * PUBLIC:
     * Build a new read connection pool.
     */
    public ReadConnectionPool(String name, Login login, int minNumberOfConnections, int maxNumberOfConnections, ServerSession owner) {
        super(name, login, minNumberOfConnections, maxNumberOfConnections, owner);
    }

    /**
     * INTERNAL:
     * Wait until a connection is avaiable and allocate the connection for the client.
     */
    public synchronized Accessor acquireConnection() throws ConcurrencyException {
        Accessor leastBusyConnection = null;

        // Search for an unused connection, also find the least busy incase all are used.
        int size = this.connectionsAvailable.size();
        for (int index = 0; index < size; index++) {
            Accessor connection = (Accessor)connectionsAvailable.get(index);
            //if the pool has encountered a connection failure on one of the accessors lets test the others.
            if (this.checkConnections){
                if (this.getOwner().getLogin().isConnectionHealthValidatedOnError() && this.getOwner().getServerPlatform().wasFailureCommunicationBased(null, connection, this.getOwner())){
                    connectionsAvailable.remove(index);
                    //reset index as we just removed a connection and should check at the same index again
                    --index;
                    //reset size as there are one less connection in the pool now.
                    --size;
                    continue; //skip back to biginning of loop
                }else{
                    this.checkConnections = false;
                }
            }
            if (connection.getCallCount() == 0) {
                connection.incrementCallCount(getOwner());
                return connection;
            }
            if ((leastBusyConnection == null) || (leastBusyConnection.getCallCount() > connection.getCallCount())) {
                leastBusyConnection = connection;
            }
        }

        // If still not at max, add a new connection.
        if ((this.connectionsAvailable.size() + this.connectionsUsed.size()) < this.maxNumberOfConnections) {
            Accessor connection = buildConnection();
            this.connectionsAvailable.add(connection);
            connection.incrementCallCount(getOwner());
            return connection;
        }

        // Use the least busy connection.
        leastBusyConnection.incrementCallCount(getOwner());
        return leastBusyConnection;
    }

    /**
     * INTERNAL:
     * Concurrent reads are supported.
     */
    public boolean hasConnectionAvailable() {
        return true;
    }

    /**
     * INTERNAL:
     * Because connections are not exclusive nothing is required.
     */
    public synchronized void releaseConnection(Accessor connection) throws DatabaseException {
        connection.decrementCallCount();
        if (!connection.isValid()){
            this.connectionsAvailable.remove(connection);
            try{
                connection.disconnect(getOwner());
            }catch (Exception ex){
                //ignore
            }
        }
    }

}