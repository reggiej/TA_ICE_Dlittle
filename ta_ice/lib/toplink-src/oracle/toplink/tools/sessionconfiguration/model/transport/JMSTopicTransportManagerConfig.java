// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport;

import oracle.toplink.tools.sessionconfiguration.model.transport.naming.*;

/**
 * INTERNAL:
 */
public class JMSTopicTransportManagerConfig extends TransportManagerConfig {
    private String m_topicHostURL;
    private String m_topicConnectionFactoryName;
    private String m_topicName;
    private JNDINamingServiceConfig m_jndiNamingServiceConfig;

    public JMSTopicTransportManagerConfig() {
        super();
    }

    public void setTopicHostURL(String topicHostURL) {
        m_topicHostURL = topicHostURL;
    }

    public String getTopicHostURL() {
        return m_topicHostURL;
    }

    public void setTopicConnectionFactoryName(String topicConnectionFactoryName) {
        m_topicConnectionFactoryName = topicConnectionFactoryName;
    }

    public String getTopicConnectionFactoryName() {
        return m_topicConnectionFactoryName;
    }

    public void setTopicName(String topicName) {
        m_topicName = topicName;
    }

    public String getTopicName() {
        return m_topicName;
    }

    public void setJNDINamingServiceConfig(JNDINamingServiceConfig jndiNamingServiceConfig) {
        m_jndiNamingServiceConfig = jndiNamingServiceConfig;
    }

    public JNDINamingServiceConfig getJNDINamingServiceConfig() {
        return m_jndiNamingServiceConfig;
    }
}