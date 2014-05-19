// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.ejb.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.descriptors.*;
import oracle.toplink.mappings.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetDeclaredField;
import oracle.toplink.internal.security.PrivilegedGetValueFromField;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.internal.security.PrivilegedSetValueInField;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Allow for EntityBean descriptors to store information about the bean properties.
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public abstract class EJBWrapperPolicy implements WrapperPolicy {
    protected ClassDescriptor descriptor;
    protected Class primaryKeyClass;
    protected Class homeInterface;
    protected Class remoteInterface;
    protected String homeName;

    /** Added to separate custom (such as AccountPK) from non-custom (such as java.lang.Integer) */
    protected boolean hasCustomPrimaryKey;

    /**
     * PUBLIC: Default constructor. Initialize.
     */
    public EJBWrapperPolicy() {
        this.hasCustomPrimaryKey = true;
    }

    /**
     * INTERNAL:
     * Return an instance of the primary key class with the bean primary key values.
     */
    public Object buildBeanFromPrimaryKey(Object primaryKeyObject, AbstractSession session) {
        Object bean = null;
        Object value = null;

        try {
            ClassDescriptor descriptor = getDescriptor();
            bean = descriptor.getObjectBuilder().buildNewInstance();
            if (!hasCustomPrimaryKey()) {
                value = primaryKeyObject;
                ((DatabaseMapping)descriptor.getObjectBuilder().getPrimaryKeyMappings().firstElement()).setAttributeValueInObject(bean, value);
                return bean;
            }
            for (Enumeration mappingsEnum = descriptor.getObjectBuilder().getPrimaryKeyMappings().elements();
                     mappingsEnum.hasMoreElements();) {
                DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
                String attributeName = mapping.getAttributeName();
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    Field field = (Field)AccessController.doPrivileged(new PrivilegedGetDeclaredField(primaryKeyObject.getClass(), attributeName, false));
                    value = AccessController.doPrivileged(new PrivilegedGetValueFromField(field, primaryKeyObject));
                }else{
                    Field field = PrivilegedAccessHelper.getDeclaredField(primaryKeyObject.getClass(), attributeName, false);
                    value = PrivilegedAccessHelper.getValueFromField(field, primaryKeyObject);
                }
                mapping.setAttributeValueInObject(bean, value);
            }
        } catch (Exception exception) {
            return session.handleException(ValidationException.ejbPrimaryKeyReflectionException(exception, primaryKeyObject, bean));
        }

        return bean;
    }

    /**
     * INTERNAL:
     * Return an instance of the primary key class with the bean primary key values.
     */
    public Object buildPrimaryKeyFromBean(Object bean, AbstractSession session) {
        Object primaryKeyObject = null;

        try {
            ClassDescriptor descriptor = getDescriptor();
            if (!hasCustomPrimaryKey()) {
                primaryKeyObject = ((DatabaseMapping)descriptor.getObjectBuilder().getPrimaryKeyMappings().firstElement()).getAttributeValueFromObject(bean);
                return primaryKeyObject;
            }
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                 primaryKeyObject = AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(primaryKeyClass));
            }else{
                primaryKeyObject = PrivilegedAccessHelper.newInstanceFromClass(primaryKeyClass);
            }
            for (Enumeration mappingsEnum = descriptor.getObjectBuilder().getPrimaryKeyMappings().elements();
                     mappingsEnum.hasMoreElements();) {
                DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
                String attributeName = mapping.getAttributeName();
                Object value = mapping.getAttributeValueFromObject(bean);
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    Field field = (Field)AccessController.doPrivileged(new PrivilegedGetDeclaredField(primaryKeyClass, attributeName, false));
                    AccessController.doPrivileged(new PrivilegedSetValueInField(field, primaryKeyObject, value));
                }else{
                    Field field = PrivilegedAccessHelper.getDeclaredField(primaryKeyClass, attributeName, false);
                    PrivilegedAccessHelper.setValueInField(field, primaryKeyObject, value);
                }
            }
        } catch (Exception exception) {
            return session.handleException(ValidationException.ejbPrimaryKeyReflectionException(exception, primaryKeyObject, bean));
        }

        return primaryKeyObject;
    }

    /**
     * INTERNAL:
     * Return an instance of the primary key class with the bean primary key values.
     */
    public void dumpPrimaryKeyIntoBean(Object primaryKeyObject, Object bean, AbstractSession session) {
        try {
            ClassDescriptor descriptor = getDescriptor();
            if (!hasCustomPrimaryKey()) {
                ((DatabaseMapping)descriptor.getObjectBuilder().getPrimaryKeyMappings().firstElement()).setAttributeValueInObject(bean, primaryKeyObject);
                return;
            }
            for (Enumeration mappingsEnum = descriptor.getObjectBuilder().getPrimaryKeyMappings().elements();
                     mappingsEnum.hasMoreElements();) {
                DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
                String attributeName = mapping.getAttributeName();
                Object value =  null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        Field field = (Field)AccessController.doPrivileged(new PrivilegedGetDeclaredField(primaryKeyObject.getClass(), attributeName, false));
                        AccessController.doPrivileged(new PrivilegedGetValueFromField(field, primaryKeyObject));
                    }catch (PrivilegedActionException ex){
                        if (ex.getCause() instanceof NoSuchMethodException){
                            throw (NoSuchMethodException) ex.getCause();
                        }
                        throw (RuntimeException)ex.getCause();
                    }
                }else{
                    Field field = PrivilegedAccessHelper.getDeclaredField(primaryKeyObject.getClass(), attributeName, false);
                    value = PrivilegedAccessHelper.getValueFromField(field, primaryKeyObject);
                }
                mapping.setAttributeValueInObject(bean, value);
            }
        } catch (Exception exception) {
            session.handleException(ValidationException.ejbPrimaryKeyReflectionException(exception, primaryKeyObject, bean));
        }
    }

    /**
     * INTERNAL:
     * Return the descriptor back reference.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * PUBLIC:
     * The home interface defines how a bean is accessed and queried.
     * It includes the create and finder definitions.
     */
    public Class getHomeInterface() {
        return homeInterface;
    }

    /**
     * PUBLIC:
     * The home name is the name or URL that the bean's home is stored in JNDI under.
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * PUBLIC:
     * The primary key class is the class that represents the bean's primary key.
     * It must have the same attributes as the bean's primary key attributes of the same type.
     * Primary key classes can be shared, but normally are exclusive.
     */
    public Class getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    /**
     * INTERNAL:
     * Get the primary key from this object through the methods in EJBObject
     */
    public Object getPrimaryKeyFromObject(Object object, AbstractSession session) {
        try {
            return ((EJBObject)object).getPrimaryKey();
        } catch (Exception exception) {
            return session.handleException(ValidationException.ejbContainerExceptionRaised(exception));
        }
    }

    /**
     * PUBLIC:
     * The remote interface defines a bean's public interface.
     */
    public Class getRemoteInterface() {
        return remoteInterface;
    }

    /**
     * PUBLIC: Answer whether the primary key is custom (such as AccountPK) or non-custom
     * (such as java.lang.Integer)
     */
    public boolean hasCustomPrimaryKey() {
        return hasCustomPrimaryKey;
    }

    /**
     * INTERNAL:
     * Initialize the descriptor.
     * This adds the bean descriptor under the remote interface to allow querying on the interface.
     */
    public void initialize(AbstractSession session) {
        session.getDescriptors().put(getRemoteInterface(), getDescriptor());
        Enumeration queryEnumeration = getDescriptor().getQueryManager().getAllQueries().elements();
        while (queryEnumeration.hasMoreElements()) {
            ((DatabaseQuery)queryEnumeration.nextElement()).setShouldUseWrapperPolicy(false);
        }
    }

    /**
     * REQUIRED:
     * Return if the wrapped value should be traverse.
     * Because normally the wrapped value is looked after independently it is not required to be traversed.
     */
    public boolean isTraversable() {
        return false;
    }

    /**
     * INTERNAL:
     * Return if the object is wrapped, assume implementors of EntityBean are beans.
     */
    public boolean isWrapped(Object beanOrRemote) {
        return !(beanOrRemote instanceof EntityBean);
    }

    /**
     * INTERNAL:
     * Set the descriptor back reference.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * PUBLIC:
     * Used to set whether the primary key is custom (such as AccountPK) or non-custom
     * (such as java.lang.Integer)
     */
    public void setHasCustomPrimaryKey(boolean hasCustomPrimaryKey) {
        this.hasCustomPrimaryKey = hasCustomPrimaryKey;
    }

    /**
     * PUBLIC:
     * The home interface defines how a bean is accessed and queried.
     * It includes the create and finder definitions.
     */
    public void setHomeInterface(Class homeInterface) {
        this.homeInterface = homeInterface;
    }

    /**
     * PUBLIC:
     * The home name is the name or URL that the bean's home is stored in JNDI under.
     */
    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    /**
     * PUBLIC:
     * The primary key class is the class that represents the bean's primary key.
     * It must have the same number and type of attributes as the bean's primary key attributes.
     * Primary key classes can be shared, but normally are exclusive.
     */
    public void setPrimaryKeyClass(Class primaryKeyClass) {
        this.primaryKeyClass = primaryKeyClass;
    }

    /**
     * PUBLIC:
     * The remote interface defines a bean's public interface.
     */
    public void setRemoteInterface(Class remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    /**
     * PUBLIC:
     * Unwrap the object to return the implementation that is meant to be used by TopLink.
     * The object may already be unwrapped in which case the object should be returned.
     */
    public Object unwrapObject(Object remote, AbstractSession session) {
        if ((remote == null) || (!this.isWrapped(remote))) {
            return remote;
        }

        session.startOperationProfile(SessionProfiler.Wrapping);
        Object primaryKeyObject = null;
        primaryKeyObject = getPrimaryKeyFromObject(remote, session);
        Object bean = buildBeanFromPrimaryKey(primaryKeyObject, session);
        Vector key = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(bean, session);

        Object unwrapped = session.getIdentityMapAccessorInstance().getFromIdentityMap(key, getDescriptor().getJavaClass(), getDescriptor());
        if (unwrapped == null) {
            ReadObjectQuery query = new ReadObjectQuery(bean);
            query.setShouldUseWrapperPolicy(false);
            unwrapped = session.executeQuery(query);
        }
        session.getIdentityMapAccessorInstance().setWrapper(key, bean.getClass(), remote);
        session.endOperationProfile(SessionProfiler.Wrapping);

        return unwrapped;
    }
}