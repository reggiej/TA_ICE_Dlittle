// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.rmi.wls;

import java.rmi.*;
import oracle.toplink.internal.remote.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * RemoteSessionController sits between the remote session and the session. Any interaction between these
 * two classes takes place through this object. As the object extends unicast remote object it listens to
 * only single remote session during runtime.
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remote.rmi.RMIRemoteSessionControllerDispatcher}
 */
public class RMIRemoteSessionControllerDispatcher extends weblogic.rmi.server.UnicastRemoteObject implements RMIRemoteSessionController {

    /** Store the real controller. */
    protected RemoteSessionController controller;

    public RMIRemoteSessionControllerDispatcher(AbstractSession session) throws RemoteException {
        // This call to the super is required in RMI.
        super();
        this.controller = new RemoteSessionController(session);
    }

    public RMIRemoteSessionControllerDispatcher(oracle.toplink.sessions.Session session) throws RemoteException {
        this((AbstractSession)session);
    }

    /**
     * INTERNAL:
     * This method is intended to be used by by sessions that wish to execute a command on a
     * remote session
     * @param remoteCommand RemoteCommand The command to be executed on the remote session
     */
    public Transporter processCommand(Transporter remoteTransporter) {
        return getController().processCommand(remoteTransporter);
    }

    /**
     * Begin a transaction on the database.
     */
    public Transporter beginTransaction() {
        return getController().beginTransaction();
    }

    /**
     * Remote unit of work after serialization is commited locally.
     */
    public Transporter commitRootUnitOfWork(Transporter remoteUnitOfWork) {
        return getController().commitRootUnitOfWork(remoteUnitOfWork);
    }

    /**
     * Commit a transaction on the database.
     */
    public Transporter commitTransaction() {
        return getController().commitTransaction();
    }

    /**
     * Used for closing cursored streams across RMI.
     */
    public Transporter cursoredStreamClose(Transporter remoteCursoredStreamID) {
        return getController().cursoredStreamClose(remoteCursoredStreamID);
    }

    /**
     * Retrieve next page size of objects from the remote cursored stream
     */
    public Transporter cursoredStreamNextPage(Transporter remoteCursoredStream, int pageSize) {
        return getController().cursoredStreamNextpage(remoteCursoredStream, pageSize);
    }

    /**
     * Return the cursored stream size
     */
    public Transporter cursoredStreamSize(Transporter remoteCursoredStreamOid) {
        return getController().cursoredStreamSize(remoteCursoredStreamOid);
    }

    /**
     * Returns a remote cursor stub in a transporter
     */
    public Transporter cursorSelectObjects(Transporter remoteTransporter) {
        Transporter transporter = getController().cursorSelectObjects(remoteTransporter);

        return transporter;
    }

    /**
     * A remote query after serialization is executed locally.
     */
    public Transporter executeNamedQuery(Transporter nameTransporter, Transporter classTransporter, Transporter argumentsTransporter) {
        return getController().executeNamedQuery(nameTransporter, classTransporter, argumentsTransporter);
    }

    /**
     * A remote query after serialization is executed locally.
     */
    public Transporter executeQuery(Transporter query) {
        return getController().executeQuery(query);
    }

    /**
     * Return the controller.  All work is dispatched to the controller.
     * This is required to be protocol independent.
     */
    protected RemoteSessionController getController() {
        return controller;
    }

    /**
     * Get the default read-only classes
     **/
    public Transporter getDefaultReadOnlyClasses() {
        return getController().getDefaultReadOnlyClasses();
    }

    /**
     * Extract descriptor from the session
     */
    public Transporter getDescriptor(Transporter theClass) {
        return getController().getDescriptor(theClass);
    }

    /**
     * Get the associated session login.
     */
    public Transporter getLogin() {
        return getController().getLogin();
    }

    /**
     * INTERNAL:
     * Get the value returned by remote function call
     */
    public Transporter getSequenceNumberNamed(Transporter remoteFunctionCall) {
        return getController().getSequenceNumberNamed(remoteFunctionCall);
    }

