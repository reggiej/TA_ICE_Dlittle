// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.event;

import java.util.Vector;

/**
 * INTERNAL:
 */
public class SessionEventManagerConfig {
    private Vector m_sessionEventListeners;

    public SessionEventManagerConfig() {
        m_sessionEventListeners = new Vector();
    }

    public void addSessionEventListener(String listener) {
        m_sessionEventListeners.add(listener);
    }

    public void setSessionEventListeners(Vector sessionEventListeners) {
        m_sessionEventListeners = sessionEventListeners;
    }

    public Vector getSessionEventListeners() {
        return m_sessionEventListeners;
    }
}