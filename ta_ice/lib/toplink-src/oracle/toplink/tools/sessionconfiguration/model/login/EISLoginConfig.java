// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.login;


/**
 * INTERNAL:
 */
public class EISLoginConfig extends LoginConfig {
    private String m_connectionSpecClass;
    private String m_connectionFactoryURL;

    public EISLoginConfig() {
        super();
    }

    public void setConnectionSpecClass(String connectionSpecClass) {
        m_connectionSpecClass = connectionSpecClass;
    }

    public String getConnectionSpecClass() {
        return m_connectionSpecClass;
    }

    public void setConnectionFactoryURL(String connectionFactoryURL) {
        m_connectionFactoryURL = connectionFactoryURL;
    }

    public String getConnectionFactoryURL() {
        return m_connectionFactoryURL;
    }
}