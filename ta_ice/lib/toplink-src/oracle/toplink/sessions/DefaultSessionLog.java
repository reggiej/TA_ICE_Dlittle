// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.io.*;
import oracle.toplink.logging.AbstractSessionLog;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.*;

/**
 * <p><b>Purpose</b>: Default log used for the session when message logging is enabled.
 * The session can log information such as,<ul>
 * <li> all SQL executed
 * <li> informational messages
 * <li> debugging information
 * <li> all exceptions that occur within TopLink
 * </ul>
 * As well information about the message can be logged such as,<ul>
 * <li> the session logging the message
 * <li> the connection executing the SQL
 * <li> the thread in which the log entry occured
 * <li> the exact time (to milliseconds) that the log entry occured
 * <li> the stack trace to the exception
 * </ul>
 * @see SessionLog
 * @see DefaultSessionLog
 * @see Session#logMessage(String)
 *
 * @author Big Country
 * @deprecated Please use oracle.toplink.logging.DefaultSessionLog
 */
public class DefaultSessionLog extends AbstractSessionLog implements Serializable {

    /** The filename associated with this DefaultSessionLog, if it is being written out to a file **/
    protected String fileName;

    /**
     * Represents the Map that stores log levels per the name space strings.
     * The keys are category names. The values are log levels.
     */
    private Map<String, Integer> categoryLogLevelMap = new HashMap();

    /**
     * PUBLIC:
     * Create a new default session log.
     */
    public DefaultSessionLog() {
        super();
        this.level = INFO;
        for (int i = 0; i < loggerCatagories.length; i++) {
            String loggerCategory = loggerCatagories[i]; 
            categoryLogLevelMap.put(loggerCategory, null);
        }
    }

    /**
     * PUBLIC:
     * Create a new default session log for the given writer.
     */
    public DefaultSessionLog(Writer writer) {
        this();
        this.initialize(writer);
    }

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void dontLogDebug() {
        setShouldLogDebug(false);
    }

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void dontLogExceptions() {
        setShouldLogExceptions(false);
    }

    /**
     * OBSOLETE:
     * @deprecated Stack trace is logged at FINER level
     */
    public void dontLogExceptionStackTrace() {
        setShouldLogExceptionStackTrace(false);
    }

    /**
     * OBSOLETE:
     * @deprecated Connection is always printed
     */
    public void dontPrintConnection() {
        this.setShouldPrintConnection(false);
    }

    /**
     * OBSOLETE:
     * @deprecated Date is always printed
     */
    public void dontPrintDate() {
        this.setShouldPrintDate(false);
    }

    /**
     * OBSOLETE:
     * @deprecated Session is always printed
     */
    public void dontPrintSession() {
        this.setShouldPrintSession(false);
    }

    /**
     * OBSOLETE:
     * @deprecated Thread is logged at FINE or less level
     */
    public void dontPrintThread() {
        this.setShouldPrintThread(false);
    }
    
    @Override
    public int getLevel(String category) {
        if(category != null) {
            Integer logLevel = categoryLogLevelMap.get(category);
            // if category-specific log level is not set, use parent level.
            if(logLevel != null) {
                return logLevel.intValue();
            }
        }
        return level;
    }

    @Override
    public void setLevel(int level, String category) {
        if(category == null) {
            this.level = level;
        } else if(categoryLogLevelMap.containsKey(category)) {
            categoryLogLevelMap.put(category, level);
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged by the logger
     * with name space built from the given session and category.
     * Return the shouldLog for the given category from
     * </p><p>
     * @return true if the given message level will be logged
     * </p>
     */
    @Override
    public boolean shouldLog(int level, String category) {
        return (getLevel(category) <= level);
    }

    /**
     * Initialize the log to be backward-compatible with
     * the original TopLink log.
     */
    protected void initialize() {
        this.printSession();
        this.printConnection();
    }

    /**
     * Initialize the log.
     */
    protected void initialize(Writer writer) {
        this.writer = writer;
    }

    /**
     * PUBLIC:
     * Log the entry.
     * This writes the log entries information to a writer such as System.out or a file.
     */
    public void log(oracle.toplink.logging.SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }

        synchronized (this) {
            try {
                printPrefixString(entry.getLevel());
                this.getWriter().write(getSupplementDetailString(entry));
    
                if (entry.hasException()) {
                    if (entry.getLevel() == SEVERE) {
                        entry.getException().printStackTrace(new PrintWriter(getWriter()));
                    } else if (entry.getLevel() <= WARNING) {
                        if (shouldLogExceptionStackTrace()) {
                            entry.getException().printStackTrace(new PrintWriter(getWriter()));
                        } else {
                            writeMessage(entry.getException().toString());
                        }
                    }
                } else {
                    writeMessage(formatMessage(entry));
                }
                getWriter().write(Helper.cr());
                getWriter().flush();
            } catch (IOException exception) {
                throw ValidationException.logIOError(exception);
            }
        }
    }

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void logDebug() {
        setShouldLogDebug(true);
    }

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void logExceptions() {
        setShouldLogExceptions(true);
    }

    /**
     * OBSOLETE:
     * @deprecated Stack trace is logged at FINER level
     */
    public void logExceptionStackTrace() {
        setShouldLogExceptionStackTrace(true);
    }

    /**
     * OBSOLETE:
     * @deprecated Connection is always printed
     */
    public void printConnection() {
        this.setShouldPrintConnection(true);
    }

    /**
     * OBSOLETE:
     * @deprecated Date is always printed
     */
    public void printDate() {
        this.setShouldPrintDate(true);
    }

    /**
     * OBSOLETE:
     * @deprecated Session is always printed
     */
    public void printSession() {
        this.setShouldPrintSession(true);
    }

    /**
     * OBSOLETE:
     * @deprecated Thread is logged at FINE or less level
     */
    public void printThread() {
        this.setShouldPrintThread(true);
    }

    /**
     * PUBLIC:
     * Set the writer that will receive the
     * formatted log entries for a file name.
     */
    public void setWriter(String aFileName) {
        if (aFileName != null) {
            try {
                this.writer = new FileWriter(aFileName);
                this.fileName = aFileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * PUBLIC:
     * For the given writer, return it's associated filename.
     * If associated writer does not have a filename, return null.
     */
    //Added for F2104: Properties.XML  .. gn
    public String getWriterFilename() {
        return fileName;
    }

    /**
     * Append the specified message information to the writer.
     */
    protected void writeMessage(String message) throws IOException {
        this.getWriter().write(message);
    }

    /**
     * Append the separator string to the writer.
     */
    protected void writeSeparator() throws IOException {
        this.getWriter().write("--");
    }
}