    public Transporter initializeIdentityMapsOnServerSession() {
        return getController().initializeIdentityMapsOnServerSession();
    }

    /**
     * The corresponding original value holder is instantiated.
     */
    public Transporter instantiateRemoteValueHolderOnServer(Transporter remoteValueHolder) {
        return getController().instantiateRemoteValueHolderOnServer(remoteValueHolder);
    }

    /**
     * Rollback a transaction on the database.
     */
    public Transporter rollbackTransaction() {
        return getController().rollbackTransaction();
    }

    /**
     * Moves the cursor to the given row number in the result set
     */
    public Transporter scrollableCursorAbsolute(Transporter remoteScrollableCursorOid, int rows) {
        return getController().scrollableCursorAbsolute(remoteScrollableCursorOid, rows);
    }

    /**
     * Moves the cursor to the end of the result set, just after the last row.
     */
    public Transporter scrollableCursorAfterLast(Transporter remoteScrollableCursorOid) {
        return getController().scrollableCursorAfterLast(remoteScrollableCursorOid);
    }

    /**
     * Moves the cursor to the front of the result set, just before the first row
     */
    public Transporter scrollableCursorBeforeFirst(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorBeforeFirst(remoteScrollableCursor);
    }

    /**
     * Used for closing scrollable cursor across RMI.
     */
    public Transporter scrollableCursorClose(Transporter remoteScrollableCursorOid) {
        return getController().scrollableCursorClose(remoteScrollableCursorOid);
    }

    /**
     * Retrieves the current row index number
     */
    public Transporter scrollableCursorCurrentIndex(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorCurrentIndex(remoteScrollableCursor);
    }

    /**
     * Moves the cursor to the first row in the result set
     */
    public Transporter scrollableCursorFirst(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorFirst(remoteScrollableCursor);
    }

    /**
     * Indicates whether the cursor is after the last row in the result set.
     */
    public Transporter scrollableCursorIsAfterLast(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorIsAfterLast(remoteScrollableCursor);
    }

    /**
     * Indicates whether the cursor is before the first row in the result set.
     */
    public Transporter scrollableCursorIsBeforeFirst(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorIsBeforeFirst(remoteScrollableCursor);
    }

    /**
     * Indicates whether the cursor is on the first row of the result set.
     */
    public Transporter scrollableCursorIsFirst(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorIsFirst(remoteScrollableCursor);
    }

    /**
     * Indicates whether the cursor is on the last row of the result set.
     */
    public Transporter scrollableCursorIsLast(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorIsLast(remoteScrollableCursor);
    }

    /**
     * Moves the cursor to the last row in the result set
     */
    public Transporter scrollableCursorLast(Transporter remoteScrollableCursor) {
        return getController().scrollableCursorLast(remoteScrollableCursor);
    }

    /**
     * Retrieve next object from the scrollable cursor
     */
    public Transporter scrollableCursorNextObject(Transporter scrollableCursorOid) {
        return getController().scrollableCursorNextObject(scrollableCursorOid);
    }

    /**
     * Retrieve previous object from the scrollable cursor
     */
    public Transporter scrollableCursorPreviousObject(Transporter scrollableCursorOid) {
        return getController().scrollableCursorPreviousObject(scrollableCursorOid);
    }

    /**
     * Moves the cursor to the given row number in the result set
     */
    public Transporter scrollableCursorRelative(Transporter remoteScrollableCursor, int rows) {
        return getController().scrollableCursorRelative(remoteScrollableCursor, rows);
    }

    /**
     * Return the cursor size
     */
    public Transporter scrollableCursorSize(Transporter remoteCursorOid) {
        return getController().scrollableCursorSize(remoteCursorOid);
    }

    /**
     * Set the controller.  All work is dispatched to the controller.
     * This is required to be protocol independent.
     */
    protected void setController(RemoteSessionController controller) {
        this.controller = controller;
    }
}