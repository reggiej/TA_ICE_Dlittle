// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.server;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.platform.server.ServerPlatformBase;

/**
 * INTERNAL:
 *
 * This is the concrete subclass responsible for handling backward compatibility for 9.0.4.
 *
 * This platform overrides:
 *
 * getExternalTransactionControllerClass(): to use a user-specified controller class
 *
 * This platform adds:
 *
 * setExternalTransactionControllerClass(Class newClass): to allow the user to define
 * the external transaction controller when the 904 sessions.xml defines an
 * external-transaction-controller-class.
 *
 */
public final class CustomServerPlatform extends ServerPlatformBase {

    /**
     * INTERNAL:
     * Default Constructor: JTA is disabled until a transaction controller class is set.
       * Runtime services are disabled.
     */
    public CustomServerPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * in the DatabaseSession
     * This is defined by the user via the 904 sessions.xml.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public Class getExternalTransactionControllerClass() {
        return externalTransactionControllerClass;
    }

    /**
     * INTERNAL: externalTransactionControllerNotNullWarning():
       * When the external transaction controller is being initialized, we warn the developer
       * if they have already defined the external transaction controller in some way other
       * than subclassing ServerPlatformBase.
       *
       * This warning is omitted in 9.0.4.
     *
       * @see ServerPlatformBase
     *
     * @return void
     *
     */
    protected void externalTransactionControllerNotNullWarning() {
        //do nothing, because it would be really annoying to show a warning here
    }
}