// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.log;

/**
 *  INTERNAL:
 */
public class LoggingOptionsConfig  {

    private Boolean m_logExceptionStacktrace;
    private Boolean m_printDate;
    private Boolean m_printSession;
    private Boolean m_printThread;
    private Boolean m_printConnection;

    public LoggingOptionsConfig() {
    }

    public void setShouldLogExceptionStackTrace(Boolean shouldLogExceptionStackTrace) {
        m_logExceptionStacktrace = shouldLogExceptionStackTrace;
    }

    public Boolean getShouldLogExceptionStackTrace() {
        return m_logExceptionStacktrace;
    }
    
    public void setShouldPrintDate(Boolean shouldPrintDate) {
        m_printDate = shouldPrintDate;
    }

    public Boolean getShouldPrintDate() {
        return m_printDate;
    }

    public void setShouldPrintSession(Boolean shouldPrintSession) {
        m_printSession = shouldPrintSession;
    }

    public Boolean getShouldPrintSession() {
        return m_printSession;
    }

    public void setShouldPrintThread(Boolean shouldPrintThread) {
        m_printThread = shouldPrintThread;
    }

    public Boolean getShouldPrintThread() {
        return m_printThread;
    }

    public void setShouldPrintConnection(Boolean shouldPrintConnection) {
        m_printConnection = shouldPrintConnection;
    }

    public Boolean getShouldPrintConnection() {
        return m_printConnection;
    }
}