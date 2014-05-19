// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.rmi.wls;

import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.sessions.Login;
import oracle.toplink.internal.remote.*;
import oracle.toplink.remote.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.CommunicationException;

/**
 * This class exists on the client side which talks to remote session controller through
 * RMI connection.
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remote.rmi.RMIConnection}
 */
public class RMIConnection extends RemoteConnection {
    RMIRemoteSessionController remoteSessionController;

    /**
     * PUBLIC:
     * The connection must be create from the server-side session controllers stub.
     * The session in then created from the connection through createRemoteSession().
     * @see #createRemoteSession();
     */
    public RMIConnection(RMIRemoteSessionController controller) {
        this.remoteSessionController = controller;
    }

    /**
     * ADVANCED:
     * This method will send the command to the remote session for processing
     * @param command RemoteCOmmand Contains a command that will be executed on the remote session
     * @see oracle.toplink.internal.RemoteCommand
     */
    public void processCommand(RemoteCommand command) {
        try {
            Transporter transporter = new Transporter();
            transporter.setObject(command);
            transporter = getRemoteSessionController().processCommand(transporter);
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * ADVANCED:
     * This method is intended to be used by newly connecting nodes to notify the
     * other nodes in a distributed system to send changes to this calling server
     * @param remoteTransporter Transporter This transporter contains the RemoteDispatcher of the calling
     * server.
     * @deprecated Since 4.0
     */
    public void addRemoteControllerForSynchronization(Object remoteDispatcher) throws Exception {
        ConnectToSessionCommand command = new ConnectToSessionCommand();
        command.setRemoteConnection(new RMIConnection((RMIRemoteSessionController)remoteDispatcher));
        processCommand(command);
    }

    /**
     * INTERNAL:
     * Begin a transaction on the database.
     */
    public void beginTransaction() {
        try {
            Transporter transporter = getRemoteSessionController().beginTransaction();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Commit root unit of work from the client side to the server side.
     */
    public RemoteUnitOfWork commitRootUnitOfWork(RemoteUnitOfWork theRemoteUnitOfWork) {
        try {
            Transporter transporter = getRemoteSessionController().commitRootUnitOfWork(new Transporter(theRemoteUnitOfWork));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            } else {
                return (RemoteUnitOfWork)transporter.getObject();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Commit a transaction on the database.
     */
    public void commitTransaction() {
        try {
            Transporter transporter = getRemoteSessionController().commitTransaction();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * PUBLIC:
     * Returns a remote session.
     */
    public oracle.toplink.sessions.Session createRemoteSession() {
        return new RemoteSession(this);
    }

    /**
     * Used for closing cursored streams across RMI.
     */
    public void cursoredStreamClose(ObjID remoteCursoredStreamOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().cursoredStreamClose(new Transporter(remoteCursoredStreamOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
    }

    /**
     * Retrieve next page size of objects from the remote cursored stream
     */
    public Vector cursoredStreamNextPage(RemoteCursoredStream remoteCursoredStream, ReadQuery query, RemoteSession session, int pageSize) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().cursoredStreamNextPage(new Transporter(remoteCursoredStream.getID()), pageSize);
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return null;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }

        Vector serverNextPageObjects = (Vector)transporter.getObject();
        if (serverNextPageObjects == null) {
            cursoredStreamClose(remoteCursoredStream.getID());
            return null;
        }
        Vector clientNextPageObjects = serverNextPageObjects;
        if (query.isReadAllQuery() && (!query.isReportQuery())) {// could be DataReadQuery
            clientNextPageObjects = new Vector(serverNextPageObjects.size());
            for (Enumeration objEnum = serverNextPageObjects.elements(); objEnum.hasMoreElements();) {
                // 2612538 - the default size of IdentityHashtable (32) is appropriate
                Object clientObject = session.getObjectCorrespondingTo(objEnum.nextElement(), transporter.getObjectDescriptors(), new IdentityHashtable(), (ObjectLevelReadQuery)query);
                clientNextPageObjects.addElement(clientObject);
            }
        }

        return clientNextPageObjects;

    }

    /**
     * Return the cursored stream size
     */
    public int cursoredStreamSize(ObjID remoteCursoredStreamID) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().cursoredStreamSize(new Transporter(remoteCursoredStreamID));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Integer)transporter.getObject()).intValue();

    }

    /**
     * INTERNAL:
     * Returns remote cursor stream
     */
    public RemoteCursoredStream cursorSelectObjects(CursoredStreamPolicy policy, DistributedSession session) {
        try {
            Transporter transporter = getRemoteSessionController().cursorSelectObjects(new Transporter(policy));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }

            RemoteCursoredStream remoteCursoredStream = (RemoteCursoredStream)transporter.getObject();
            remoteCursoredStream.setSession((RemoteSession)session);
            remoteCursoredStream.setPolicy(policy);

            if (policy.getQuery().isReadAllQuery() && (!policy.getQuery().isReportQuery())) {// could be DataReadQuery
                fixObjectReferences(transporter, (ObjectLevelReadQuery)policy.getQuery(), (RemoteSession)session);
            }
            return remoteCursoredStream;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Returns remote cursor stream
     */
    public RemoteScrollableCursor cursorSelectObjects(ScrollableCursorPolicy policy, DistributedSession session) {
        try {
            Transporter transporter = getRemoteSessionController().cursorSelectObjects(new Transporter(policy));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }

            RemoteScrollableCursor remoteScrollableCursor = (RemoteScrollableCursor)transporter.getObject();
            remoteScrollableCursor.setSession((RemoteSession)session);
            remoteScrollableCursor.setPolicy(policy);

            return remoteScrollableCursor;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * An object has been serialized from the server to the remote client.
     * Replace the transient attributes of the remote value holders with client-side objects.
     * Being used for the cursored stream only
     */
    public void fixObjectReferences(Transporter remoteCursoredStream, ObjectLevelReadQuery query, RemoteSession session) {
        RemoteCursoredStream stream = (RemoteCursoredStream)remoteCursoredStream.getObject();
        Vector remoteObjectCollection = (Vector)stream.getObjectCollection();
        if (query.isReadAllQuery() && (!query.isReportQuery())) {// could be DataReadQuery
            Vector clientObjectCollection = new Vector(remoteObjectCollection.size());

            // find next power-of-2 size
            IdentityHashtable recursiveSet = new IdentityHashtable(remoteObjectCollection.size() + 1);
            for (Enumeration enumtr = remoteObjectCollection.elements(); enumtr.hasMoreElements();) {
                Object serverSideDomainObject = enumtr.nextElement();
                clientObjectCollection.addElement(session.getObjectCorrespondingTo(serverSideDomainObject, remoteCursoredStream.getObjectDescriptors(), recursiveSet, query));
            }
            stream.setObjectCollection(clientObjectCollection);
        }
    }

    /**
     * INTERNAL
     * Return the read-only classes
     */
    public Vector getDefaultReadOnlyClasses() {
        try {
            Transporter transporter = getRemoteSessionController().getDefaultReadOnlyClasses();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            } else {
                return (Vector)transporter.getObject();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Return the table descriptor specified for the class.
     */
    public ClassDescriptor getDescriptor(Class domainClass) {
        try {
            Transporter transporter = getRemoteSessionController().getDescriptor(new Transporter(domainClass));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            } else {
                return (ClassDescriptor)transporter.getObject();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Return the table descriptor specified for the class.
     */
    public Login getLogin() {
        try {
            Transporter transporter = getRemoteSessionController().getLogin();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            } else {
                return (Login)transporter.getObject();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Return the remote session controller
     */
    public RMIRemoteSessionController getRemoteSessionController() {
        return remoteSessionController;
    }

    /**
     * INTERNAL:
     * Perform remote function call
     */
    public Object getSequenceNumberNamed(Object remoteFunctionCall) {
        try {
            Transporter transporter = getRemoteSessionController().getSequenceNumberNamed(new Transporter(remoteFunctionCall));
            Object returnValue = transporter.getObject();

            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }

            return returnValue;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Reset the cache on the server-side session.
     */
    public void initializeIdentityMapsOnServerSession() {
        try {
            Transporter transporter = getRemoteSessionController().initializeIdentityMapsOnServerSession();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Instantiate remote value holder on the server
     */
    public Transporter instantiateRemoteValueHolderOnServer(RemoteValueHolder remoteValueHolder) {
        try {
            Transporter transporter = getRemoteSessionController().instantiateRemoteValueHolderOnServer(new Transporter(remoteValueHolder));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
            return transporter;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Execute the query on the server.
     */
    public Transporter remoteExecute(DatabaseQuery query) {
        try {
            Transporter transporter = getRemoteSessionController().executeQuery(new Transporter(query));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
            return transporter;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Execute query remotely.
     */
    public Transporter remoteExecuteNamedQuery(String name, Class javaClass, Vector arguments) {
        try {
            Transporter transporter = getRemoteSessionController().executeNamedQuery(new Transporter(name), new Transporter(javaClass), new Transporter(arguments));
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
            return transporter;
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * INTERNAL:
     * Rollback a transaction on the database.
     */
    public void rollbackTransaction() {
        try {
            Transporter transporter = getRemoteSessionController().rollbackTransaction();
            if (!transporter.wasOperationSuccessful()) {
                throw transporter.getException();
            }
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
    }

    /**
     * Moves the cursor to the given row number in the result set
     */
    public boolean scrollableCursorAbsolute(ObjID remoteScrollableCursorOid, int rows) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorAbsolute(new Transporter(remoteScrollableCursorOid), rows);
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Moves the cursor to the end of the result set, just after the last row.
     */
    public void scrollableCursorAfterLast(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorAfterLast(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
    }

    /**
     * Moves the cursor to the front of the result set, just before the first row
     */
    public void scrollableCursorBeforeFirst(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorBeforeFirst(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
    }

    /**
     * Used for closing scrollable cursor across RMI.
     */
    public void scrollableCursorClose(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorClose(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
    }

    /**
     * Retrieves the current row index number
     */
    public int scrollableCursorCurrentIndex(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorAfterLast(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (transporter == null) {
            return -1;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Integer)transporter.getObject()).intValue();
    }

    /**
     * Moves the cursor to the first row in the result set
     */
    public boolean scrollableCursorFirst(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorFirst(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Indicates whether the cursor is after the last row in the result set.
     */
    public boolean scrollableCursorIsAfterLast(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorIsAfterLast(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Indicates whether the cursor is before the first row in the result set.
     */
    public boolean scrollableCursorIsBeforeFirst(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorIsBeforeFirst(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }

        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Indicates whether the cursor is on the first row of the result set.
     */
    public boolean scrollableCursorIsFirst(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorIsFirst(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Indicates whether the cursor is on the last row of the result set.
     */
    public boolean scrollableCursorIsLast(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorIsLast(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Moves the cursor to the last row in the result set
     */
    public boolean scrollableCursorLast(ObjID remoteScrollableCursorOid) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorLast(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Retrieve next object from the remote scrollable cursor
     */
    public Object scrollableCursorNextObject(ObjID remoteScrollableCursorOid, ReadQuery query, RemoteSession session) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorNextObject(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return null;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }

        Object object = transporter.getObject();
        if (object == null) {
            // For bug 2797683 do not close if at end of stream.
            return null;
        }

        if (query.isReadAllQuery() && (!query.isReportQuery())) {// could be DataReadQuery
            object = session.getObjectCorrespondingTo(object, transporter.getObjectDescriptors(), new oracle.toplink.internal.helper.IdentityHashtable(), (ObjectLevelReadQuery)query);
        }
        return object;
    }

    /**
     * Retrieve previous object from the remote scrollable cursor
     */
    public Object scrollableCursorPreviousObject(ObjID remoteScrollableCursorOid, ReadQuery query, RemoteSession session) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorPreviousObject(new Transporter(remoteScrollableCursorOid));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return null;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }

        Object object = transporter.getObject();
        if (object == null) {
            // For bug 2797683 do not close if at end of stream.
            return null;
        }

        if (query.isReadAllQuery() && (!query.isReportQuery())) {// could be DataReadQuery
            object = session.getObjectCorrespondingTo(object, transporter.getObjectDescriptors(), new oracle.toplink.internal.helper.IdentityHashtable(), (ObjectLevelReadQuery)query);
        }
        return object;
    }

    /**
     * Moves the cursor to the given row number in the result set
     */
    public boolean scrollableCursorRelative(ObjID remoteScrollableCursorOid, int rows) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorRelative(new Transporter(remoteScrollableCursorOid), rows);
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }

        if (transporter == null) {
            return false;
        }

        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Boolean)transporter.getObject()).booleanValue();
    }

    /**
     * Return the scrollable cursor size
     */
    public int scrollableCursorSize(ObjID cursorId) {
        Transporter transporter = null;
        try {
            transporter = getRemoteSessionController().scrollableCursorSize(new Transporter(cursorId));
        } catch (RemoteException exception) {
            throw CommunicationException.errorInInvocation(exception);
        }
        if (!transporter.wasOperationSuccessful()) {
            throw transporter.getException();
        }
        return ((Integer)transporter.getObject()).intValue();

    }

    /**
     * INTERNAL:
     * Set remote session controller
     */
    public void setRemoteSessionController(RMIRemoteSessionController remoteSessionController) {
        this.remoteSessionController = remoteSessionController;
    }
}