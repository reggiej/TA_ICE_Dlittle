// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.compiler;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import javax.xml.bind.Unmarshaller;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.security.PrivilegedGetMethod;

/**
 * INTERNAL:
 *  <p><b>Purpose:</b>Hold information about class based JAXB 2.0 Callback methods
 *  <p><b>Responsibilities:</b><ul>
 *  <li>Store information about domainClass and the callback methods</li>
 *  <li>Act as a means to integrate JAXB 2.0 Class based callbacks with TopLink OXM 
 *  Listener based callbacks.</li>
 *  
 *  A Map of UnmarshalCallbacks are created during JAXB 2.0 Generation and are set on a
 *  JAXBUnmarshallerListener on each JAXBUnmarshaller. These callbacks are used to invoke
 *  the JAXB 2.0 Class Based callbacks on each object at the appropriate time.
 *  
 *  @author mmacivor
 *  @since Oracle TopLink 11.1.1.0.0
 *  @see oracle.toplink.ox.jaxb.JAXBUnmarshalListener
 *  @see oracle.toplink.ox.jaxb.JAXBUnmarshaller
 */
public class UnmarshalCallback {
    private Class domainClass;
    private String domainClassName;
    private Method afterUnmarshalCallback;
    private Method beforeUnmarshalCallback;
    private boolean hasAfterUnmarshalCallback = false;
    private boolean hasBeforeUnmarshalCallback = false;
    
    public Method getAfterUnmarshalCallback() {
        return afterUnmarshalCallback;
    }

    public Method getBeforeUnmarshalCallback() {
        return beforeUnmarshalCallback;
    }

    public Class getDomainClass() {
        return domainClass;
    }
    
    /**
     * @param loader
     */
    public void initialize(ClassLoader loader) {        
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    domainClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(domainClassName, true, loader));
                }catch (PrivilegedActionException ex){
                    if (ex.getCause() instanceof ClassNotFoundException){
                        throw (ClassNotFoundException) ex.getCause();
                    }
                    throw (RuntimeException)ex.getCause();
                }
            }else{
                domainClass = PrivilegedAccessHelper.getClassForName(domainClassName, true, loader);
            }
        } catch (ClassNotFoundException ex) {
            // TODO: should never get here, but may want to throw an exception
            return;
        }
        Class[] params = new Class[]{ Unmarshaller.class, Object.class };
        if (hasBeforeUnmarshalCallback) {
            try {
                Method beforeUnmarshal = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        beforeUnmarshal = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(domainClass, "beforeUnmarshal", params, false));
                    }catch (PrivilegedActionException ex){
                        if (ex.getCause() instanceof NoSuchMethodException){
                            throw (NoSuchMethodException) ex.getCause();
                        }
                        throw (RuntimeException)ex.getCause();
                    }
                }else{
                    beforeUnmarshal = PrivilegedAccessHelper.getMethod(domainClass, "beforeUnmarshal", params, false);
                }
                setBeforeUnmarshalCallback(beforeUnmarshal);
            } catch (NoSuchMethodException nsmex) {}
        }
        if (hasAfterUnmarshalCallback) {
            try {
                Method afterUnmarshal = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        afterUnmarshal = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(domainClass, "afterUnmarshal", params, false));
                    }catch (PrivilegedActionException ex){
                        if (ex.getCause() instanceof NoSuchMethodException){
                            throw (NoSuchMethodException) ex.getCause();
                        }
                        throw (RuntimeException)ex.getCause();
                    }
                }else{
                    afterUnmarshal = PrivilegedAccessHelper.getMethod(domainClass, "afterUnmarshal", params, false);
                }
                setAfterUnmarshalCallback(afterUnmarshal);
            } catch (NoSuchMethodException nsmex) {}
        }
    }

    /**
     * Should not use this method - the init method will 
     * overwrite the set value. 
     */
    public void setAfterUnmarshalCallback(Method method) {
        afterUnmarshalCallback = method;
    }

    public void setHasAfterUnmarshalCallback() {
        hasAfterUnmarshalCallback = true;
    }

    /**
     * Should not use this method - the init method will 
     * overwrite the set value. 
     */
    public void setBeforeUnmarshalCallback(Method method) {
        beforeUnmarshalCallback = method;
    }

    public void setHasBeforeUnmarshalCallback() {
        hasBeforeUnmarshalCallback = true;
    }

    /**
     * Should use setDomainClassName - the init method will overwrite
     * the set value with Class.forName(domainClassName)
     * 
     * @param clazz
     */
    public void setDomainClass(Class clazz) {
        domainClass = clazz;
        setDomainClassName(clazz.getName());
    }

    public void setDomainClassName(String className) {
        domainClassName = className;
    }
}