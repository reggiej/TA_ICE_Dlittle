// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote;

import java.util.*;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.internal.descriptors.OptimisticLockingPolicy;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.internal.remote.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.queryframework.*;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.Login;
import oracle.toplink.internal.sequencing.Sequencing;
import oracle.toplink.internal.sequencing.SequencingFactory;
import oracle.toplink.logging.SessionLog;

/**
 * <b>Purpose</b>: Provide transparent remote three-tiered replacation support.
 * The remote session allows for complex three-tiered applications to be easily built.
 * It gives the remote client the fully functionality of the TopLink api including,</p>
 * <p><ul>
 * <li>Client side caching and object-identity maintainence.
 * <li>Complex query support
 * <li>Unit of work support
 * <li>Indirection support through remote value holders.
 * </ul></p>
 *
 * This session is a primary interface which resides on the client side. Users would interact
 * with session just the same way as if it was a normal session.
 */
public class RemoteSession extends DistributedSession {
    protected Sequencing sequencing;
    protected boolean shouldEnableDistributedIndirectionGarbageCollection = false;

    /**
     * PUBLIC:
     * Creates a RemoteSession.
     * @param remoteConnection remote session requires a remote connection. This must be accessed remotely from the client through RMI or CORBA.
     */
    public RemoteSession(RemoteConnection remoteConnection) {
        super(remoteConnection);
        initializeSequencing();
    }

    /**
     * ADVANCED:
     * Allow the server-side value holders to be cleaned-up when the client-side value holder finalize.
     */
    public void setShouldEnableDistributedIndirectionGarbageCollection(boolean shouldEnableDistributedIndirectionGarbageCollection) {
        this.shouldEnableDistributedIndirectionGarbageCollection = shouldEnableDistributedIndirectionGarbageCollection;
    }

    /**
     * ADVANCED:
     * Allow the server-side value holders to be cleaned-up when the client-side value holder finalize.
     */
    public boolean shouldEnableDistributedIndirectionGarbageCollection() {
        return shouldEnableDistributedIndirectionGarbageCollection;
    }

    /**
     * INTERNAL:
     * Acquires a special historical session for reading objects as of a past time.
     */
    public oracle.toplink.sessions.Session acquireHistoricalSession(oracle.toplink.history.AsOfClause clause) throws oracle.toplink.exceptions.ValidationException {
        throw oracle.toplink.exceptions.ValidationException.cannotAcquireHistoricalSession();
    }

    /**
     * PUBLIC:
     * Return a unit of work for this session.
     * The unit of work is an object level transaction that allows
     * a group of changes to be applied as a unit.
     *
     * @see UnitOfWorkImpl
     */
    public UnitOfWorkImpl acquireUnitOfWork() {
        log(SessionLog.FINER, SessionLog.TRANSACTION, "acquire_unit_of_work");
        setNumberOfActiveUnitsOfWork(getNumberOfActiveUnitsOfWork() + 1);
        return new RemoteUnitOfWork(this);
    }

    /**
     * PUBLIC:
     * Execute the database query.
     */
    public Object executeQuery(DatabaseQuery query) {
        return query.remoteExecute(this);
    }

    /**
     * OBSOLETE:
     * Return the login, the login holds any database connection information given.
     * This has been replaced by getDatasourceLogin to make use of the Login interface
     * to support non-relational datasources,
     * if DatabaseLogin API is required it will need to be cast.
     * @deprecated replaced by getDatasourceLogin()
     */
    public DatabaseLogin getLogin() {
        return (DatabaseLogin)getDatasourceLogin();
    }

    /**
     * PUBLIC:
     * Return the login.
     * This must retreive the login information from the server this first time called.
     * This is useful to be able to do things differently depending on the database platform.
     */
    public Login getDatasourceLogin() {
        Login login = super.getDatasourceLogin();
        if (login == null) {
            login = getRemoteConnection().getLogin();
            setDatasourceLogin(login);
        }

        return login;
    }

