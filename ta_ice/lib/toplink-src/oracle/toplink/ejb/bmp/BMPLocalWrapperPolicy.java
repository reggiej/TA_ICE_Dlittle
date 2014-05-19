// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb.bmp;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.descriptors.*;
import oracle.toplink.mappings.*;
import oracle.toplink.ejb.*;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * PUBLIC:
 * <p><b>Purpose</b>: Allow for EntityBean descriptors to store information about the bean properties.</P>
 * BMPLocalWrapperPolicy should be used for local beans rather than remote beans.
 */
public class BMPLocalWrapperPolicy extends BMPWrapperPolicy {

    /**
     * PUBLIC:
     * This constructor creates a Wrapper policy for a particular bean class
     *
     * @param homeLookUpName java.lang.String
     * @param homeInterfaceClass java.lang.String
     * @param PrimaryKeyClass java.lang.Class
     * @param interfaceClass java.lang.Class
     * @param properties java.util.Hashtable This parameter contains the initial context properties
     */
    public BMPLocalWrapperPolicy(String homeLookUpName, Class homeInterfaceClass, Class primaryKeyClass, Class interfaceClass, Hashtable contextProperties) throws ValidationException {
        super(homeLookUpName, homeInterfaceClass, primaryKeyClass, interfaceClass, contextProperties);
    }

    /**
     * INTERNAL:
     * Get the primary key from this object through the methods in EJBObject
     */
    public Object getPrimaryKeyFromObject(Object object, AbstractSession session) {
        try {
            return ((EJBLocalObject)object).getPrimaryKey();
        } catch (Exception exception) {
            return session.handleException(ValidationException.ejbContainerExceptionRaised(exception));
        }
    }

    /**
     * Return the remote from the home by primary key.
     * Validate inheritance for the object.
     */
    public Object lookupWrapperForBean(Object bean, Object primaryKeyObject, AbstractSession session) {
        try {
            Context initialContext = new InitialContext(this.contextProperties);

            //CR2191 EJBHome should be fully qualified to be javax.ejb.EJBHome 
            // Bug 3039113 - changed reference to EJB20ClassConstants
            javax.ejb.EJBLocalHome home = (javax.ejb.EJBLocalHome)javax.rmi.PortableRemoteObject.narrow(initialContext.lookup(getHomeName()), EJB20ClassConstants.EJBLocalHome_Class);
            Object[] param = { primaryKeyObject };
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                return AccessController.doPrivileged(new PrivilegedMethodInvoker(homeFindByPrimaryKeyMethod, home, param));
            }else{
                return PrivilegedAccessHelper.invokeMethod(homeFindByPrimaryKeyMethod, home, param);
            }
        } catch (Exception exception) {
            return session.handleException(ValidationException.ejbContainerExceptionRaised(exception));
        }
    }
}