// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Map;

import javax.xml.bind.Marshaller;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.ox.XMLMarshalListener;
import oracle.toplink.ox.jaxb.compiler.MarshalCallback;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide a wrapper for a JAXB 2.0 Marshal Listener that implements
 * XMLMarshalListener
 * <p><b>Responsibilities:</b><ul>
 * <li>Implement the XMLMarshalListener API</li>
 * <li>Delegate event callbacks to the wrapped Listener instance</li>
 * <li>Perform JAXB 2.0 Class-Based marshal event callbacks</li>
 * </ul>
 * 
 * @see javax.xml.bind.Marshaller.Listener
 * @see oracle.toplink.ox.XMLMarshalListener
 */

public class JAXBMarshalListener implements XMLMarshalListener {
    private Marshaller.Listener listener;
    private Map classBasedMarshalEvents;
    private javax.xml.bind.Marshaller marshaller;
    
    public JAXBMarshalListener(javax.xml.bind.Marshaller marshaller) {
        this.marshaller = marshaller;
    }
    
    public void setListener(Marshaller.Listener jaxbListener) {
        this.listener = jaxbListener;
    }
    
    public Marshaller.Listener getListener() {
        return listener;
    }
    
    public void beforeMarshal(Object obj) {
        if(classBasedMarshalEvents != null) {
            MarshalCallback callback = (MarshalCallback)classBasedMarshalEvents.get(obj.getClass().getName());
            if(callback != null && callback.getBeforeMarshalCallback() != null) {
                try {
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try{
                            AccessController.doPrivileged(new PrivilegedMethodInvoker(callback.getBeforeMarshalCallback(), obj, new Object[]{marshaller}));
                        }catch (PrivilegedActionException ex){
                            if (ex.getCause() instanceof IllegalAccessException){
                                throw (IllegalAccessException) ex.getCause();
                            }
                            if (ex.getCause() instanceof InvocationTargetException){
                                throw (InvocationTargetException) ex.getCause();
                            }
                            throw (RuntimeException)ex.getCause();
                        }
                    }else{
                        PrivilegedAccessHelper.invokeMethod(callback.getBeforeMarshalCallback(), obj, new Object[]{marshaller});
                    }
                } catch(Exception ex) {}
            }
        }
        if(listener != null) {
            listener.beforeMarshal(obj);
        }
    }
    public void afterMarshal(Object obj) {
        if(classBasedMarshalEvents != null) {
            MarshalCallback callback = (MarshalCallback)classBasedMarshalEvents.get(obj.getClass().getName());
            if (callback != null && callback.getAfterMarshalCallback() != null) {
                try {
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try{
                            AccessController.doPrivileged(new PrivilegedMethodInvoker(callback.getAfterMarshalCallback(), obj, new Object[]{marshaller}));
                        }catch (PrivilegedActionException ex){
                            if (ex.getCause() instanceof IllegalAccessException){
                                throw (IllegalAccessException) ex.getCause();
                            }
                            if (ex.getCause() instanceof InvocationTargetException){
                                throw (InvocationTargetException) ex.getCause();
                            }
                            throw (RuntimeException)ex.getCause();
                        }
                    }else{
                        PrivilegedAccessHelper.invokeMethod(callback.getAfterMarshalCallback(), obj, new Object[]{marshaller});
                    }
                } catch(Exception ex) {}
            }
        }
        if(listener != null) {
            listener.afterMarshal(obj);
        }
    }
    
    public void setClassBasedMarshalEvents(Map events) {
        this.classBasedMarshalEvents = events;
    }
}
