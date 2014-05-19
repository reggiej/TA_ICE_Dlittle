// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.rcm.command;


/**
 * INTERNAL:
 */
public class CommandsConfig {
    private boolean m_cacheSync;

    public CommandsConfig() {
        m_cacheSync = false;
    }

    public void setCacheSync(boolean cacheSync) {
        m_cacheSync = cacheSync;
    }

    public boolean getCacheSync() {
        return m_cacheSync;
    }
}