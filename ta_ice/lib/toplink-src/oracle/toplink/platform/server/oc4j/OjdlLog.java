// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.platform.server.oc4j;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.toplink.logging.JavaLog;
import oracle.toplink.logging.SessionLogEntry;

//import oracle.core.ojdl.logging.ODLLogger;
//import oracle.dms.context.ExecutionContext;
//import oracle.toplink.exceptions.TopLinkException;

/**
 * INTERNAL:
 *     08/15/2008- 11.1.1   Michael OBrien 
 *       7278787 : Remove OC4J11 specific functionality specific to ODLLogger (ojdl.jar)
 *
 */
public class OjdlLog extends JavaLog {

    /**
     * INTERNAL:
     * Add Logger to the catagoryloggers. 
     */
    protected void addLogger(String loggerCatagory, String loggerNameSpace) {
        //getCategoryLoggers().put(loggerCatagory, ODLLogger.getLogger(loggerNameSpace));
        getCategoryLoggers().put(loggerCatagory, Logger.getLogger(loggerNameSpace));
    }

    /**
     * INTERNAL:
     * <p>
     * Internally log a message
     * </p><p>
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * @param javaLevel the message level
     * @param logger the Logger for the message
     * </p>
     */
    protected void internalLog(SessionLogEntry entry, Level javaLevel, Logger logger) {
/*        if (entry.getSession() != null) {
            ExecutionContext.get().setGlobalValue("TopLinkSessionType", entry.getSession().getSessionTypeString());
            ExecutionContext.get().setGlobalValue("TopLinkSessionHashcode", String.valueOf(System.identityHashCode(entry.getSession())));
        }
        if (entry.getConnection() != null) {
            ExecutionContext.get().setGlobalValue("TopLinkConnectionHashcode", String.valueOf(System.identityHashCode(entry.getConnection())));
        }
*/        
        if (entry.hasException()) {
            //added check.  
//            if (logger instanceof ODLLogger) {
//                ((ODLLogger)logger).log(javaLevel, getMessageId(entry.getException()), null, null, null, entry.getException());
//            } else {
                logger.log(javaLevel, null, entry.getException());
//            }
            return;
        }

        if (entry.shouldTranslate()) {
            String bundleName;
            if (entry.getLevel() > FINE) {
                bundleName = LOGGING_LOCALIZATION_STRING;
            } else {
                bundleName = TRACE_LOCALIZATION_STRING;
            }
            logger.logrb(javaLevel, null, null, bundleName, entry.getMessage(), entry.getParameters());
        } else {
            logger.log(javaLevel, entry.getMessage(), entry.getParameters());
        }
    }
    
    //Create message id for exceptions based on the error code and leading zeros, e.g. "TOP-04002"
/*    private String getMessageId(Throwable th) {
        if (th instanceof TopLinkException) {
            String zeros = "0000";
            String numbString = Integer.toString(((TopLinkException)th).getErrorCode());
            // Add leading zeros
            numbString = zeros.substring(0, (5 - numbString.length())) + numbString;
            return "TOP-" + numbString;    
        } else {
            return null;
        }
    }*/
}
