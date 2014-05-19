// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transaction.jboss;

import javax.transaction.TransactionManager;
import oracle.toplink.transaction.JTATransactionController;

/**
 * <p>
 * <b>Purpose</b>: TransactionController implementation for JBoss JTA
 * <p>
 * <b>Description</b>: Implements the required behaviour for controlling JTA 1.0
 * transactions in JBoss. The JTA TransactionManager must be set on the instance.
 * <p>
 * @see oracle.toplink.transaction.JTATransactionController
 */
public class JBossTransactionController extends JTATransactionController {
    public static final String JNDI_TRANSACTION_MANAGER_NAME = "java:/TransactionManager";

    /**
     * INTERNAL:
     * Obtain and return the JTA TransactionManager on this platform
     */
    protected TransactionManager acquireTransactionManager() throws Exception {
        return (TransactionManager)jndiLookup(JNDI_TRANSACTION_MANAGER_NAME);
    }
}