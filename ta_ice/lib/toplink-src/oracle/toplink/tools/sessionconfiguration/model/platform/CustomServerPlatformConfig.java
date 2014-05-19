// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.platform;


/**
 * INTERNAL:
 */
public class CustomServerPlatformConfig extends ServerPlatformConfig {
    private String m_serverClassName;
    private String m_externalTransactionControllerClass;

    public CustomServerPlatformConfig() {
        super();
    }

    public String getServerClassName() {
        return m_serverClassName;
    }

    public String getExternalTransactionControllerClass() {
        return m_externalTransactionControllerClass;
    }

    public void setExternalTransactionControllerClass(String externalTransactionControllerClass) {
        m_externalTransactionControllerClass = externalTransactionControllerClass;
    }

    public void setServerClassName(String serverClassName) {
        m_serverClassName = serverClassName;
    }
}