// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.pool;


/**
 * INTERNAL:
 */
public class WriteConnectionPoolConfig extends ConnectionPoolConfig {
    public WriteConnectionPoolConfig() {
        m_name = "default";
    }

    /*
     * INTERNAL:
     * This method will ignore the name passed in. For the write connection
     * pool on a server session, its name must remain 'default'. It must not
     * change.
     */
    public void setName(String name) {
        // ignore it, must remain as 'default'
    }
}