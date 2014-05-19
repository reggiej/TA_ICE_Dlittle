// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.log;


/**
 * INTERNAL:
 */
public abstract class LogConfig {
    private LoggingOptionsConfig m_loggingOptions;

    public LogConfig() {
    }

    public void setLoggingOptions(LoggingOptionsConfig loggingOptions) {
        m_loggingOptions = loggingOptions;
    }

    public LoggingOptionsConfig getLoggingOptions() {
        return m_loggingOptions;
    }
}