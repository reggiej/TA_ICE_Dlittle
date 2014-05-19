// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.io.Writer;

/**
 * SessionLog is the ever-so-simple interface used by
 * TopLink to log generated messages and SQL. An implementor of
 * this interface can be passed to the TopLink session
 * (via the #setSessionLog(SessionLog) method); and
 * all logging data will be passed through to the implementor
 * via an instance of SessionLogEntry. This can be used
 * to supplement debugging; or the entries could be stored
 * in a database instead of logged to System.out, etc.
 * <p>
 * This class defines Toplink logging levels (that are used throughout Toplink code) with the following integer values:
 * <table>
 * <tr><td>&nbsp;</td><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
 * <tr><td>&nbsp;</td><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
 * <tr><td>&nbsp;</td><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
 * <tr><td>&nbsp;</td><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
 * <tr><td>&nbsp;</td><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
 * <tr><td>&nbsp;</td><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
 * <tr><td>&nbsp;</td><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
 * <tr><td>&nbsp;</td><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
 * <tr><td>&nbsp;</td><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
 * </table>
 * <p>
 * In addition, TopLink categories used for logging name space are defined with the following String values:
 * <table>
 * <tr><td>&nbsp;</td><td>SQL</td>           <td>&nbsp;</td><td>= "sql"</td>
 * <tr><td>&nbsp;</td><td>TRANSACTION</td>   <td>&nbsp;</td><td>= "transaction"</td>
 * <tr><td>&nbsp;</td><td>EVENT</td>         <td>&nbsp;</td><td>= "event"</td>
 * <tr><td>&nbsp;</td><td>QUERY</td>         <td>&nbsp;</td><td>= "query"</td>
 * <tr><td>&nbsp;</td><td>CACHE</td>         <td>&nbsp;</td><td>= "cache"</td>
 * <tr><td>&nbsp;</td><td>PROPAGATION</td>   <td>&nbsp;</td><td>= "propagation"</td>
 * <tr><td>&nbsp;</td><td>SEQUENCING</td>    <td>&nbsp;</td><td>= "sequencing"</td>
 * <tr><td>&nbsp;</td><td>EJB</td>           <td>&nbsp;</td><td>= "ejb"</td>
 * <tr><td>&nbsp;</td><td>DMS</td>           <td>&nbsp;</td><td>= "dms"</td>
 * <tr><td>&nbsp;</td><td>EJB_ANNOTATION</td><td>&nbsp;</td><td>= "ejb_annotation"</td>
 * <tr><td>&nbsp;</td><td>WEAVER</td>        <td>&nbsp;</td><td>= "weaver"</td>
 * </table>
 * @see oracle.toplink.logging.AbstractSessionLog
 * @see SessionLogEntry
 * @see Session
 *
 * @since TOPLink/Java 3.0
 * @deprecated Please use oracle.toplink.logging.SessionLog
 */
public interface SessionLog {
    //TopLink log levels.  They are mapped to java.util.logging.Level values
    public static final int OFF = 8;

    //TL is not in a state to continue
    public static final int SEVERE = 7;

    //Exceptions that don't force a stop
    public static final int WARNING = 6;

    //Login and logout per server session with name
    public static final int INFO = 5;

    //Configuration info
    public static final int CONFIG = 4;

    //SQL
    public static final int FINE = 3;

    //Previously logged under logMessage and stack trace of exceptions at WARNING level
    public static final int FINER = 2;

    //Previously logged under logDebug
    public static final int FINEST = 1;
    public static final int ALL = 0;

    //TopLink categories used for logging name space.
    public static final String SQL = "sql";
    public static final String TRANSACTION = "transaction";
    public static final String EVENT = "event";
    public static final String CONNECTION = "connection";
    public static final String QUERY = "query";
    public static final String CACHE = "cache";
    public static final String PROPAGATION = "propagation";
    public static final String SEQUENCING = "sequencing";
    public static final String EJB = "ejb";
    public static final String DMS = "dms";
    public static final String EJB_OR_METADATA = "ejb_or_metadata";
    public static final String WEAVER = "weaver";
    public static final String PROPERTIES = "properties";
    public static final String SERVER = "server";
    public final String[] loggerCatagories = new String[] { SQL ,TRANSACTION ,EVENT ,CONNECTION ,QUERY ,CACHE ,PROPAGATION ,SEQUENCING ,EJB ,DMS ,EJB_OR_METADATA ,WEAVER ,PROPERTIES ,SERVER};

    /**
     * PUBLIC:
     * TopLink will call this method whenever something
     * needs to be logged (messages, SQL, etc.).
     * All the pertinent information will be contained in
     * the specified entry.
     *
     * @param entry oracle.toplink.sessions.LogEntry
     */
    public void log(oracle.toplink.logging.SessionLogEntry entry);

    /**
     * OBSOLETE:
     * @deprecated Replaced by log(oracle.toplink.logging.SessionLogEntry)
     */
    public void log(SessionLogEntry entry);

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public boolean shouldLogDebug();

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public boolean shouldLogExceptions();

