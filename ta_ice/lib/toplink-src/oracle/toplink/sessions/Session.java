// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.*;
import java.io.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.expressions.*;
import oracle.toplink.platform.server.ServerPlatform;
import oracle.toplink.platform.database.DatabasePlatform;
import oracle.toplink.queryframework.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.logging.SessionLogEntry;
import oracle.toplink.remote.CacheSynchronizationManager;

/**
 * <p>
 * <b>Purpose</b>: Define the TopLink session public interface.
 * <p>
 * <b>Description</b>: This interface is meant to clarify the public protocol into TopLink.
 * It also allows for non-subclasses of Session to conform to the TopLink API.
 * It should be used as the applications main interface into the TopLink API to
 * ensure compatibility between all TopLink sessions.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the API for all reading, units of work.
 * </ul>
 * @see UnitOfWork
 * @see DatabaseSession
 * @see oracle.toplink.internal.sessions.AbstractSession
 * @see oracle.toplink.internal.sessions.DatabaseSessionImpl
 * @see oracle.toplink.threetier.ServerSession
 * @see oracle.toplink.threetier.ClientSession
 */
public interface Session {

    /**
     * ADVANCED:
     * Returns a light weight read-only session where all
     * objects are automatically read as of the specified past time.
     * <p>Use this Session to take advantage of Oracle 9 Release 2 Flashback or
     * TopLink general history support and still be able to cache query results.
     * <p>A special historical session is required as all objects read may
     * be of different versions than those stored in the global session cache.
     * Hence also known as IsolationSession, as all reads bypass the global
     * cache.
     * <p>An AsOfClause at the Session level will override any clauses set at the
     * query or expression levels.
     * <p>
     * Example: Using a historical session to read past versions of objects.
     * <p>
     * <pre><blockquote>
     *  AsOfClause pastTime = new AsOfClause(System.currentTimeMillis() - 24*60*60*1000);
     *     Session historicalSession = session.acquireSessionAsOf(pastTime);
     *      Employee pastEmployee = (Employee)historicalSession.readObject(Employee.class);
     *      Address pastAddress = pastEmployee.getAddress();
     *      Vector pastProjects = pastEmployee.getProjects();
     *  historicalSession.release();
     * </blockquote></pre>
     * <p>
     * Example: Using the above past employee to recover objects.
     * <p>
     * <pre><blockquote>
     *     UnitOfWork uow = baseSession.acquireUnitOfWork();
     *      Employee presentClone = (Employee)uow.readObject(pastEmployee);
     *      uow.deepMergeClone(pastEmployee);
     *  uow.commit();
     * <p>
     * By definition all data as of a past time is frozen.  So this session is
     * also ideal for read consistent queries and read only transactions, as all
     * queries will be against a consistent and immutable snap shot of the data.
     * @param pastTime Represents a valid snap shot time.
     * @throws ValidationException if <code>this</code>
     * not a ClientSession, plain Session, or SessionBroker.
     * @see oracle.toplink.history.AsOfClause
     * @see oracle.toplink.expressions.Expression#asOf(oracle.toplink.history.AsOfClause)
     * @see oracle.toplink.queryframework.ObjectLevelReadQuery#setAsOfClause(oracle.toplink.history.AsOfClause)
     * @see oracle.toplink.history.HistoryPolicy
     */
    public Session acquireHistoricalSession(oracle.toplink.history.AsOfClause pastTime);

    /**
     * PUBLIC:
     * Return a unit of work for this session.
     * The unit of work is an object level transaction that allows
     * a group of changes to be applied as a unit.
     * The return value should be used as the oracle.toplink.sessions.UnitOfWork interface
     * 
     * @see UnitOfWork
     */
    public UnitOfWork acquireUnitOfWork();

