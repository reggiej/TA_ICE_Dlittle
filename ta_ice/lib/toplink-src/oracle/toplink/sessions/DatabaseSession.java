// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.platform.server.ServerPlatform;
import oracle.toplink.sequencing.SequencingControl;
import oracle.toplink.remotecommand.CommandManager;
import oracle.toplink.sequencing.SequencingControl;

/**
 * <p>
 * <b>Purpose</b>: Add login and configuration API to that of Session.
 * This interface is to be used during the creation and login of the session only.
 * The Session interface should be used after login for normal reading/writing.
 */
public interface DatabaseSession extends Session {

    /**
     * PUBLIC:
     * Add the descriptor to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * It is best to add the descriptors before login, if added after login the order in which
     * descriptors are added is dependant on inheritice and references unless the addDescriptors
     * method is used.
     *
     * @see #addDescriptors(Vector)
     * @see #addDescriptors(Project)
     */
    public void addDescriptor(ClassDescriptor descriptor);

    /**
     * PUBLIC:
     * Add the descriptors to the session.
     * All persistent classes must have a descriptor registered for them with the session.
     * This method allows for a batch of descriptors to be added at once so that TopLink
     * can resolve the dependancies between the descriptors and perform initialization optimially.
     */
    public void addDescriptors(Vector descriptors);

    /**
     * PUBLIC:
     * Add the descriptors to the session from the Project.
     * This can be used to combine the descriptors from multiple projects into a single session.
     * This can be called after the session has been connected as long as there are no external dependencies.
     */
    public void addDescriptors(oracle.toplink.sessions.Project project);

