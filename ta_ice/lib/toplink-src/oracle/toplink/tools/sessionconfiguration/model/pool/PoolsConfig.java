// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.pool;

import java.util.Vector;

/**
 * INTERNAL:
 */
public class PoolsConfig {
    private ReadConnectionPoolConfig m_readConnectionPoolConfig;
    private WriteConnectionPoolConfig m_writeConnectionPoolConfig;
    private ConnectionPoolConfig m_sequenceConnectionPoolConfig;
    private Vector m_connectionPoolConfigs;

    public PoolsConfig() {
        m_connectionPoolConfigs = new Vector();
    }

    public void setReadConnectionPoolConfig(ReadConnectionPoolConfig readConnectionPoolConfig) {
        m_readConnectionPoolConfig = readConnectionPoolConfig;
    }

    public ReadConnectionPoolConfig getReadConnectionPoolConfig() {
        return m_readConnectionPoolConfig;
    }

    public void setWriteConnectionPoolConfig(WriteConnectionPoolConfig writeConnectionPoolConfig) {
        m_writeConnectionPoolConfig = writeConnectionPoolConfig;
    }

    public WriteConnectionPoolConfig getWriteConnectionPoolConfig() {
        return m_writeConnectionPoolConfig;
    }

    public void setSequenceConnectionPoolConfig(ConnectionPoolConfig sequenceConnectionPoolConfig) {
        m_sequenceConnectionPoolConfig = sequenceConnectionPoolConfig;
    }

    public ConnectionPoolConfig getSequenceConnectionPoolConfig() {
        return m_sequenceConnectionPoolConfig;
    }

    public void addConnectionPoolConfig(ConnectionPoolConfig connectionPoolConfig) {
        m_connectionPoolConfigs.add(connectionPoolConfig);
    }

    public void setConnectionPoolConfigs(Vector connectionPoolConfigs) {
        m_connectionPoolConfigs = connectionPoolConfigs;
    }

    public Vector getConnectionPoolConfigs() {
        return m_connectionPoolConfigs;
    }
}