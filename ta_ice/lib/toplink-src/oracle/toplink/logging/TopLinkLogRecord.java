// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.logging;

import java.util.logging.LogRecord;
import java.util.logging.Level;
import oracle.toplink.internal.databaseaccess.Accessor;

/**
 * INTERNAL:
 * <p>
 * Used to include TopLink's own logging properties that will be formatted by a TopLink Formatter
 * </p>
 */
public class TopLinkLogRecord extends LogRecord {
    private String sessionString;
    private Accessor connection;
    private boolean shouldLogExceptionStackTrace;
    private boolean shouldPrintDate;
    private boolean shouldPrintThread;

    public TopLinkLogRecord(Level level, String msg) {
        super(level, msg);
    }

    public String getSessionString() {
        return sessionString;
    }

    public void setSessionString(String sessionString) {
        this.sessionString = sessionString;
    }

    public Accessor getConnection() {
        return connection;
    }

    public void setConnection(Accessor connection) {
        this.connection = connection;
    }

    public boolean shouldLogExceptionStackTrace() {
        return shouldLogExceptionStackTrace;
    }

    public void setShouldLogExceptionStackTrace(boolean shouldLogExceptionStackTrace) {
        this.shouldLogExceptionStackTrace = shouldLogExceptionStackTrace;
    }

    public boolean shouldPrintDate() {
        return shouldPrintDate;
    }
    
    public void setShouldPrintDate(boolean shouldPrintDate) {
        this.shouldPrintDate = shouldPrintDate;
    }

    public boolean shouldPrintThread() {
        return shouldPrintThread;
    }
    
    public void setShouldPrintThread(boolean shouldPrintThread) {
        this.shouldPrintThread = shouldPrintThread;
    }
}