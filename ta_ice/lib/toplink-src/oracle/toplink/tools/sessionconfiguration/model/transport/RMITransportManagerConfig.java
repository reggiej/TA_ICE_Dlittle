// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport;

import oracle.toplink.tools.sessionconfiguration.model.transport.naming.*;
import oracle.toplink.tools.sessionconfiguration.model.transport.discovery.*;

/**
 * INTERNAL:
 */
public class RMITransportManagerConfig extends TransportManagerConfig {
    private String m_sendMode;
    private DiscoveryConfig m_discoveryConfig;
    private JNDINamingServiceConfig m_jndiNamingServiceConfig;
    private RMIRegistryNamingServiceConfig m_rmiRegistryNamingServiceConfig;

    public RMITransportManagerConfig() {
        super();
    }

    public void setSendMode(String sendMode) {
        m_sendMode = sendMode;
    }

    public String getSendMode() {
        return m_sendMode;
    }

    public void setDiscoveryConfig(DiscoveryConfig discoveryConfig) {
        m_discoveryConfig = discoveryConfig;
    }

    public DiscoveryConfig getDiscoveryConfig() {
        return m_discoveryConfig;
    }

    public void setJNDINamingServiceConfig(JNDINamingServiceConfig jndiNamingServiceConfig) {
        m_jndiNamingServiceConfig = jndiNamingServiceConfig;
    }

    public JNDINamingServiceConfig getJNDINamingServiceConfig() {
        return m_jndiNamingServiceConfig;
    }

    public void setRMIRegistryNamingServiceConfig(RMIRegistryNamingServiceConfig rmiRegistryNamingServiceConfig) {
        m_rmiRegistryNamingServiceConfig = rmiRegistryNamingServiceConfig;
    }

    public RMIRegistryNamingServiceConfig getRMIRegistryNamingServiceConfig() {
        return m_rmiRegistryNamingServiceConfig;
    }
}