// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.clustering;

/**
 * INTERNAL: 
 */
public class JNDIClusteringServiceConfig extends ClusteringServiceConfig {
    private char[] m_jndiPassword;
    private String m_jndiUsername;
    private String m_namingServiceInitialContextFactoryName;

    protected JNDIClusteringServiceConfig() {
        super();
    }

    public String getJNDIPassword() {
        // Bug 4117441 - Secure programming practices, store password in char[]
        if (m_jndiPassword != null) {
            return new String(m_jndiPassword);
        } else {
            return null;
        }
    }

    public String getJNDIUsername() {
        return m_jndiUsername;
    }

    public String getNamingServiceInitialContextFactoryName() {
        return m_namingServiceInitialContextFactoryName;
    }

    public void setJNDIPassword(String password) {
        // Bug 4117441 - Secure programming practices, store password in char[]
        if (password != null) {
            m_jndiPassword = password.toCharArray();
        } else {
            // must respect dereferencing of the password
            m_jndiPassword = null;
        }
    }

    public void setJNDIUsername(String username) {
        m_jndiUsername = username;
    }

    public void setNamingServiceInitialContextFactoryName(String namingServiceInitialContextFactoryName) {
        m_namingServiceInitialContextFactoryName = namingServiceInitialContextFactoryName;
    }
}