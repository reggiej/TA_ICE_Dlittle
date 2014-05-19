// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport.discovery;


/**
 * INTERNAL:
 */
public class DiscoveryConfig {
    private String m_multicastGroupAddress;
    private int m_multicastPort;
    private int m_announcementDelay;
    private int m_packetTimeToLive;

    public DiscoveryConfig() {
    }

    public void setMulticastGroupAddress(String multicastGroupAddress) {
        m_multicastGroupAddress = multicastGroupAddress;
    }

    public String getMulticastGroupAddress() {
        return m_multicastGroupAddress;
    }

    public void setMulticastPort(int multicastPort) {
        m_multicastPort = multicastPort;
    }

    public int getMulticastPort() {
        return m_multicastPort;
    }

    public void setAnnouncementDelay(int announcementDelay) {
        m_announcementDelay = announcementDelay;
    }

    public int getAnnouncementDelay() {
        return m_announcementDelay;
    }

    public int getPacketTimeToLive() {
        return m_packetTimeToLive;
    }

    public void setPacketTimeToLive(int packetTimeToLive) {
        m_packetTimeToLive = packetTimeToLive;
    }
}