// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport.naming;


/**
 * INTERNAL:
 */
public class RMIRegistryNamingServiceConfig {
    private String m_url;

    public RMIRegistryNamingServiceConfig() {
    }

    public void setURL(String url) {
        m_url = url;
    }

    public String getURL() {
        return m_url;
    }
}