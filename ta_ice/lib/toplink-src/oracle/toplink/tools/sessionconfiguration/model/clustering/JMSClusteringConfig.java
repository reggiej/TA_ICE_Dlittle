// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.clustering;


/**
 * INTERNAL:
 */
public class JMSClusteringConfig extends JNDIClusteringServiceConfig {
    private String m_jmsTopicConnectionFactoryName;
    private String m_jmsTopicName;

    public JMSClusteringConfig() {
        super();
    }

    public void setJMSTopicConnectionFactoryName(String jmsTopicConnectionFactoryName) {
        m_jmsTopicConnectionFactoryName = jmsTopicConnectionFactoryName;
    }

    public String getJMSTopicConnectionFactoryName() {
        return m_jmsTopicConnectionFactoryName;
    }

    public void setJMSTopicName(String jmsTopicName) {
        m_jmsTopicName = jmsTopicName;
    }

    public String getJMSTopicName() {
        return m_jmsTopicName;
    }
}