// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.pool;


/**
 * INTERNAL:
 */
public class ReadConnectionPoolConfig extends ConnectionPoolConfig {
    private boolean m_exclusive;

    public ReadConnectionPoolConfig() {
        super();
    }

    public void setExclusive(boolean exclusive) {
        m_exclusive = exclusive;
    }

    public boolean getExclusive() {
        return m_exclusive;
    }
}