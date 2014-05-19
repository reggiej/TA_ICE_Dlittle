// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport;

/**
 * INTERNAL:
 */
public class Oc4jJGroupsTransportManagerConfig extends TransportManagerConfig {
    private String transportManagerClassName;
    private boolean m_useSingleThreadedNotification;
    private String m_topicName;

    public Oc4jJGroupsTransportManagerConfig() {
        super();
        transportManagerClassName = "oracle.toplink.remotecommand.jgroups.oc4j.Oc4jJGroupsTransportManager";
    }

    public void setUseSingleThreadedNotification(boolean useSingleThreadedNotification) {
        m_useSingleThreadedNotification = useSingleThreadedNotification;
    }

    public boolean useSingleThreadedNotification() {
        return m_useSingleThreadedNotification;
    }
    
    public void setTopicName(String topicName) {
        m_topicName = topicName;
    }

    public String getTopicName() {
        return m_topicName;
    }

    public String getTransportManagerClassName() {
        return transportManagerClassName;
    }
}
