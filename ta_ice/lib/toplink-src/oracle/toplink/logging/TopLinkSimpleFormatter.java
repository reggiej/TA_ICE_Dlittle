// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.logging;

import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.text.*;
import java.util.Date;
import oracle.toplink.internal.security.*;

/**
 * <p>
 * Print a brief summary of a TopLink LogRecord in a human readable
 * format.  The summary will typically be 1 or 2 lines.
 * </p>
 */
public class TopLinkSimpleFormatter extends SimpleFormatter {
    Date dat = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;
    private Object[] args = new Object[1];

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = (String)PrivilegedAccessHelper.getLineSeparator();

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record0) {
        if (!(record0 instanceof TopLinkLogRecord)) {
            return super.format(record0);
        } else {
            TopLinkLogRecord record = (TopLinkLogRecord)record0;

            StringBuffer sb = new StringBuffer();

            if (record.shouldPrintDate()) {
                // Minimize memory allocations here.
                dat.setTime(record.getMillis());
                args[0] = dat;
                StringBuffer text = new StringBuffer();
                if (formatter == null) {
                    formatter = new MessageFormat(format);
                }
                formatter.format(args, text, null);
                sb.append(text);
                sb.append(" ");
            }            
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());
            } else {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null) {
                sb.append(" ");
                sb.append(record.getSourceMethodName());
            }
            if (record.getSessionString() != null) {
                sb.append(" ");
                sb.append(record.getSessionString());
            }
            if (record.getConnection() != null) {
                sb.append(" ");
                sb.append(AbstractSessionLog.CONNECTION_STRING + "(" + String.valueOf(System.identityHashCode(record.getConnection())) + ")");
            }
            if (record.shouldPrintThread()) {
                sb.append(" ");
                sb.append(AbstractSessionLog.THREAD_STRING + "(" + String.valueOf(record.getThreadID()) + ")");
            }
            sb.append(lineSeparator);
            String message = formatMessage(record);
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
            sb.append(message);
            sb.append(lineSeparator);
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    if (record.getLevel().intValue() == Level.SEVERE.intValue()) {
                        record.getThrown().printStackTrace(pw);
                    } else if (record.getLevel().intValue() <= Level.WARNING.intValue()) {
                        if (record.shouldLogExceptionStackTrace()) {
                            record.getThrown().printStackTrace(pw);
                        } else {
                            pw.write(record.getThrown().toString());
                            pw.write(lineSeparator);
                        }
                    }
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            }
            return sb.toString();
        }
    }
}
