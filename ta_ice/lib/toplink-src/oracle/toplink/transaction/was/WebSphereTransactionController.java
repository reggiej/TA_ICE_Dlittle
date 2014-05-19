// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.transaction.was;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import javax.transaction.TransactionManager;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.security.PrivilegedGetMethod;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.transaction.JTATransactionController;

/**
 * <p>
 * <b>Purpose</b>: TransactionController implementation for WebSphere
 * <p>
 * <b>Description</b>: Implements the required behaviour for controlling transactions
 * in WebSphere
 * <p>
 * @see oracle.toplink.transaction.JTATransactionController
 */
public class WebSphereTransactionController extends JTATransactionController {
    // Class and method to execute to obtain the TransactionManager
    protected final static String TX_MANAGER_FACTORY_CLASS = "com.ibm.ws.Transaction.TransactionManagerFactory";
    protected final static String TX_MANAGER_FACTORY_METHOD = "getTransactionManager";

    public WebSphereTransactionController() {
        super();
    }

    /**
     * INTERNAL:
     * Obtain and return the JTA TransactionManager on this platform.
     * This will be called once when the controller is initialized.
     */
    protected TransactionManager acquireTransactionManager() throws Exception {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try{
                Class clazz = (Class) AccessController.doPrivileged(new PrivilegedClassForName(TX_MANAGER_FACTORY_CLASS));
                Method method = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(clazz, TX_MANAGER_FACTORY_METHOD, null, false));
                return (TransactionManager) AccessController.doPrivileged(new PrivilegedMethodInvoker(method, null, null));
            }catch (PrivilegedActionException ex){
                if (ex.getCause() instanceof ClassNotFoundException){
                    throw (ClassNotFoundException)ex.getCause();
                }
                if (ex.getCause() instanceof NoSuchMethodException){
                    throw (NoSuchMethodException)ex.getCause();
                }
                if (ex.getCause() instanceof IllegalAccessException){
                    throw (IllegalAccessException)ex.getCause();
                }
                if (ex.getCause() instanceof InvocationTargetException){
                    throw (InvocationTargetException)ex.getCause();
                }
                throw (RuntimeException) ex.getCause();
            }
        }else{
            Class clazz = PrivilegedAccessHelper.getClassForName(TX_MANAGER_FACTORY_CLASS);
            Method method = PrivilegedAccessHelper.getMethod(clazz, TX_MANAGER_FACTORY_METHOD, null, false);
            return (TransactionManager)PrivilegedAccessHelper.invokeMethod(method, null, null);
        }
    }
}