    /**
     * PUBLIC:
     * Add the query to the session queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public void addQuery(String name, DatabaseQuery query);
    
    /**
     * ADVANCED:
     * Add a pre-defined not yet parsed EJBQL String/query to the session to be parsed 
     * after descriptors are initialized.
     * @see #getAllQueries()
     */
    public void addEjbqlPlaceHolderQuery(DatabaseQuery query);

    /**
     * PUBLIC:
     * clear the integrityChecker, the integrityChecker holds all the ClassDescriptor Exceptions.
     */
    public void clearIntegrityChecker();

    /**
     * PUBLIC:
     * Clear the profiler, this will end the current profile opperation.
     */
    public void clearProfile();

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public boolean containsObjectInIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass);

    /**
     * PUBLIC:
     * Return true if the pre-defined query is defined on the session.
     */
    public boolean containsQuery(String queryName);

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object and all of its privately owned parts will be copied, the object's primary key will be reset to null.
     *
     * @see #copyObject(Object, ObjectCopyingPolicy)
     */
    public Object copyObject(Object original);

    /**
     * PUBLIC:
     * Return a complete copy of the object.
     * This can be used to obtain a scatch copy of an object,
     * or for templatizing an existing object into another new object.
     * The object copying policy allow for the depth, and reseting of the primary key to null, to be specified.
     */
    public Object copyObject(Object original, ObjectCopyingPolicy policy);

    /**
     * PUBLIC:
     * Return if the object exists on the database or not.
     * This always checks existence on the database.
     */
    public boolean doesObjectExist(Object object) throws DatabaseException;

    /**
     * PUBLIC:
     * Turn off logging
     */
    public void dontLogMessages();

    /**
     * PUBLIC:
     * Execute the call on the database.
     * The row count is returned.
     * The call can be a stored procedure call, SQL call or other type of call.
     * <p>Example:
     * <p>session.executeNonSelectingCall(new SQLCall("Delete from Employee");
     *
     * @see #executeSelectingCall(Call)
     */
    public int executeNonSelectingCall(Call call);

    /**
     * PUBLIC:
     * Execute the non-selecting (update/DML) SQL string.
     * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public void executeNonSelectingSQL(String sqlString);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Object arg1, Object arg2, Object arg3);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     * The class is the descriptor in which the query was pre-defined.
     *
     * @see oracle.toplink.descriptors.DescriptorQueryManager#addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Class domainClass, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Object arg1, Object arg2, Object arg3);

    /**
     * PUBLIC:
     * Execute the pre-defined query by name and return the result.
     * Queries can be pre-defined and named to allow for their reuse.
     *
     * @see #addQuery(String, DatabaseQuery)
     */
    public Object executeQuery(String queryName, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the database query.
     * A query is a database operation such as reading or writting.
     * The query allows for the operation to be customized for such things as,
     * performance, depth, caching, etc.
     *
     * @see DatabaseQuery
     */
    public Object executeQuery(DatabaseQuery query) throws TopLinkException;

    /**
     * PUBLIC:
     * Return the results from exeucting the database query.
     * the arguments are passed in as a vector
     */
    public Object executeQuery(DatabaseQuery query, Vector argumentValues);

    /**
     * PUBLIC:
     * Execute the call on the database and return the result.
     * The call must return a value, if no value is return executeNonSelectCall must be used.
     * The call can be a stored procedure call, SQL call or other type of call.
     * A vector of database rows is returned, database row implements Java 2 Map which should be used to access the data.
     * <p>Example:
     * <p>session.executeSelectingCall(new SQLCall("Select * from Employee");
     *
     * @see #executeNonSelectingCall(Call)
     */
    public Vector executeSelectingCall(Call call);

    /**
     * PUBLIC:
     * Execute the selecting SQL string.
     * A Vector of DatabaseRecords are returned.
	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
	 */
    public Vector executeSQL(String sqlString);

    /**
     * PUBLIC:
     * Return the active session for the current active external (JTS) transaction.
     * This should only be used with JTS and will return the session if no external transaction exists.
     */
    public Session getActiveSession();

    /**
     * PUBLIC:
     * Return the active unit of work for the current active external (JTS) transaction.
     * This should only be used with JTS and will return null if no external transaction exists.
     */
    public UnitOfWork getActiveUnitOfWork();

    /**
     * ADVANCED:
     * Return the descriptor specified for the class.
     * If the class does not have a descriptor but implements an interface that is also implemented
     * by one of the classes stored in the hashtable, that descriptor will be stored under the
     * new class.
     */
    public ClassDescriptor getClassDescriptor(Class theClass);

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     */
    public ClassDescriptor getClassDescriptor(Object domainObject);

    /**
     * PUBLIC:
     * Return the descriptor for the alias.
     */
    public ClassDescriptor getClassDescriptorForAlias(String alias);

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException;

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException;

    /**
     * ADVANCED:
     * Answers the past time this session is as of.  Indicates whether or not this
     * is a special historical session where all objects are read relative to a
     * particular point in time.
     * @return An immutable object representation of the past time.
     * <code>null</code> if no clause set, or this a regular session.
     * @see #acquireHistoricalSession(oracle.toplink.history.AsOfClause)
     */
    public oracle.toplink.history.AsOfClause getAsOfClause();

    /**
     * ADVANCED:
     * Returns the Synchronization Policy for this session.
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This method is replaced by
     *         {@link DatabaseSession.getCommandManager()}
     */
    public CacheSynchronizationManager getCacheSynchronizationManager();

    /**
     * ADVANCED:
     * Return the descriptor specified for the class.
     * If the class does not have a descriptor but implements an interface that is also implemented
     * by one of the classes stored in the hashtable, that descriptor will be stored under the
     * new class.
     */
    public ClassDescriptor getDescriptor(Class theClass);

    /**
     * ADVANCED:
     * Return the descriptor specified for the object's class.
     */
    public ClassDescriptor getDescriptor(Object domainObject);

    /**
     * PUBLIC:
     * Return the descriptor for  the alias.
     * UnitOfWork delegates this to the parent
     */
    public ClassDescriptor getDescriptorForAlias(String alias);

    /**
     * ADVANCED:
     * Return all registered descriptors.
     */
    public Map getDescriptors();

    /**
     * ADVANCED:
     * Return all pre-defined not yet parsed EJBQL queries.
     * @see #getAllQueries()
     */
    public List getEjbqlPlaceHolderQueries();
    
    /**
     * PUBLIC:
     * Return the event manager.
     * The event manager can be used to register for various session events.
     */
    public SessionEventManager getEventManager();

    /**
     * PUBLIC:
     * Return the ExceptionHandler.Exception handler can catch errors that occur on queries or during database access.
     */
    public ExceptionHandler getExceptionHandler();

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.transaction.JTATransactionController
     */
    public ExternalTransactionController getExternalTransactionController();

    /**
     * ADVANCED:
     * Return the object from the identity with primary and class of the given object.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass);

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     * @deprecated Since 3.6.3
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException;

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException;

    /**
     * PUBLIC:
     * The IdentityMapAccessor is the preferred way of accessing IdentityMap funcitons
     * This will return an object which implements an interface which exposes all public
     * IdentityMap functions.
     */
    public IdentityMapAccessor getIdentityMapAccessor();

    /**
     * PUBLIC:
     * Returns the integrityChecker,the integrityChecker holds all the ClassDescriptor Exceptions.
     */
    public IntegrityChecker getIntegrityChecker();

    /**
     * PUBLIC:
     * Return the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     * To enable logging logMessages must be turned on.
     *
     * @see #logMessages()
     */
    public Writer getLog();

    /**
     * PUBLIC:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     * NOTE: this must only be used for relational specific usage,
     * it will fail for non-relational datasources.
     */
    public DatabasePlatform getPlatform();

    /**
     * PUBLIC:
     * Return the database platform currently connected to.
     * The platform is used for database specific behavoir.
     */
    public Platform getDatasourcePlatform();
        
    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * NOTE: this must only be used for relational specific usage,
     * it will fail for non-relational datasources.
     */
    public DatabaseLogin getLogin();

    /**
     * PUBLIC:
     * Return the login, the login holds any database connection information given.
     * This return the Login interface and may need to be cast to the datasource specific implementation.
     */
    public Login getDatasourceLogin();

    /**
     * PUBLIC:
     * Return the name of the session.
     * This is used with the session broker, or to give the session a more meaningful name.
     */
    public String getName();

    /**
     * ADVANCED:
     * Return the sequnce number from the database
     */
    public Number getNextSequenceNumberValue(Class domainClass);

    /**
     * PUBLIC:
     * Return the profiler.
     * The profiler is a tool that can be used to determine performance bottlenecks.
     * The profiler can be queries to print summaries and configure for logging purposes.
     */
    public SessionProfiler getProfiler();

    /**
     * PUBLIC:
     * Return the project.
     * The project includes the login and descriptor and other configuration information.
     */
    public oracle.toplink.sessions.Project getProject();

    /**
     * ADVANCED:
     * Allow for user defined properties.
     */
    public Map getProperties();

    /**
     * ADVANCED:
     * Returns the user defined property.
     */
    public Object getProperty(String name);

    /**
     * ADVANCED:
     * Return all pre-defined queries.
     */
    public Map getQueries();

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name);

    /**
     * PUBLIC:
     * Return the query from the session pre-defined queries with the given name.
     * This allows for common queries to be pre-defined, reused and executed by name.
     */
    public DatabaseQuery getQuery(String name, Vector arguments);

    /**
     * PUBLIC:
     * Return the server platform currently used.
     * The server platform is used for application server specific behavior.
     */
    public ServerPlatform getServerPlatform();

    /**
     * PUBLIC:
     * Return the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     * To enable logging, logMessages must be turned on.
     *
     * @see #logMessages()
     */
    public SessionLog getSessionLog();

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getWriteLockValue(Object domainObject);

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass);

    /**
     * PUBLIC:
     * Allow any WARNING level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleException(RuntimeException exception) throws RuntimeException;

    /**
     * ADVANCED:
     * Return true if a synchronisation policy exists
     */
    public boolean hasCacheSynchronizationManager();

    /**
     * ADVANCED:
     * Return true if a descriptor exists for the given class.
     */
    public boolean hasDescriptor(Class theClass);

    /**
     * PUBLIC:
     * Return if an exception handler is present.
     */
    public boolean hasExceptionHandler();

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.transaction.JTATransactionController
     */
    public boolean hasExternalTransactionController();

    /**
     * PUBLIC:
     * Reset the entire object cache.
     * <p> NOTE: be careful using this method. This method blows away both this session's and its parents caches,
     * this includes the server cache or any other cache. This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void initializeAllIdentityMaps();

    /**
     * PUBLIC:
     * Reset the identity map for only the instances of the class.
     * For inheritence the user must make sure that they only use the root class.
     * Caution must be used in doing this to ensure that the objects within the identity map
     * are not referenced from other objects of other classes or from the application.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void initializeIdentityMap(Class theClass);

    /**
     * PUBLIC:
     * Reset the entire object cache.
     * This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void initializeIdentityMaps();

    /**
     * PUBLIC:
     * Return if this session is a client session.
     */
    public boolean isClientSession();

    /**
     * PUBLIC:
     * Return if this session is connected to the database.
     */
    public boolean isConnected();

    /**
     * PUBLIC:
     * Return if this session is a database session.
     */
    public boolean isDatabaseSession();

    /**
     * PUBLIC:
     * Return if this session is a distributed session.
     */
    public boolean isDistributedSession();

    /**
     * PUBLIC:
     * Return if a profiler is being used.
     */
    public boolean isInProfile();

    /**
     * PUBLIC:
     * Return if this session is a remote session.
     */
    public boolean isRemoteSession();

    /**
     * PUBLIC:
     * Return if this session is a server session.
     */
    public boolean isServerSession();

    /**
     * PUBLIC:
     * Return if this session is a session broker.
     */
    public boolean isSessionBroker();

    /**
     * PUBLIC:
     * Return if this session is a unit of work.
     */
    public boolean isUnitOfWork();

    /**
     * PUBLIC:
     * Return if this session is a remote unit of work.
     */
    public boolean isRemoteUnitOfWork();
    
    /**
     * ADVANCED:
     * Extract and return the primary key from the object.
     */
    public Vector keyFromObject(Object domainObject) throws ValidationException;

    /**
     * PUBLIC:
     * Log the log entry.
     */
    public void log(SessionLogEntry entry);

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(oracle.toplink.logging.SessionLogEntry)
     * @see #log(oracle.toplink.logging.SessionLogEntry)
     */
    public void log(oracle.toplink.sessions.SessionLogEntry entry);

    /**
     * OBSOLETE:
     * @deprecated    No direct replacement, moved to internal AbstractSession.
     * @see #logMessage(String)
     */
    public void logDebug(String message, Object[] arguments);

    /**
     * OBSOLETE:
     * @deprecated    No direct replacement, moved to internal AbstractSession.
     * @see #logMessage(String)
     */
    public void logDebug(String message);

    /**
     * OBSOLETE:
     * @deprecated    No direct replacement, moved to internal AbstractSession.
     */
    public void logException(Exception exception);

    /**
     * OBSOLETE:
     * @deprecated    Replaced by log(int level, String category, String message, Object[] params)
     * @see #logMessage(String)
     */
    public void logMessage(String message, Object[] arguments);

    /**
     * Log a untranslated message to the TopLink log at FINER level.
     */
    public void logMessage(String message);

    /**
     * OBSOLETE:
     * @deprecated    Replaced by setLogLevel(int level, String category);
     * @see #setLogLevel(int level)
     */
    public void logMessages();

    /**
     * PUBLIC:
     * Used to print all the objects in the identity map of the passed in class.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void printIdentityMap(Class businessClass);

    /**
     * PUBLIC:
     * Used to print all the objects in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void printIdentityMaps();

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject, Vector key);

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public Object putInIdentityMap(Object domainObject, Vector key, Object writeLockValue);

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database.
     * This operation can be customized through using a ReadAllQuery,
     * or through also passing in a selection criteria.
     *
     * @see ReadAllQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Vector readAllObjects(Class domainClass) throws DatabaseException;

    /**
     * OBSOLETE:
     * Read all of the instances of the class from the database returned through execution the SQL string.
     * The SQL string must be a valid SQL select statement or selecting stored procedure call.
     * This operation can be customized through using a ReadAllQuery.
     * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     * @deprecated  Replaced by
     *         {@link #readAllObjects(Class, Call)}
     */
    public Vector readAllObjects(Class domainClass, String sqlString) throws DatabaseException;

    /**
     * PUBLIC:
     * Read all the instances of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or EJBQLCall.
     *
     * example: session.readAllObjects(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see SQLCall
     * @see EJBQLCall
     */
    public Vector readAllObjects(Class domainClass, Call aCall) throws DatabaseException;

    /**
     * PUBLIC:
     * Read all of the instances of the class from the database matching the given expression.
     * This operation can be customized through using a ReadAllQuery.
     *
     * @see ReadAllQuery
     */
    public Vector readAllObjects(Class domainClass, Expression selectionCriteria) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database.
     * This operation can be customized through using a ReadObjectQuery,
     * or through also passing in a selection criteria.
     * By default, this method executes a query without selection criteria and
     * consequently it will always result in a database access even if an instance
     * of the specified Class exists in the cache. Executing a query with
     * selection criteria allows you to avoid a database access if the selected
     * instance is in the cache.
     * Because of this, you may whish to consider a readObject method that takes selection criteria, such as: {@link #readObject(Class, Call)}, {@link #readObject(Class, Expression)}, or {@link #readObject(Object)}.
     * @see ReadObjectQuery
     * @see #readAllObjects(Class, Expression)
     */
    public Object readObject(Class domainClass) throws DatabaseException;

    /**
     * OBSOLETE:
     * Read the first instance of the class from the database returned through execution the SQL string.
     * The SQL string must be a valid SQL select statement or selecting stored procedure call.
     * This operation can be customized through using a ReadObjectQuery.
     * Warning: Allowing an unverified SQL string to be passed into this 
     * method makes your application vulnerable to SQL injection attacks. 
     * @deprecated use readObject(Class domainClass, Call aCall)
     * @see ReadObjectQuery
     */
    public Object readObject(Class domainClass, String sqlString) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database returned through execution the Call string.
     * The Call can be an SQLCall or EJBQLCall.
     *
     * example: session.readObject(Employee.class, new SQLCall("SELECT * FROM EMPLOYEE"));
     * @see SQLCall
     * @see EJBQLCall
     */
    public Object readObject(Class domainClass, Call aCall) throws DatabaseException;

    /**
     * PUBLIC:
     * Read the first instance of the class from the database matching the given expression.
     * This operation can be customized through using a ReadObjectQuery.
     *
     * @see ReadObjectQuery
     */
    public Object readObject(Class domainClass, Expression selectionCriteria) throws DatabaseException;

    /**
     * PUBLIC:
     * Use the example object to consruct a read object query by the objects primary key.
     * This will read the object from the database with the same primary key as the object
     * or null if no object is found.
     */
    public Object readObject(Object object) throws DatabaseException;

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * This can be used to ensure the object is up to date with the database.
     * Caution should be used when using this to make sure the application has no un commited
     * changes to the object.
     */
    public Object refreshObject(Object object);

    /**
     * PUBLIC:
     * Release the session.
     * This does nothing by default, but allows for other sessions such as the ClientSession to do something.
     */
    public void release();

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     * Caution should be used when calling to avoid violating object identity.
     * The application should only call this is it knows that no references to the object exist.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void removeFromIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void removeFromIdentityMap(Vector key, Class theClass);

    /**
     * PUBLIC:
     * Remove the user defined property.
     */
    public void removeProperty(String property);

    /**
     * PUBLIC:
     * Remove the query name from the set of pre-defined queries
     */
    public void removeQuery(String queryName);

    /**
     * ADVANCED:
     * Sets synchronization policy for this session.
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This method is replaced by
     *         {@link DatabaseSession.setCommandManager(CommandManager)}
     */
    public void setCacheSynchronizationManager(CacheSynchronizationManager synchronizationManager);

    /**
     * PUBLIC:
     * Set the exceptionHandler.
     * Exception handler can catch errors that occur on queries or during database access.
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler);

    /**
     * OBSOLETE:
     * Previously used for JTS integration.
     *
     * If your application requires to have JTS control transactions a
     * ServerPlatform must be specified before login, either via your sessions.xml or in code.
     *
     * A subclass of ServerPlatformBase should handle your requirements.
     *
     * If not, we suggest creating your own subclass of ServerPlatformBase to specify the
     * external transaction controller class.
     *
     * @see oracle.toplink.platform.server.CustomServerPlatform
     */
    //@deprecated was removed from this method as there is no viable alternative bug 5637867 was filed to
    // have this resolved.
    public void setExternalTransactionController(ExternalTransactionController externalTransactionController);

    /**
     * PUBLIC:
     * Set the integrityChecker, the integrityChecker holds all the ClassDescriptor Exceptions.
     */
    public void setIntegrityChecker(IntegrityChecker integrityChecker);

    /**
     * PUBLIC:
     * Set the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference defaults to a writer on System.out.
     * To enable logging logMessages() is used.
     *
     * @see #logMessages()
     */
    public void setLog(Writer log);

    /**
     * PUBLIC:
     * Set the name of the session.
     * This is used with the session broker, or to give the session a more meaningful name.
     */
    public void setName(String name);

    /**
     * PUBLIC:
     * Set the profiler for the session.
     * This allows for performance operations to be profiled.
     */
    public void setProfiler(SessionProfiler profiler);

    /**
     * PUBLIC:
     * Allow for user defined properties.
     */
    public void setProperty(String propertyName, Object propertyValue);

    /**
     * PUBLIC:
     * Set the session log to which an accessor logs messages and SQL.
     * If not set, this will default to a session log on a writer on System.out.
     * To enable logging, logMessages must be turned on.
     *
     * @see #logMessages()
     */
    public void setSessionLog(SessionLog sessionLog);

    /**
     * OBSOLETE:
     * Replaced by setSessionLog(oracle.toplink.logging.SessionLog);
     * @deprecated
     * @see #setSessionLog(oracle.toplink.logging.SessionLog)
     */
    public void setSessionLog(oracle.toplink.sessions.SessionLog sessionLog);

    /**
     * OBSOLETE:
     * Replaced by setLogLevel(int level, String category);
     * @deprecated
     * @see #setLogLevel(int)
     */
    public void setShouldLogMessages(boolean shouldLogMessages);

    /**
     * PUBLIC:
     * Return if logging is enabled (false if log level is OFF)
     */
    public boolean shouldLogMessages();

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void updateWriteLockValue(Object domainObject, Object writeLockValue);

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     * @deprecated
     * @see #getIdentityMapAccessor()
     * @see oracle.toplink.sessions.IdentityMapAccessor
     */
    public void updateWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue);

    /**
     * OBSOLETE:
     * This should no longer be used.
     * @deprecated replaced by #hasExternalTransactionController()
     */
    public boolean usesExternalTransactionController();

    /**
     * ADVANCED:
     * This can be used to help debugging an object identity problem.
     * An object identity problem is when an object in the cache references an object not in the cache.
     * This method will validate that all cached objects are in a correct state.
     */
    public void validateCache();

    /**
     * PUBLIC:
     * Return the log level.
     * <br>Possible values for log level and category are listed in SessionLog.
     * @see oracle.toplink.sessions.SessionLog
     */
    public int getLogLevel(String category);

    /**
     * PUBLIC:
     * Return the log level.
     * <br>Possible values for log level are listed in SessionLog.
     * @see oracle.toplink.sessions.SessionLog
     */
    public int getLogLevel();

    /**
     * PUBLIC:
     * Set the log level. 
	 * <br>Possible values for log level are listed in SessionLog.
     * @see oracle.toplink.sessions.SessionLog
     */
    public void setLogLevel(int level);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     * <br>Possible values for log level and category are listed in SessionLog.
     * @see oracle.toplink.sessions.SessionLog
     */
    public boolean shouldLog(int Level, String category);

    /**
     * PUBLIC:
     * Allow any SEVERE level exceptions that occur within TopLink to be logged and handled by the exception handler.
     */
    public Object handleSevere(RuntimeException exception) throws RuntimeException;
    
    /**
     * PUBLIC:
     * Return if this session's decendants should use finalizers.
     * The allows certain finalizers such as in ClientSesion to be enabled.
     * These are disable by default for performance reasons.
     */
    public boolean isFinalizersEnabled();
    
    /**
     * PUBLIC:
     * Set if this session's decendants should use finalizers.
     * The allows certain finalizers such as in ClientSesion to be enabled.
     * These are disable by default for performance reasons.
     */
    public void setIsFinalizersEnabled(boolean isFinalizersEnabled);
}
