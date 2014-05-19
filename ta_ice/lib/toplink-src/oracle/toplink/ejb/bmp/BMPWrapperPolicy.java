// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb.bmp;

import javax.ejb.EntityBean;
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
import oracle.toplink.mappings.transformers.FieldTransformer;
import oracle.toplink.ejb.*;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetMethod;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Allow for EntityBean descriptors to store information about the bean properties.</P>
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public class BMPWrapperPolicy extends EJBWrapperPolicy {
    protected java.lang.reflect.Method homeFindByPrimaryKeyMethod;

    /** This variable stores the properties required when creating the initialContext */
    protected Hashtable contextProperties;

    /**
     * This constructor creates a Wrapper policy for a particular bean class
     *
     * @param homeLookUpName java.lang.String
     * @param homeInterfaceClass java.lang.String
     * @param PrimaryKeyClass java.lang.Class
     * @param interfaceClass java.lang.Class
     * @param properties java.util.Hashtable This parameter contains the initial context properties
     */
    public BMPWrapperPolicy(String homeLookUpName, Class homeInterfaceClass, Class primaryKeyClass, Class interfaceClass, Hashtable contextProperties) throws ValidationException {
        super();
        this.homeName = homeLookUpName;
        this.homeInterface = homeInterfaceClass;
        this.primaryKeyClass = primaryKeyClass;
        this.remoteInterface = interfaceClass;
        this.contextProperties = contextProperties;
        checkForNonCustomPrimaryKey(primaryKeyClass);
        Class[] params = { getPrimaryKeyClass() };
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    this.homeFindByPrimaryKeyMethod = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(getHomeInterface(), "findByPrimaryKey", params, false));
                }catch (PrivilegedActionException ex){
                    if (ex.getCause() instanceof NoSuchMethodException){
                        throw (NoSuchMethodException) ex.getCause();
                    }
                    throw (RuntimeException)ex.getCause();
                }
            }else{
                this.homeFindByPrimaryKeyMethod = PrivilegedAccessHelper.getMethod(getHomeInterface(), "findByPrimaryKey", params, false);
            }
        } catch (NoSuchMethodException exception) {
            throw ValidationException.ejbInvalidHomeInterfaceClass(getHomeInterface());
        }
    }

    /**
     * Determine based on the primaryKeyClass whether this is a custom primary key
     */
    public void checkForNonCustomPrimaryKey(Class primaryKeyClass) {
        // Have to parse the package name directly, because JDK 1.1 does not have a Class.getPackage() method. - RMB
        String packageName = primaryKeyClass.getName().substring(0, primaryKeyClass.getName().lastIndexOf("."));

        if ((primaryKeyClass == null) || (packageName == null)) {
            return;
        }

        // We assume that non-custom primary keys will either be in the java.lang package or the java.math package
        if (packageName.equals("java.lang") || packageName.equals("java.math")) {
            hasCustomPrimaryKey = false;
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
            javax.ejb.EJBHome home = (javax.ejb.EJBHome)javax.rmi.PortableRemoteObject.narrow(initialContext.lookup(getHomeName()), EJBClassConstants.EJBHome_Class);
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

    /**
     * Wrap the bean with its remote.
     */
    public Object wrapObject(Object bean, AbstractSession session) {
        session.startOperationProfile(SessionProfiler.Wrapping);
        Vector key = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(bean, session);

        // Wrappers are cached so first do a cache lookup,
        Object wrapped = session.getIdentityMapAccessorInstance().getWrapper(key, bean.getClass());
        if (wrapped != null) {
            session.endOperationProfile(SessionProfiler.Wrapping);
            return wrapped;
        }

        // otherwise a wrapper must be build from the container.
        Object primaryKeyObject = buildPrimaryKeyFromBean(bean, session);
        wrapped = lookupWrapperForBean(bean, primaryKeyObject, session);
        session.getIdentityMapAccessorInstance().setWrapper(key, bean.getClass(), wrapped);
        session.endOperationProfile(SessionProfiler.Wrapping);

        return wrapped;
    }
}