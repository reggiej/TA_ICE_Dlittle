// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.log;


/**
 * INTERNAL:
 */
public class DefaultSessionLogConfig extends LogConfig {
    private String m_logLevel;
    private String m_filename;

    public DefaultSessionLogConfig() {
        super();
    }

    public void setLogLevel(String logLevel) {
        m_logLevel = logLevel;
    }

    public String getLogLevel() {
        return m_logLevel;
    }

    public void setFilename(String filename) {
        m_filename = filename;
    }

    public String getFilename() {
        return m_filename;
    }
}