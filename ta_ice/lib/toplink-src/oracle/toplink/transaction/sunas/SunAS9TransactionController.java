
// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.transaction.sunas;

import javax.transaction.TransactionManager;

import oracle.toplink.transaction.JTATransactionController;

/**
 * <p>
 * <b>Purpose</b>: TransactionController implementation for OC4J JTA
 * <p>
 * <b>Description</b>: Implements the required behaviour for controlling JTA
 * transactions in SunAS9. The JTA TransactionManager must be set on the instance.
 * <p>
 * @see oracle.toplink.transaction.JTATransactionController
 */
public class SunAS9TransactionController extends JTATransactionController {
    // Use "java:appserver/TransactionManager" instead of java:pm/TransactionManager
    // as the former one is available in ACC as well as server.
    // See com.sun.enterprise.naming.java.javaURLContext in GlassFish appserv-core
    public static final String JNDI_TRANSACTION_MANAGER_NAME = "java:appserver/TransactionManager";

    public SunAS9TransactionController() {
        super();
    }

    /**
     * INTERNAL:
     * Obtain and return the JTA TransactionManager on this platform
     */
    protected TransactionManager acquireTransactionManager() throws Exception {
        return (TransactionManager)jndiLookup(JNDI_TRANSACTION_MANAGER_NAME);
    }
}
