// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.session;

import java.util.Vector;

/**
 * INTERNAL:
 */
public class SessionBrokerConfig extends SessionConfig {
    private Vector m_sessionNames;

    public SessionBrokerConfig() {
        super();
        m_sessionNames = new Vector();
    }

    public void addSessionName(String sessionName) {
        m_sessionNames.add(sessionName);
    }

    public void setSessionNames(Vector sessionNames) {
        m_sessionNames = sessionNames;
    }

    public Vector getSessionNames() {
        return m_sessionNames;
    }
}