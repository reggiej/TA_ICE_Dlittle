// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetMethodParameterTypes;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;

/**
 *  @version $Header: MethodBasedFieldTransformer.java 18-sep-2006.16:20:59 gyorke Exp $
 *  @author  mmacivor
 *  @since   10
 *  This class is used to preserve the old method of doing Field Transformations
 *  on a transformation mapping. It is used internally when the older API is used on
 *  a TransformationMapping, and handles doing invocations on the user's domain class
 */
public class MethodBasedFieldTransformer implements FieldTransformer {
    protected transient Method fieldTransformationMethod;
    protected String methodName;
    protected AbstractTransformationMapping mapping;

    public MethodBasedFieldTransformer(String methodName) {
        this.methodName = methodName;
    }

    public void initialize(AbstractTransformationMapping mapping) {
        this.mapping = mapping;
        try {
            // look for the zero-argument version first
            fieldTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, new Class[0]);
        } catch (Exception ex) {
            try {
                // if the zero-argument version is not there, look for the one-argument version
                Class[] methodParameterTypes = new Class[1];
                methodParameterTypes[0] = ClassConstants.PublicInterfaceSession_Class;
                fieldTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, methodParameterTypes);
            } catch (Exception ex2) {
                try {
                    //if the one-argument version is absent, try with sessions.Session
                    Class[] methodParameterTypes = new Class[1];
                    methodParameterTypes[0] = ClassConstants.SessionsSession_Class;
                    fieldTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, methodParameterTypes);
                } catch (NoSuchMethodException exception) {
                    throw DescriptorException.noSuchMethodWhileConvertingToMethod(methodName, mapping, exception);
                } catch (SecurityException exception) {
                    throw DescriptorException.securityWhileConvertingToMethod(methodName, mapping, exception);
                }
            }
        }
    }

    public Object buildFieldValue(Object object, String fieldName, Session session) {
        Class[] parameterTypes = null;
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try{
                parameterTypes = (Class [])AccessController.doPrivileged(new PrivilegedGetMethodParameterTypes(fieldTransformationMethod));
            }catch (PrivilegedActionException ex){
                throw (RuntimeException) ex.getCause();
            }
        }else{
            parameterTypes = PrivilegedAccessHelper.getMethodParameterTypes(fieldTransformationMethod);
        }

        Object[] parameters = new Object[parameterTypes.length];
        if (parameters.length == 1) {
            parameters[0] = session;
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(fieldTransformationMethod, object, parameters));
                }catch (PrivilegedActionException ex){
                    if (ex.getCause() instanceof IllegalAccessException){
                        throw (IllegalAccessException) ex.getCause();
                    }
                    if (ex.getCause() instanceof InvocationTargetException){
                        throw (InvocationTargetException) ex.getCause();
                    }
                    throw (RuntimeException) ex.getCause();
                }
            }else{
                return PrivilegedAccessHelper.invokeMethod(fieldTransformationMethod, object, parameters);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileInvokingFieldToMethod(fieldTransformationMethod.getName(), mapping, exception);
        } catch (IllegalArgumentException exception) {
            throw DescriptorException.illegalArgumentWhileInvokingFieldToMethod(fieldTransformationMethod.getName(), mapping, exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileInvokingFieldToMethod(fieldTransformationMethod.getName(), mapping, exception);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String name) {
        methodName = name;
    }
}