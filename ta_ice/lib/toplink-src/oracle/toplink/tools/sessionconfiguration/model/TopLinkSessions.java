// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model;

import java.util.Vector;
import java.util.Enumeration;
import oracle.toplink.tools.sessionconfiguration.model.session.SessionConfig;

/**
 * INTERNAL:
 */
public class TopLinkSessions {
    private Vector m_sessionConfigs;
    private String m_version;

    public TopLinkSessions() {
        m_sessionConfigs = new Vector();
    }

    public void addSessionConfig(SessionConfig sessionConfig) {
        m_sessionConfigs.add(sessionConfig);
    }

    public void setSessionConfigs(Vector sessionConfigs) {
        m_sessionConfigs = sessionConfigs;
    }

    public Vector getSessionConfigs() {
        return m_sessionConfigs;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    public String getVersion() {
        return m_version;
    }
}