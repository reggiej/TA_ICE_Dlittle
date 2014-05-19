// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.platform;


/**
 * INTERNAL:
 */
public abstract class ServerPlatformConfig {
    private boolean m_enableRuntimeServices;
    private boolean m_enableJTA;
    private String m_serverClassName;
    protected boolean isSupported;

    public ServerPlatformConfig() {
        isSupported = true;
    }

    public ServerPlatformConfig(String serverClassName) {
        this();
        m_serverClassName = serverClassName;
    }

    public boolean getEnableJTA() {
        return m_enableJTA;
    }

    public boolean getEnableRuntimeServices() {
        return m_enableRuntimeServices;
    }

    public String getServerClassName() {
        return m_serverClassName;
    }

    public void setEnableRuntimeServices(boolean enableRuntimeServices) {
        m_enableRuntimeServices = enableRuntimeServices;
    }

    public void setEnableJTA(boolean enableJTA) {
        m_enableJTA = enableJTA;
    }
    
    public boolean isSupported() {
        return isSupported;
    }
}