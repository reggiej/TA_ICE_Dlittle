// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.Properties;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.platform.database.DatabasePlatform;

/**
 * <p>
 * <b>Purpose</b>: Define the information required to connect to a TopLink session.
 * <p>
 * <b>Description</b>: This interface represents a generic concept of a login to be used
 * when connecting to a data-store.  It is independant of JDBC so that the TopLink
 * session interface can be used for JCA, XML, non-relational or three-tiered frameworks.
 * <p>
 * @see DatabaseLogin
 */
public interface Login {

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    String getPassword();

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    String getUserName();

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    void setPassword(String password);

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    void setUserName(String userName);

    /**
     * PUBLIC:
     * Return whether TopLink uses some externally managed connection pooling.
     */
    boolean shouldUseExternalConnectionPooling();

    /**
     * PUBLIC:
     * Return whether TopLink uses some externally managed transaction service such as JTS.
     */
    boolean shouldUseExternalTransactionController();

    /**
     * INTERNAL:
     * Return the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     * NOTE: this must only be used for relational specific usage and will not work for
     * non-relational datasources.
     */
    DatabasePlatform getPlatform();

    /**
     * PUBLIC:
     * Return the datasource platform specific information.
     * This allows TopLink to configure certain advanced features for the datasource desired.
     * The platform also allows configuration of sequence information.
     */
    Platform getDatasourcePlatform();

    /**
     * INTERNAL:
     * Set the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     */
    void setPlatform(Platform platform);

    /**
     * PUBLIC:
     * Set the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     */
    void setDatasourcePlatform(Platform platform);

    /**
     * INTERNAL:
     * Connect to the datasource, and return the driver level connection object.
     */
    Object connectToDatasource(Accessor accessor, Session session) throws DatabaseException;

    /**
     * INTERNAL:
     * Build the correct datasource Accessor for this login instance.
     */
    Accessor buildAccessor();

    /**
     * INTERNAL:
     * Clone the login.
     */
    Object clone();

    /**
     * PUBLIC:
     * Return the qualifier for the all of the tables.
     */
    public String getTableQualifier();

    /**
     * INTERNAL:
     * Used for cache isolation.
     */
    public boolean shouldAllowConcurrentReadWrite();

    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the class level on
     * cache updates.
     */
    public boolean shouldSynchronizeWrites();
    
    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the object level on
     * cache updates and cache access.
     */
    public boolean shouldSynchronizeObjectLevelReadWrite();
    
    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the object level on
     * cache updates and cache access, based on database transaction.
     */
    public boolean shouldSynchronizeObjectLevelReadWriteDatabase();
    
    /**
     * INTERNAL:
     * Used for cache isolation.
     */
    public boolean shouldSynchronizedReadOnWrite();
    
    /**
     * PUBLIC:
     * The properties are additional, driver-specific, connection information
     * to be passed to the driver.<p>
     * NOTE: Do not set the password directly by getting the properties and
     * setting the "password" property directly. Use the method DatabaseLogin.setPassword(String).
     */
    public Object getProperty(String name);
    
    /**
     * PUBLIC:
     * The properties are additional, driver-specific, connection information
     * to be passed to the JDBC driver.
     */
    public void setProperties(Properties properties);

    /**
     * PUBLIC:
     * Some JDBC drivers require additional, driver-specific, properties.
     * Add the specified property to those to be passed to the JDBC driver.
     */
    public void setProperty(String propertyName, Object propertyValue);

    /**
     * PUBLIC:
     * This value defaults to false when not on a DatabaseLogin as the functionality has not been implemented
     * for other datasource type.  On an SQL Exception TopLink will ping the database to determine
     * if the connection used can continue to be used for queries.  This should have no impact on applications
     * unless the user is using pessimistic locking queries with 'no wait' or are using a query timeout feature.
     * If that is the case and the application is experiencing a performance impact from the health check then
     * this feature can be turned off. Turning this feature off will prevent TopLink from being able to
     * retry queries in the case of database failure. 
     */
    public boolean isConnectionHealthValidatedOnError();
}