    /**
     * By default the stack trace is logged for SEVERE all the time and at FINER level for WARNING or less,
     * this can be turned off.
     */
    public boolean shouldLogExceptionStackTrace();

    /**
     * By default the date is always printed, this can be turned off.
     */
    public boolean shouldPrintDate();

    /**
     * By default the thread is logged at FINE or less level, this can be turned off.
     */
    public boolean shouldPrintThread();

    /**
     * By default the connection is always printed whenever available, this can be turned off.
     */
    public boolean shouldPrintConnection();

    /**
     * By default the Session is always printed whenever available, this can be turned off.
     */
    public boolean shouldPrintSession();

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void setShouldLogDebug(boolean flag);

    /**
     * OBSOLETE:
     * @deprecated replaced by level
     */
    public void setShouldLogExceptions(boolean flag);

    /**
     * By default stack trace is logged for SEVERE all the time and at FINER level for WARNING or less.
     * This can be turned off.
     */
    public void setShouldLogExceptionStackTrace(boolean flag);

    /**
     * By default date is printed, this can be turned off.
     */
    public void setShouldPrintDate(boolean flag);

    /**
     * By default the thread is logged at FINE or less level, this can be turned off.
     */
    public void setShouldPrintThread(boolean flag);

    /**
     * By default the connection is always printed whenever available, this can be turned off.
     */
    public void setShouldPrintConnection(boolean flag);

    /**
     * By default the Session is always printed whenever available, this can be turned off.
     */
    public void setShouldPrintSession(boolean flag);

    /**
     * PUBLIC:
     * Return the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference usually defaults to a writer on System.out.
     * To enable logging, logMessages must be turned on in the session.
     */
    public Writer getWriter();

    /**
     * PUBLIC:
     * Set the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference usually defaults to a writer on System.out.
     * To enable logging, logMessages() is used on the session.
     */
    public void setWriter(Writer log);

    /**
     * PUBLIC:
     * Return the log level.  Used when session is not available.
     * <p>
     * The Toplink logging levels returned correspond to:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public int getLevel();

    /**
     * PUBLIC:
     * <p>
     * Return the log level as a string value.
     */
    public String getLevelString();
    
    /**
     * PUBLIC:
     * Return the log level; category is only needed where name space
     * is available.
     * <p>
     * The Toplink logging levels returned correspond to:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     * <p>
     * The Toplink categories for logging name space are: 
     * <table>
     * <tr><td>SQL</td>              <td>&nbsp;</td><td>= "sql"</td>
     * <tr><td>TRANSACTION</td>   <td>&nbsp;</td><td>= "transaction"</td>
     * <tr><td>EVENT</td>          <td>&nbsp;</td><td>= "event"</td>
     * <tr><td>QUERY</td>         <td>&nbsp;</td><td>= "query"</td>
     * <tr><td>CACHE</td>         <td>&nbsp;</td><td>= "cache"</td>
     * <tr><td>PROPAGATION</td>   <td>&nbsp;</td><td>= "propagation"</td>
     * <tr><td>SEQUENCING</td>    <td>&nbsp;</td><td>= "sequencing"</td>
     * <tr><td>EJB</td>           <td>&nbsp;</td><td>= "ejb"</td>
     * <tr><td>DMS</td>           <td>&nbsp;</td><td>= "dms"</td>
     * <tr><td>EJB_ANNOTATION</td><td>&nbsp;</td><td>= "ejb_annotation"</td>
     * <tr><td>WEAVER</td>        <td>&nbsp;</td><td>= "weaver"</td>
     * <tr><td>PROPERTIES</td>        <td>&nbsp;</td><td>= "properties"</td>
     * <tr><td>SERVER</td>        <td>&nbsp;</td><td>= "server"</td>
     * </table>
     */
    public int getLevel(String category);