    /**
     * INTERNAL:
     * Return the corresponding objects from the remote session for the objects read from the server.
     */
    public Object getObjectCorrespondingTo(Object serverSideDomainObject, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query) {
        if (serverSideDomainObject == null) {
            return null;
        }

        ClassDescriptor descriptor = getDescriptor(serverSideDomainObject);

        // CR... fix to descriptor iterator exposed the bug that we were putting aggregate-collections in the cache.
        if (descriptor.isAggregateCollectionDescriptor() || ((query != null) && (!query.shouldMaintainCache()))) {
            if ((query != null) && (!query.hasPartialAttributeExpressions())) {
                descriptor.getObjectBuilder().fixObjectReferences(serverSideDomainObject, objectDescriptors, processedObjects, query, this);
            }
            return serverSideDomainObject;
        }

        // Extract the object primary key and check if it exist on the remote session or not. If we find an object
        // with this primary key then that's the corresponding object. Other wise its a new object for the remote 
        // session which needs to be registered in the remote sessions identity map and this is also a corresponding 
        // object.
        ObjectDescriptor objectDescriptor = (ObjectDescriptor)objectDescriptors.get(serverSideDomainObject);
        if (objectDescriptor == null){
            //the object must have been added concurently before serialize generate a new ObjectDescriptor on this side
            objectDescriptor = new ObjectDescriptor();
            objectDescriptor.setKey(descriptor.getObjectBuilder().extractPrimaryKeyFromObject(serverSideDomainObject, this));
            objectDescriptor.setObject(serverSideDomainObject);
            OptimisticLockingPolicy policy = descriptor.getOptimisticLockingPolicy();
            if (policy == null){
                objectDescriptor.setWriteLockValue(null);
            }else{
                objectDescriptor.setWriteLockValue(policy.getBaseValue());
            }
            objectDescriptors.put(serverSideDomainObject, objectDescriptor);
        }
        Vector primaryKeyValues = objectDescriptor.getKey();
        Object clientSideDomainObject = getIdentityMapAccessorInstance().getFromIdentityMap(primaryKeyValues, serverSideDomainObject.getClass(), descriptor);

        // If object is already processed the return back, this check must be done after the cliet-side object is found.
        if (processedObjects.containsKey(serverSideDomainObject)) {
            if (clientSideDomainObject == null) {
                return serverSideDomainObject;
            } else {
                return clientSideDomainObject;
            }
        }

        processedObjects.put(serverSideDomainObject, serverSideDomainObject);

        if (clientSideDomainObject == null) {
            getIdentityMapAccessorInstance().putInIdentityMap(serverSideDomainObject, primaryKeyValues, objectDescriptor.getWriteLockValue(), objectDescriptor.getReadTime(), descriptor);
            descriptor.getObjectBuilder().fixObjectReferences(serverSideDomainObject, objectDescriptors, processedObjects, query, this);
            clientSideDomainObject = serverSideDomainObject;
        } else {
            // if the query is null, that means we refreshed a newly-created client object at some point
            // and we should refresh the identity map and cascade private parts
            if ((query == null) || (query.shouldRefreshRemoteIdentityMapResult()) || getDescriptor(clientSideDomainObject).shouldAlwaysRefreshCacheOnRemote()) {
                MergeManager mergeManager = new MergeManager(this);
                mergeManager.refreshRemoteObject();
                mergeManager.setObjectDescriptors(objectDescriptors);
                if (query == null) {
                    mergeManager.cascadePrivateParts();
                } else {
                    mergeManager.setCascadePolicy(query.getCascadePolicy());
                }
                clientSideDomainObject = mergeManager.mergeChanges(serverSideDomainObject, null);
            }
        }

        return clientSideDomainObject;
    }

    /**
     * INTERNAL:
     * Return the corresponding objects from the remote session for the objects read from the server.
     */
    public Object getObjectsCorrespondingToAll(Object serverSideDomainObjects, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, ContainerPolicy containerPolicy) {
        Object clientSideDomainObjects = containerPolicy.containerInstance(containerPolicy.sizeFor(serverSideDomainObjects));

        for (Object iter = containerPolicy.iteratorFor(serverSideDomainObjects);
                 containerPolicy.hasNext(iter);) {
            Object serverSideDomainObject = containerPolicy.next(iter, this);
            containerPolicy.addInto(getObjectCorrespondingTo(serverSideDomainObject, objectDescriptors, processedObjects, query), clientSideDomainObjects, this);
        }

        return clientSideDomainObjects;
    }

    /**
     * INTERNAL:
     * This will instantiate value holder on the server.
     */
    public Object instantiateRemoteValueHolderOnServer(RemoteValueHolder remoteValueHolder) {
        Transporter transporter = getRemoteConnection().instantiateRemoteValueHolderOnServer(remoteValueHolder);
        return remoteValueHolder.getMapping().getObjectCorrespondingTo(transporter.getObject(), this, transporter.getObjectDescriptors(), new IdentityHashtable(), remoteValueHolder.getQuery());
    }

    /**
     * INTERNAL:
     * Return if this session is remote.
     */
    public boolean isRemoteSession() {
        return true;
    }

    /**
     * INTERNAL:
     * Return the Sequencing object used by the session.
     */
    public Sequencing getSequencing() {
        return sequencing;
    }

    /**
    * ADVANCED:
    * Creates sequencing object for the session.
    * Typically there is no need for the user to call this method -
    * it is called from the constructor.
    */
    public void initializeSequencing() {
        sequencing = SequencingFactory.createSequencing(this);
    }
}