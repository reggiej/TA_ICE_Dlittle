// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.threetier;

import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.sessions.Login;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: This subclass is intended to be used with external connection pools.
 * For these pools, TopLink does not control the pooling behaviour.
 * The login should have the usesExternalConnectionPooling set to "true".
 */
public class ExternalConnectionPool extends ConnectionPool {
    protected Accessor cachedConnection;

    /**
     * PUBLIC:
     * Build a new external connection pool.  The JDBC driver is responsible for pooling the connections.
     */
    public ExternalConnectionPool() {
        super();
    }

    /**
     * PUBLIC:
     * Build a new external connection pool.  The JDBC driver is responsible for pooling the connections.
     */
    public ExternalConnectionPool(String name, Login login, ServerSession owner) {
        super(name, login, 0, 0, owner);
    }

    /**
     * INTERNAL:
     * When we acquire a connection from an ExternalConnectionPool we build
     * a new connection (retrieve it from the external pool).
     */
    public Accessor acquireConnection() throws ConcurrencyException {
        return (Accessor)getCachedConnection().clone();
    }

    /**
     *  INTERNAL:
     *  Return the currently cached connection to the external connection pool
     *  @return oracle.toplink.internal.databaseaccess.Accessor
     */
    protected Accessor getCachedConnection() {
        return cachedConnection;
    }

    /**
     * INTERNAL:
     * Assume true as the driver is responsible for blocking.
     */
    public boolean hasConnectionAvailable() {
        return true;
    }

    /**
     * INTERNAL:
     * Checks for a conflict between pool's type and pool's login
     */
    public boolean isThereConflictBetweenLoginAndType() {
        return !getLogin().shouldUseExternalConnectionPooling();
    }

    /**
     * INTERNAL:
     * When you release an external connection, you simply let it go.
     */
    public void releaseConnection(Accessor connection) throws DatabaseException {
        connection.closeConnection();
    }

    /**
     *  Set the currently cached connection to the external connection pool.
     *  @param oracle.toplink.internal.databaseaccess.Accessor
     */
    protected void setCachedConnection(Accessor cachedConnection) {
        this.cachedConnection = cachedConnection;
    }

    /**
     * INTERNAL:
     * This mehtod is a no-op for external pools.
     */
    public synchronized void shutDown() {
        //do nothing
        setIsConnected(false);
    }

    /**
     * INTERNAL:
     * Build the default connection.
     * This validates that connect will work and sets up the parent accessor to clone.
     */
    public synchronized void startUp() {
        setCachedConnection(buildConnection());
        setIsConnected(true);
    }
}