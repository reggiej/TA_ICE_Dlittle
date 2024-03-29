// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remotecommand;

import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Provide an extendable framework class for a Command object
 * that can be remotely executed.
 * <p>
 * <b>Description</b>: Is the root command class from which all other remotely
 * executable commands must extend. A Command is invoked by calling
 * propagateCommand() on a local CommandManager, and is executed on each remote
 * service by each remote CommandManager invoking processCommand() on its local
 *
 * @see CommandManager
 * @see CommandProcessor
 * @author Steven Vo
 * @since OracleAS TopLink 10<i>g</i> (9.0.4)
 */
public abstract class Command implements java.io.Serializable {

    /** The unique calling card of the service that initiated the command */
    ServiceId serviceId;

    /**
     * INTERNAL:
     * If the CommandProcessor is a TopLink session then this method will
     * get executed.
     *
     * @param session The session that can be used to execute the command on.
     */
    public abstract void executeWithSession(AbstractSession session);

    /**
     * PUBLIC:
     * Return the service identifier of the service where the command originated
     *
     * @return The unique identifier of the sending RCM service
     */
    public ServiceId getServiceId() {
        return serviceId;
    }

    /**
     * ADVANCED:
     * Set the service identifier of the service where the command originated
     *
     * @param newServiceId The unique identifier of the sending RCM service
     */
    public void setServiceId(ServiceId newServiceId) {
        serviceId = newServiceId;
    }

    /**
     * INTERNAL:
     * Determine whether this command is public or internal to TopLink.
     * User commands must return false.
     */
    public boolean isInternalCommand() {
        return false;
    }
}