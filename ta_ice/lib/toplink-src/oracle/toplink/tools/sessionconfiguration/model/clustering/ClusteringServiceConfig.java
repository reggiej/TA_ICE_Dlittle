// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.clustering;


/**
 * INTERNAL:
 */
public abstract class ClusteringServiceConfig {
    private Integer m_multicastPort;
    private Integer m_packetTimeToLive;
    private String m_multicastGroupAddress;
    private String m_namingServiceURL;

    protected ClusteringServiceConfig() {
    }

    public String getMulticastGroupAddress() {
        return m_multicastGroupAddress;
    }

    public Integer getMulticastPort() {
        return m_multicastPort;
    }

    public String getNamingServiceURL() {
        return m_namingServiceURL;
    }

    public Integer getPacketTimeToLive() {
        return m_packetTimeToLive;
    }

    public void setMulticastGroupAddress(String multicastGroupAddress) {
        m_multicastGroupAddress = multicastGroupAddress;
    }

    public void setMulticastPort(Integer multicastPort) {
        m_multicastPort = multicastPort;
    }

    public void setNamingServiceURL(String namingServiceURL) {
        m_namingServiceURL = namingServiceURL;
    }

    public void setPacketTimeToLive(Integer packetTimeToLive) {
        m_packetTimeToLive = packetTimeToLive;
    }
}