    /**
     * PUBLIC:
     * Begin a transaction on the database.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the begin is rejected.
     *
     * @see #isInTransaction()
     */
    public void beginTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * Commit the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException most databases validate changes as they are done,
     * normally errors do not occur on commit unless the disk fails or the connection is lost.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void commitTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Collection domainObjects);

    /**
     * PUBLIC:
     * delete all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be deleted as a unit.
     * The objects will be deleted through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void deleteAllObjects(Vector domainObjects);

    /**
     * PUBLIC:
     * Delete the object and all of its privately owned parts from the database.
     * The delete operation can be customized through using a delete query.
     *
     * @see oracle.toplink.queryframework.DeleteObjectQuery
     */
    public Object deleteObject(Object domainObject) throws DatabaseException, OptimisticLockException;

    /**
     * PUBLIC:
     * Insert the object and all of its privately owned parts into the database.
     * Insert should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The insert operation can be customized through using an insert query.
     *
     * @see oracle.toplink.queryframework.InsertObjectQuery
     * @see #writeObject(Object)
     */
    public Object insertObject(Object domainObject) throws DatabaseException;

    /**
     * PUBLIC:
     * Return if the session is currently in the progress of a database transaction.
     * Because nested transactions are allowed check if the transaction mutex has been aquired.
     */
    public boolean isInTransaction();

    /**
     * PUBLIC:
     * Set the server platform defining server-specific behaviour for the receiver (Oc4j, WLS, ... ).
     *
     * This is not permitted after the session is logged in.
     *
     * If the user wants a different external transaction controller class or
     * to provide some different behaviour than the provided ServerPlatform(s), we recommend
     * subclassing oracle.toplink.platform.server.ServerPlatformBase (or a subclass),
     * and overriding:
     *
     * ServerPlatformBase.getExternalTransactionControllerClass()
     * ServerPlatformBase.registerMBean()
     * ServerPlatformBase.unregisterMBean()
     *
     * for the desired behaviour.
     *
     * @see oracle.toplink.platform.server.ServerPlatformBase
     */
    public void setServerPlatform(ServerPlatform newServerPlatform);

    /**
     * PUBLIC:
     * Answer the server platform defining server-specific behaviour for the receiver (Oc4j, WLS, ...).
     *
      * If the user wants a different external transaction controller class or
     * to provide some different behaviour than the provided ServerPlatform(s), we recommend
     * subclassing oracle.toplink.platform.server.ServerPlatformBase (or a subclass),
     * and overriding:
     *
     * ServerPlatformBase.getExternalTransactionControllerClass()
     * ServerPlatformBase.registerMBean()
     * ServerPlatformBase.unregisterMBean()
     *
     * for the desired behaviour.
     *
     * @see oracle.toplink.platform.server.ServerPlatformBase
    */
    public ServerPlatform getServerPlatform();

    /**
     * PUBLIC:
     * Return  SequencingControl which used for sequencing setup and
     * customization including management of sequencing preallocation.
     */
    public SequencingControl getSequencingControl();
        
    /**
     * PUBLIC:
     * Connect to the database using the predefined login.
     * The login must have been assign when or after creating the session.
     *
     * @see #login(Login)
     */
    public void login() throws DatabaseException;

    /**
     * PUBLIC:
     * Connect to the database using the given user name and password.
     * The additional login information must have been preset in the session's login attribute.
     * This is the login that should be used if each user has their own id,
     * but all users share the same database configuration.
     * Under this login mode the password should not stay withint the login definition after login.
     */
    public void login(String userName, String password) throws DatabaseException;

    /**
     * PUBLIC:
     * Connect to the database using the given login.
     * The login may also the preset and the login() protocol called.
     * This is the login should only be used if each user has their own database configuration.
     * Under this login mode the password should not stay withint the login definition after login.
     */
    public void login(Login login) throws DatabaseException;

    /**
     * PUBLIC:
     * Disconnect from the database.
     *
     * @exception TopLinkException if a transaction is active, you must rollback any active transaction before logout.
     * @exception DatabaseException the database will also raise an error if their is an active transaction,
     * or a general error occurs.
     */
    public void logout() throws DatabaseException;

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * If the object is already locked this method will wait until the lock is released.
     * A no wait option is available through setting the lock mode.
     * @see #refreshAndLockObject(Object, lockMode)
     */
    public Object refreshAndLockObject(Object object);

    /**
     * PUBLIC:
     * Refresh the attributes of the object and of all of its private parts from the database.
     * The object will be pessimisticly locked on the database for the duration of the transaction.
     * <p>Lock Modes: ObjectBuildingQuery.NO_LOCK, LOCK, LOCK_NOWAIT
     */
    public Object refreshAndLockObject(Object object, short lockMode);

    /**
     * PUBLIC:
     * Rollback the active database transaction.
     * This allows a group of database modification to be commited or rolledback as a unit.
     * All writes/deletes will be sent to the database be will not be visible to other users until commit.
     * Although databases do not allow nested transaction,
     * TopLink supports nesting through only committing to the database on the outer commit.
     *
     * @exception DatabaseException if the database connection is lost or the rollback fails.
     * @exception ConcurrencyException if this session is not within a transaction.
     */
    public void rollbackTransaction() throws DatabaseException;

    /**
     * PUBLIC:
     * Used for JTS integration.  If your application requires to have JTS control transactions instead of TopLink an
     * external transaction controler must be specified.  TopLink provides JTS controlers for JTS 1.0 and Weblogic's JTS.
     * @see oracle.toplink.transaction.JTATransactionController
     * @see oracle.toplink.platform.server.CustomServerPlatform
     */
    //@deprecated was removed from this method as there is no viable alternative bug 5637867 was filed to
    // have this resolved.
    public void setExternalTransactionController(ExternalTransactionController etc);

    /**
     * ADVANCED:
     * Return the CommandManager that allows this session to act as a
     * CommandProcessor and receive or propagate commands from/to the
     * TopLink cluster.
     * This can be set to enable cache synchronization in a clustered environment where
     * multiple servers in the cluster update the same database.
     *
     * @see CommandManager
     * @return The CommandManager instance that controls the remote command
     * service for this session.
     */
    public CommandManager getCommandManager();

    /**
     * ADVANCED:
     * Set the CommandManager that allows this session to act as a
     * CommandProcessor and receive or propagate commands from/to the
     * TopLink cluster.
     * This can be used to enable cache synchronization in a clustered environment where
     * multiple servers in the cluster update the same database.
     * To enable cache synchronization you must also set, setShouldPropagateChanges to true.
     *
     * @see #setShouldPropagateChanges(boolean)
     * @see CommandManager
     * @param commandManager The CommandManager instance to control the remote command
     * service for this session.
     */
    public void setCommandManager(CommandManager commandManager);

    /**
     * ADVANCED:
     * Set if cache changes should be propagated to other sessions or applications
     * in a TopLink cluster through the Remote Command Manager mechanism.
     * This can be used to enable cache synchronization in a clustered environment where
     * multiple servers in the cluster update the same database.
     * In order for this to occur the CommandManager must be set.
     *
     * @see #setCommandManager(CommandManager)
     * @param choice If true (and the CommandManager is set) then propagation will occur.
     */
    public void setShouldPropagateChanges(boolean choice);

    /**
     * ADVANCED:
     * Return whether changes should be propagated to other sessions or applications
     * in a TopLink cluster through the Remote Command Manager mechanism. In order for
     * this to occur the CommandManager must be set.
     *
     * @see #setCommandManager(CommandManager)
     * @return true if propagation is set to occur, false if not.
     */
    public boolean shouldPropagateChanges();

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setLogin(Login login);

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setDatasourceLogin(Login login);

    /**
     * PUBLIC:
     * Update the object and all of its privately owned parts in the database.
     * Update should only be used if the application knows that the object is new,
     * otherwise writeObject should be used.
     * The update operation can be customized through using an update query.
     *
     * @see oracle.toplink.queryframework.UpdateObjectQuery
     * @see #writeObject(Object)
     */
    public Object updateObject(Object domainObject) throws DatabaseException, OptimisticLockException;

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be commited as a unit.
     * The objects will be commited through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Collection domainObjects);

    /**
     * PUBLIC:
     * Write all of the objects and all of their privately owned parts in the database.
     * The allows for a group of objects to be commited as a unit.
     * The objects will be commited through a single transactions.
     *
     * @exception DatabaseException if an error occurs on the database,
     * these include constraint violations, security violations and general database erros.
     * @exception OptimisticLockException if the object's descriptor is using optimistic locking and
     * the object has been updated or deleted by another user since it was last read.
     */
    public void writeAllObjects(Vector domainObjects);

    /**
     * PUBLIC:
     * Write the object and all of its privately owned parts in the database.
     * Write will determine if an insert or an update should be done,
     * it may go to the database to determine this (by default will check the identity map).
     * The write operation can be customized through using an write query.
     *
     * @see oracle.toplink.queryframework.WriteObjectQuery
     * @see #insertObject(Object)
     * @see #updateObject(Object)
     */
    public Object writeObject(Object domainObject) throws DatabaseException, OptimisticLockException;
}
