// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.server.wls;

import weblogic.logging.NonCatalogLogger;
import oracle.toplink.platform.server.ServerLog;

/**
 * Logging class that integrates with the application server log. This class also implements the
 * TopLink SessionLog interface so that it can be set as the session log, thereby causing all
 * TopLink session logging to get redirected through this log sink. Note that extends the
 * DefaultSessionLog log so that .
 * TopLink for Wls logging may be done either through a session or directly through this logger.
 */
public class WlsLog extends ServerLog {
    public static final String TOPLINK_LOG_ID = "TopLink";
    protected NonCatalogLogger logger = null;

    public WlsLog() {
        super();
        logger = new NonCatalogLogger(TOPLINK_LOG_ID);
    }

    // Basic logging routines
    protected void basicLog(int level, String message) {
        if (logger == null) {
            super.basicLog(level, message);
            return;
        }

        if (level == FINE) {
            logger.debug(message);
        } else if (level == INFO) {
            logger.info(message);
        } else if (level == SEVERE) {
            logger.error(message);
        } else if (level == WARNING) {
            logger.warning(message);
        }
    }
}