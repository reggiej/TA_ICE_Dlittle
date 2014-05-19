// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.pool;

import oracle.toplink.tools.sessionconfiguration.model.login.LoginConfig;

/**
 * INTERNAL:
 */
public class ConnectionPoolConfig {
    protected String m_name;
    private Integer m_maxConnections;
    private Integer m_minConnections;
    private LoginConfig m_loginConfig;

    public ConnectionPoolConfig() {
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setMaxConnections(Integer maxConnections) {
        m_maxConnections = maxConnections;
    }

    public Integer getMaxConnections() {
        return m_maxConnections;
    }

    public void setMinConnections(Integer minConnections) {
        m_minConnections = minConnections;
    }

    public Integer getMinConnections() {
        return m_minConnections;
    }

    public void setLoginConfig(LoginConfig loginConfig) {
        m_loginConfig = loginConfig;
    }

    public LoginConfig getLoginConfig() {
        return m_loginConfig;
    }
}