// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.pool;

/**
 * INTERNAL:
 */
public class ConnectionPolicyConfig {
    private boolean m_lazy;
    private boolean m_useExclusiveConnection;

    public ConnectionPolicyConfig() {
    }

    public void setLazy(boolean lazy) {
        m_lazy = lazy;
    }

    public boolean getLazy() {
        return m_lazy;
    }

    public void setUseExclusiveConnection(boolean useExclusiveConnection) {
        m_useExclusiveConnection = useExclusiveConnection;
    }

    public boolean getUseExclusiveConnection() {
        return m_useExclusiveConnection;
    }
}