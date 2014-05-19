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
import oracle.toplink.mappings.foundation.AbstractTransformationMapping;
import oracle.toplink.sessions.Record;
import oracle.toplink.sessions.Session;

/**
 *  @version $Header: MethodBasedAttributeTransformer.java 18-sep-2006.14:33:54 gyorke Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 *  This class is used to preserve the old method of doing Attribute Transformations
 *  on a transformation mapping. It is used internally when the older API is used on
 *  a TransformationMapping, and handles doing invocations on the user's domain class
 */
public class MethodBasedAttributeTransformer implements AttributeTransformer {
    protected transient Method attributeTransformationMethod;
    protected AbstractTransformationMapping mapping;
    protected String methodName;

    public MethodBasedAttributeTransformer() {
    }

    public MethodBasedAttributeTransformer(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String name) {
        methodName = name;
    }

    public Method getAttributeTransformationMethod() {
        return attributeTransformationMethod;
    }

    public void setAttributeTransformationMethod(Method theMethod) {
        attributeTransformationMethod = theMethod;
    }

    /**
     * INTERNAL:
     * Initilizes the transformer. Looks up the transformation method on the
     * domain class using reflection. This method can have either 1 or 2 parameters.
     */
    public void initialize(AbstractTransformationMapping mapping) {
        this.mapping = mapping;
        try {
            // look for the one-argument version first
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = ClassConstants.Record_Class;
            attributeTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, parameterTypes);
        } catch (Exception ex) {
            try {
                //now look for the one-argument version with Record
                Class[] parameterTypes = new Class[1];
                parameterTypes[0] = ClassConstants.Record_Class;
                attributeTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, parameterTypes);
            } catch (Exception ex2) {
                try {
                    // if the one-argument version is not there, look for the two-argument version
                    Class[] parameterTypes = new Class[2];
                    parameterTypes[0] = ClassConstants.Record_Class;
                    parameterTypes[1] = ClassConstants.PublicInterfaceSession_Class;
                    attributeTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, parameterTypes);
                } catch (Exception ex3) {
                    try {
                        //now look for the 2 argument version using Record and sessions Session
                        Class[] parameterTypes = new Class[2];
                        parameterTypes[0] = ClassConstants.Record_Class;
                        parameterTypes[1] = ClassConstants.SessionsSession_Class;
                        attributeTransformationMethod = Helper.getDeclaredMethod(mapping.getDescriptor().getJavaClass(), methodName, parameterTypes);
                    } catch (NoSuchMethodException exception) {
                        throw DescriptorException.noSuchMethodOnInitializingAttributeMethod(mapping.getAttributeMethodName(), mapping, exception);
                    } catch (SecurityException exception) {
                        throw DescriptorException.securityOnInitializingAttributeMethod(mapping.getAttributeMethodName(), mapping, exception);
                    }
                }
            }
        }
        if (attributeTransformationMethod.getReturnType() == ClassConstants.Void_Class) {
            throw DescriptorException.returnTypeInGetAttributeAccessor(methodName, mapping);
        }
    }

    /**
     * INTERNAL:
     * Build the attribute value by invoking the user's transformation method.
     */
    public Object buildAttributeValue(Record record, Object object, Session session) {
        Class[] parameterTypes = null;
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try{
                parameterTypes = (Class[])AccessController.doPrivileged(new PrivilegedGetMethodParameterTypes(attributeTransformationMethod));
            }catch (PrivilegedActionException ex){
                throw (RuntimeException)ex.getCause();
            }
        }else{
            parameterTypes = PrivilegedAccessHelper.getMethodParameterTypes(attributeTransformationMethod);
        }
        Object[] parameters = new Object[parameterTypes.length];
        parameters[0] = record;
        if (parameters.length == 2) {
            parameters[1] = session;
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(attributeTransformationMethod, object, parameters));
                }catch (PrivilegedActionException ex){
                    if (ex.getCause() instanceof IllegalArgumentException){
                        throw (IllegalArgumentException) ex.getCause();
                    }
                    if (ex.getCause() instanceof InvocationTargetException){
                        throw (InvocationTargetException) ex.getCause();
                    }
                    throw (RuntimeException) ex.getCause();
                }
            }else {
                return PrivilegedAccessHelper.invokeMethod(attributeTransformationMethod, object, parameters);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileInvokingAttributeMethod(mapping, exception);
        } catch (IllegalArgumentException exception) {
            throw DescriptorException.illegalArgumentWhileInvokingAttributeMethod(mapping, exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileInvokingAttributeMethod(mapping, exception);
        }
    }
}