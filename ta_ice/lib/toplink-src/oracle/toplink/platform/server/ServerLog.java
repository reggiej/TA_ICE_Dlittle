// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server;

import java.io.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.logging.*;

/**
 * <p>
 * Basic logging class that provides framework for integration with the application
 * server log. This class is used when messages need to be logged through an application
 * server, e.g. OC4J.
 *
 *  @see SessionLog
 *  @see AbstractSessionLog
 *  @see SessionLogEntry
 *  @see Session
 * </p>
 */
public class ServerLog extends AbstractSessionLog {

    /**
     * PUBLIC:
     * <p>
     * Create and return a new ServerLog.
     * </p>
     */
    public ServerLog() {
        super();
    }

    /**
     * PUBLIC:
     * <p>
     * Log a SessionLogEntry
     * </p><p>
     *
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * </p>
     */
    public void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel())) {
            return;
        }

        String message = getSupplementDetailString(entry);

        if (entry.hasException()) {
            message += entry.getException();
        } else {
            message += formatMessage(entry);
        }

        basicLog(entry.getLevel(), message);
    }

    /**
     * <p>
     * Log message to a writer by default.  It needs to be overridden by the subclasses.
     * </p><p>
     *
     * @param level the log request level
     * </p><p>
     * @param message the formatted string message
     * </p>
     */
    protected void basicLog(int level, String message) {
        try {
            printPrefixString(level);
            getWriter().write(message);
            getWriter().write(Helper.cr());
            getWriter().flush();
        } catch (IOException exception) {
            throw ValidationException.logIOError(exception);
        }
    }
}