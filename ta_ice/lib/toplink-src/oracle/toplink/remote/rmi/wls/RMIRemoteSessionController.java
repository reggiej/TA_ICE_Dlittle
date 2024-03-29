// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.rmi.wls;

import java.rmi.*;
import oracle.toplink.internal.remote.*;

/**
 * Defines the public methods remote connection can invoke on the remote session controller.
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remote.rmi.RMIRemoteSessionController}
 */
public interface RMIRemoteSessionController extends java.rmi.Remote {

    /**
     * INTERNAL:
     * This method is intended to be used by by sessions that wish to execute a command on a
     * remote session
     * @param remoteCommand RemoteCommand The command to be executed on the remote session
     */
    public Transporter processCommand(Transporter remoteTransporter) throws RemoteException;

    /**
     * Begin a transaction on the database.
     */
    public Transporter beginTransaction() throws RemoteException;

    /**
     * To commit remote unit of work on the server side.
     */
    public Transporter commitRootUnitOfWork(Transporter remoteUnitOfWork) throws RemoteException;

    /**
     * Commit a transaction on the database.
     */
    public Transporter commitTransaction() throws RemoteException;

    /**
     * Used for closing cursored streams across RMI.
     */
    public Transporter cursoredStreamClose(Transporter remoetCursoredStreamID) throws RemoteException;

    /**
     * Retrieve next page size of objects from the remote cursored stream
     */
    public Transporter cursoredStreamNextPage(Transporter remoteCursoredStream, int pageSize) throws RemoteException;

    /**
     * INTERNAL:
     * Return the cursored stream size
     */
    public Transporter cursoredStreamSize(Transporter cursoredStream) throws RemoteException;

    /**
     * To get remote cursor stub in a transporter
     */
    public Transporter cursorSelectObjects(Transporter policy) throws RemoteException;

    /**
     * A remote query after serialization is executed locally.
     */
    public Transporter executeNamedQuery(Transporter name, Transporter theClass, Transporter arguments) throws RemoteException;

    /**
     * To execute remote query on the server side.
     */
    public Transporter executeQuery(Transporter query) throws RemoteException;

    /**
     * To get the default read-only classes from the server side.
     **/
    public Transporter getDefaultReadOnlyClasses() throws RemoteException;

    /**
     * To get descriptor from the server side
     */
    public Transporter getDescriptor(Transporter domainClass) throws RemoteException;

    /**
     * To get login from the server side
     */
    public Transporter getLogin() throws RemoteException;

    /**
     * INTERNAL:
     * Get the value returned by remote function call
     */
    public Transporter getSequenceNumberNamed(Transporter remoteFunctionCall) throws RemoteException;

    public Transporter initializeIdentityMapsOnServerSession() throws RemoteException;

    /**
     * To instantiate remote value holder on the server side.
     */
    public Transporter instantiateRemoteValueHolderOnServer(Transporter remoteValueHolder) throws RemoteException;

    /**
     * Rollback a transaction on the database.
     */
    public Transporter rollbackTransaction() throws RemoteException;

    /**
     * Moves the cursor to the given row number in the result set
     */
    public Transporter scrollableCursorAbsolute(Transporter remoteScrollableCursorOid, int rows) throws RemoteException;

    /**
     * Moves the cursor to the end of the result set, just after the last row.
     */
    public Transporter scrollableCursorAfterLast(Transporter remoteScrollableCursorOid) throws RemoteException;

    /**
     * Moves the cursor to the front of the result set, just before the first row
     */
    public Transporter scrollableCursorBeforeFirst(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Used for closing scrollable cursor across RMI.
     */
    public Transporter scrollableCursorClose(Transporter remoteScrollableCursorOid) throws RemoteException;

    /**
     * Retrieves the current row index number
     */
    public Transporter scrollableCursorCurrentIndex(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Moves the cursor to the first row in the result set
     */
    public Transporter scrollableCursorFirst(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Indicates whether the cursor is after the last row in the result set.
     */
    public Transporter scrollableCursorIsAfterLast(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Indicates whether the cursor is before the first row in the result set.
     */
    public Transporter scrollableCursorIsBeforeFirst(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Indicates whether the cursor is on the first row of the result set.
     */
    public Transporter scrollableCursorIsFirst(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Indicates whether the cursor is on the last row of the result set.
     */
    public Transporter scrollableCursorIsLast(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * Moves the cursor to the last row in the result set
     */
    public Transporter scrollableCursorLast(Transporter remoteScrollableCursor) throws RemoteException;

    /**
     * INTERNAL:
     * Retrieve next object from the scrollable cursor
     */
    public Transporter scrollableCursorNextObject(Transporter scrollableCursorOid) throws RemoteException;

    /**
     * INTERNAL:
     * Retrieve previous object from the scrollable cursor
     */
    public Transporter scrollableCursorPreviousObject(Transporter scrollableCursorOid) throws RemoteException;

    /**
     * Moves the cursor to the given row number in the result set
     */
    public Transporter scrollableCursorRelative(Transporter remoteScrollableCursor, int rows) throws RemoteException;

    /**
     * INTERNAL:
     * Return the cursor size
     */
    public Transporter scrollableCursorSize(Transporter cursoredStream) throws RemoteException;
}