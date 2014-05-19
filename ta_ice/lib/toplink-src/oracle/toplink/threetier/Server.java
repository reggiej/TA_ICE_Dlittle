// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.threetier;

import oracle.toplink.sessions.*;
import oracle.toplink.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: A single session that supports multiple user/clients connection at the same time.
 * <p>
 * <b>Description</b>: This session supports a shared session that can be used by multiple users
 * or clients in a three-tiered application.  It brokers client sessions to allow read and write access
 * through a unified object cache.  The server session provides a shared read only database connection that
 * is used by all of its client for reads.  All changes to objects and the database must be done through
 * a unit of work acquired from the client session, this allows the changes to occur in a transactional object
 * space and under a exclusive database connection.
 * <p>
 * <b>Responsibilities</b>:
 *    <ul>
 *    <li> Connecting/disconnecting the default reading login.
 *    <li> Reading objects and maintaining the object cache.
 *    <li> Brokering client sessions.
 *    <li> Disabling database modification through the shared connection.
 *    </ul>
 * @see ClientSession
 * @see oracle.toplink.sessions.UnitOfWork UnitOfWork
 */
public interface Server extends oracle.toplink.sessions.DatabaseSession {

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing the same login as the server session.
     */
    public ClientSession acquireClientSession() throws DatabaseException;

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing its connection from a pool
     * of connection allocated on the server session.
     * By default this uses a lazy connection policy.
     */
    public ClientSession acquireClientSession(String poolName);

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * The client must provide its own login to use, and the client session returned
     * will have its own exclusive database connection.  This connection will be used to perform
     * all database modification for all units of work acquired from the client session.
     * By default this does not use a lazy connection policy.
     */
    public ClientSession acquireClientSession(Login login);

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * The connection policy specifies how the client session's connection will be acquired.
     */
    public ClientSession acquireClientSession(ConnectionPolicy connectionPolicy);

    /**
     * PUBLIC:
     * Add the connection pool.
     * Connections are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(String poolName, Login login, int minNumberOfConnections, int maxNumberOfConnections);

    /**
     * PUBLIC:
     * Connection are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(ConnectionPool pool);

    /**
     * PUBLIC:
     * Return the pool by name.
     */
    public ConnectionPool getConnectionPool(String poolName);

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public ConnectionPolicy getDefaultConnectionPolicy();

    /**
     * PUBLIC:
     * Return the default connection pool.
     */
    public ConnectionPool getDefaultConnectionPool();

    /**
     * PUBLIC:
     * Return the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public int getMaxNumberOfNonPooledConnections();

    /**
     * OBSOLETE:
     * A read connection pool has be added to ecapsulate read connection pooling.
     * @deprecated  Replaced by getReadConnectionPool().getMaxNumberOfConnections()
     *         {@link ConnectionPool#getMaxNumberOfConnections()}
     */
    public int getNumberOfReadConnections();

    /**
     * PUBLIC:
     * Handles allocating connections for read queries.
     * <p>
     * By default a read connection pool is created and configured automatically in the
     * constructor.  A default read connection pool is one with two connections, and
     * does not support concurrent reads.
     * <p> The read connection pool is not used while in transaction.
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useExclusiveReadConnectionPool
     * @see #useExternalReadConnectionPool
     * @see #useReadConnectionPool
     */
    public ConnectionPool getReadConnectionPool();

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setDatasourceLogin(Login login);

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public void setDefaultConnectionPolicy(ConnectionPolicy defaultConnectionPolicy);

    /**
     * PUBLIC:
     * Set the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public void setMaxNumberOfNonPooledConnections(int maxNumberOfNonPooledConnections);

    /**
     * OBSOLETE:
     * A read connection pool has be added to ecapsulate read connection pooling.
     * @deprecated  Replaced by getReadConnectionPool().setMaxNumberOfConnections(int)
     *         {@link ConnectionPool#setMaxNumberOfConnections(int)}
     */
    public void setNumberOfReadConnections(int numberOfReadConnections);

    /**
     * PUBLIC:
     * Sets the read connection pool directly.
     * <p>
     * Either {@link #useExclusiveReadConnectionPool} or {@link #useExternalReadConnectionPool} is
     * called in the constructor.  For a connection pool using concurrent reading
     * {@link #useReadConnectionPool} should be called on a new instance of <code>this</code>.
     *
     * @throws ValidationException if already connected
     */
    public void setReadConnectionPool(ConnectionPool readConnectionPool);

    /**
     * PUBLIC:
     * Sets the read connection pool to be a standard <code>ConnectionPool</code>.
     * <p>
     * Minimum and maximum number of connections is determined from the ConnectionPolicy.  The defaults are 2 for both.
     * <p>
     * Since the same type of connection pool is used as for writing, no
     * two users will use the same connection for reading at the same time.
     * <p>
     * This read connection pool is the default as some JDBC drivers do not support
     * concurrent reading.
     * <p>
     * Unless <code>this</code> {@link oracle.toplink.sessions.Session#hasExternalTransactionController hasExternalTransactionController()}
     * a read connection pool of this type will be setup in the constructor.
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useReadConnectionPool
     * @see #useExternalReadConnectionPool
     */
    public void useExclusiveReadConnectionPool(int minNumberOfConnections, int maxNumberOfConnections);

    /**
     * PUBLIC:
     * Sets the read connection pool to be an <code>ExternalConnectionPool</code>.
     * <p>
     * This type of connection pool will be created and configured automatically if
     * an external transaction controller is used.
     * @see oracle.toplink.sessions.Session#hasExternalTransactionController
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useReadConnectionPool
     * @see #useExclusiveReadConnectionPool
     */
    public void useExternalReadConnectionPool();

    /**
     * PUBLIC:
     * Sets the read connection pool to be a <code>ReadConnectionPool</code>.
     * <p>
     * Since read connections are not used for writing, multiple users can
     * theoretically use the same connection at the same time.  Most JDBC drivers
     * have concurrent reading which supports this.
     * <p>
     * Use this read connection pool to take advantage of concurrent reading.
     * <p>
     * @param minNumberOfConnections
     * @param maxNumberOfConnections As multiple readers can use the same connection
     * concurrently fewer connections are needed.
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useExternalReadConnectionPool
     * @see #useExclusiveReadConnectionPool
     */
    public void useReadConnectionPool(int minNumberOfConnections, int maxNumberOfConnections);

}