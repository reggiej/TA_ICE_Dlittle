// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import oracle.toplink.sessionbroker.*;
import java.util.Vector;
import oracle.toplink.sessions.*;
import oracle.toplink.exceptions.*;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>: This class is used to represent a Session Broker within a SessionManager.
 * If a session Broker is requested from the SessionManager then this object is created.  Once
 * all of the required sessions have been loaded into the SesssionManger then the SessionBroker
 * will be returned.  Before that null will be returned.
 *
 * @since TopLink 4.0
 * @author Gordon Yorke
 */
public class SessionBrokerPlaceHolder extends oracle.toplink.sessionbroker.SessionBroker {

    /** This member variable stores the sessions that have been retreived */
    protected Vector sessionsCompleted;

    /** This member variable stores the sessions that need to be retreived */
    protected Vector sessionNamesRequired;

    public SessionBrokerPlaceHolder() {
        super();
        this.sessionNamesRequired = new Vector();
        this.sessionsCompleted = new Vector();
    }

    public void addSessionName(String sessionName) {
        this.sessionNamesRequired.add(sessionName);
    }

    public Vector getSessionNamesRequired() {
        return this.sessionNamesRequired;
    }

    public Vector getSessionCompleted() {
        return this.sessionsCompleted;
    }
}