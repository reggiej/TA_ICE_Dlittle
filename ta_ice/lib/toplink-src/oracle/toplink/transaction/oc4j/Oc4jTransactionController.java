// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transaction.oc4j;

import javax.transaction.TransactionManager;
import oracle.toplink.transaction.JTATransactionController;

/**
 * <p>
 * <b>Purpose</b>: TransactionController implementation for OC4J JTA
 * <p>
 * <b>Description</b>: Implements the required behaviour for controlling JTA
 * transactions in OC4J. The JTA TransactionManager must be set on the instance.
 * <p>
 * @see oracle.toplink.transaction.JTATransactionController
 */
public class Oc4jTransactionController extends JTATransactionController {
    public static final String JNDI_TRANSACTION_MANAGER_NAME = "java:comp/pm/TransactionManager";

    public Oc4jTransactionController() {
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