// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.publicinterface;

import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.remotecommand.CommandProcessor;

/**
 * Implementation of oracle.toplink.sessions.Session
 * The public interface should be used.
 * @see oracle.toplink.sessions.Session
 *
 * <p>
 * <b>Purpose</b>: Define the interface and common protocol of a TopLink compliant session.
 * <p>
 * <b>Description</b>: The session is the primary interface into TopLink,
 * the application should do all of its reading and writing of objects through the session.
 * The session also manages transactions and units of work.  Normally the session
 * is passed and used by the application controler objects.  Controler objects normally
 * sit behind the GUI and perform the buiness processes required for the application,
 * they should perform all explict database access and database access should be avoided from
 * the domain object model.  Do not use a globally accessable session instance, doing so does
 * not allow for multiple sessions.  Multiple sessions may required when performing things like
 * data migration or multiple database access, as well the unit of work feature requires the usage
 * of multiple session instances.  Although session is abstract, any users of its subclasses
 * should only cast the variables to Session to allow usage of any of its subclasses.
 * <p>
 * <b>Responsibilities</b>:
 *    <ul>
 *    <li> Connecting/disconnecting.
 *    <li> Reading and writing objects.
 *    <li> Transaction and unit of work support.
 *    <li> Identity maps and caching.
 *    </ul>
 * @see DatabaseSession
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.sessions.Session}, and
 *         {@link oracle.toplink.internal.sessions.AbstractSession}
 */
public abstract class Session extends AbstractSession implements oracle.toplink.sessions.Session, CommandProcessor, java.io.Serializable, java.lang.Cloneable {

    /**
     * INTERNAL:
     * Create and return a new session.
     * This should only be called if the database login information is not know at the time of creation.
     * Normally it is better to call the constructor that takes the login information as an argument
     * so that the session can initialize itself to the platform information given in the login.
     */
    protected Session() {
        super();
    }

}
