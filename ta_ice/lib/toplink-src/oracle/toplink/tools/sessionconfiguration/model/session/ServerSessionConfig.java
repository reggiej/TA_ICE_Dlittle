// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.session;

import oracle.toplink.tools.sessionconfiguration.model.pool.*;

/**
 * INTERNAL:
 */
public class ServerSessionConfig extends DatabaseSessionConfig {
    private PoolsConfig m_poolsConfig;
    private ConnectionPolicyConfig m_connectionPolicyConfig;

    public ServerSessionConfig() {
        super();
    }

    public void setPoolsConfig(PoolsConfig poolsConfig) {
        m_poolsConfig = poolsConfig;
    }

    public PoolsConfig getPoolsConfig() {
        return m_poolsConfig;
    }

    public void setConnectionPolicyConfig(ConnectionPolicyConfig connectionPolicyConfig) {
        m_connectionPolicyConfig = connectionPolicyConfig;
    }

    public ConnectionPolicyConfig getConnectionPolicyConfig() {
        return m_connectionPolicyConfig;
    }
}