    /**
     * PUBLIC:
     * Set the log level.  Used when session is not available.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void setLevel(int level);

    /**
     * PUBLIC:
     * Set the log level.  Category is only needed where name space
     * is available.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     * <p>
     * The Toplink categories for logging name space are: 
     * <table>
     * <tr><td>SQL</td>              <td>&nbsp;</td><td>= "sql"</td>
     * <tr><td>TRANSACTION</td>   <td>&nbsp;</td><td>= "transaction"</td>
     * <tr><td>EVENT</td>          <td>&nbsp;</td><td>= "event"</td>
     * <tr><td>QUERY</td>         <td>&nbsp;</td><td>= "query"</td>
     * <tr><td>CACHE</td>         <td>&nbsp;</td><td>= "cache"</td>
     * <tr><td>PROPAGATION</td>   <td>&nbsp;</td><td>= "propagation"</td>
     * <tr><td>SEQUENCING</td>    <td>&nbsp;</td><td>= "sequencing"</td>
     * <tr><td>EJB</td>           <td>&nbsp;</td><td>= "ejb"</td>
     * <tr><td>DMS</td>           <td>&nbsp;</td><td>= "dms"</td>
     * <tr><td>EJB_ANNOTATION</td><td>&nbsp;</td><td>= "ejb_annotation"</td>
     * <tr><td>WEAVER</td>        <td>&nbsp;</td><td>= "weaver"</td>
     * </table>
     */
    public void setLevel(int level, String category);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     * Used when session is not available.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public boolean shouldLog(int level);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     * Category is only needed where name space is available.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     * <p>
     * The Toplink categories for logging name space are: 
     * <table>
     * <tr><td>SQL</td>              <td>&nbsp;</td><td>= "sql"</td>
     * <tr><td>TRANSACTION</td>   <td>&nbsp;</td><td>= "transaction"</td>
     * <tr><td>EVENT</td>          <td>&nbsp;</td><td>= "event"</td>
     * <tr><td>QUERY</td>         <td>&nbsp;</td><td>= "query"</td>
     * <tr><td>CACHE</td>         <td>&nbsp;</td><td>= "cache"</td>
     * <tr><td>PROPAGATION</td>   <td>&nbsp;</td><td>= "propagation"</td>
     * <tr><td>SEQUENCING</td>    <td>&nbsp;</td><td>= "sequencing"</td>
     * <tr><td>EJB</td>           <td>&nbsp;</td><td>= "ejb"</td>
     * <tr><td>DMS</td>           <td>&nbsp;</td><td>= "dms"</td>
     * <tr><td>EJB_ANNOTATION</td><td>&nbsp;</td><td>= "ejb_annotation"</td>
     * <tr><td>WEAVER</td>        <td>&nbsp;</td><td>= "weaver"</td>
     * </table>
     */
    public boolean shouldLog(int level, String category);

    /**
     * PUBLIC:
     * Log a message that does not need to be translated.  This method is intended for 
     * external use when logging messages are wanted within the TopLink output.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message);

    /**
     * PUBLIC:
     * Log a message with one parameter that needs to be translated.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message, Object param);

    /**
     * PUBLIC:
     * Log a message with two parameters that needs to be translated.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message, Object param1, Object param2);

    /**
     * PUBLIC:
     * Log a message with three parameters that needs to be translated.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message, Object param1, Object param2, Object param3);

    /**
     * PUBLIC:
     * This method is called when the log request is from somewhere session is not available.
     * The message needs to be translated.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message, Object[] arguments);

    /**
     * PUBLIC:
     * This method is called when the log request is from somewhere session is not available.
     * shouldTranslate flag determines if the message needs to be translated.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void log(int level, String message, Object[] arguments, boolean shouldTranslate);

    /**
     * PUBLIC:
     * This method is called when a throwable at finer level needs to be logged.
     */
    public void throwing(Throwable throwable);

    /**
     * PUBLIC:
     * This method is called when a severe level message needs to be logged.
     * The message will be translated
     */
    public void severe(String message);

    /**
     * PUBLIC:
     * This method is called when a warning level message needs to be logged.
     * The message will be translated
     */
    public void warning(String message);

    /**
     * PUBLIC:
     * This method is called when a info level message needs to be logged.
     * The message will be translated
     */
    public void info(String message);

    /**
     * PUBLIC:
     * This method is called when a config level message needs to be logged.
     * The message will be translated
     */
    public void config(String message);

    /**
     * PUBLIC:
     * This method is called when a fine level message needs to be logged.
     * The message will be translated
     */
    public void fine(String message);

    /**
     * PUBLIC:
     * This method is called when a finer level message needs to be logged.
     * The message will be translated
     */
    public void finer(String message);

    /**
     * PUBLIC:
     * This method is called when a finest level message needs to be logged.
     * The message will be translated
     */
    public void finest(String message);

    /**
     * PUBLIC:
     * Log a throwable with level.
     * <p>
     * The Toplink logging levels available are:
     * <table>
     * <tr><td>ALL</td>    <td>&nbsp;</td><td>= 0</td>
     * <tr><td>FINEST</td> <td>&nbsp;</td><td>= 1</td>
     * <tr><td>FINER</td>  <td>&nbsp;</td><td>= 2</td>
     * <tr><td>FINE</td>   <td>&nbsp;</td><td>= 3</td>
     * <tr><td>CONFIG</td> <td>&nbsp;</td><td>= 4</td>
     * <tr><td>INFO</td>   <td>&nbsp;</td><td>= 5</td>
     * <tr><td>WARNING</td><td>&nbsp;</td><td>= 6</td>
     * <tr><td>SEVERE</td> <td>&nbsp;</td><td>= 7</td>
     * <tr><td>OFF</td>    <td>&nbsp;</td><td>= 8</td>
     * </table>
     */
    public void logThrowable(int level, Throwable throwable);

    /**
     * PUBLIC:
     * Get the session that owns this SessionLog.
     */
    public Session getSession();

    /**
     * PUBLIC:
     * Set the session that owns this SessionLog.
     */
    public void setSession(Session session);
}
