// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import oracle.javatools.parser.java.v2.classfile.Name;
import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaField;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>The purpose of this class is to act as a dynamic proxy 
 * that allows JDK Annotation method calls to be made on a TopLink JAXB 2.0
 * Java model JavaAnnotation.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Create and return a dynamic proxy instance based on a TopLink JAXB 2.0
 * Java model JavaAnnotation</li>
 * <li>Allow JDK Annotation method calls to be invoked on the proxy
 * object</li>
 * </ul>
 * <p> This class provides a means to invoke JDK Annotation method calls on a 
 * TopLink JAXB 2.0 java model JavaAnnotation instance.
 *  
 * @since   Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaAnnotation
 * @see oracle.toplink.ox.jaxb.javamodel.JavaHasAnnotations
 * @see java.lang.reflect.Proxy
 */
public class AnnotationProxy implements InvocationHandler {
    private JavaAnnotation jAnnotation = null;

    public AnnotationProxy(JavaAnnotation janno) {
        this.jAnnotation = janno;
    }

    public Object invoke(Object arg0, Method method, Object[] arg2) throws Throwable {
        Object val = jAnnotation.getComponents().get(method.getName());
        Class returnType = method.getReturnType();

        if (returnType.isArray()) {
            return handleArrayData(returnType, val);
        } else if (Class.class.getName().equals(returnType.getName())) {
            JavaClass jc = (JavaClass) val;
            try {
                return Class.forName(jc.getRawName());
            } catch (ClassNotFoundException cnfe) {
                int idx = jc.getRawName().lastIndexOf(".");
                String name = "";
                if (idx != -1) {
                    name = jc.getRawName().substring(0, idx) + "$" + jc.getRawName().substring(idx+1);
                }
                return Class.forName(name);
            }
        } else if (String.class.getName().equals(returnType.getName())) {
            if (val instanceof Name) {
                return val.toString();
            } else if (val instanceof String) {
                return val;
            }
        } else if (returnType.isEnum()) {
            String name = null;
            if (val instanceof JavaField) {
                name = ((JavaField) val).getName();
            } else if (val instanceof String) {
                name = (String) val;
            }
            return Enum.valueOf(returnType, name);
        } else if (returnType.isAnnotation()) {
            JavaAnnotation ja = (JavaAnnotation) val;
            InvocationHandler ih = new AnnotationProxy(ja);
            return (Annotation) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { returnType }, ih);
        }

        // catch any remaining which should only be primitives
        return val;
    }

    public static <A extends Annotation> A getProxy(JavaAnnotation janno, Class<A> annoClass, ClassLoader cl) {
        return (A) Proxy.newProxyInstance(cl, new Class[] { annoClass }, new AnnotationProxy(janno));

    }

    private Object handleArrayData(Class returnType, Object val) throws ClassNotFoundException {
        Object[] data = (Object[]) val;
        Class componentType = returnType.getComponentType();
        Object res = Array.newInstance(componentType, data.length);

        if (componentType.isPrimitive()) {
            return primitiveTypeArray(componentType, res, data);
        } else {
            Object[] tmp = (Object[]) res;

            if (Class.class.getName().equals(componentType.getName())) {
                for (int i = 0; i < data.length; i++) {
                    JavaClass jc = (JavaClass) data[i];
                    tmp[i] = Class.forName(jc.getRawName());
                }
            } else if (String.class.getName().equals(componentType.getName())) {
                for (int i = 0; i < data.length; i++) {
                    if (data[i] instanceof Name) {
                        tmp[i] = ((Name) data[i]).toString();
                    } else if (data[i] instanceof String) {
                        tmp[i] = (String) data[i];
                    }
                }
            } else if (componentType.isEnum()) {
                String name = null;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] instanceof JavaField) {
                        name = ((JavaField) data[i]).getName();
                    } else if (data[i] instanceof String) {
                        name = (String) data[i];
                    } else {
                        name = "";
                    }
                    tmp[i] = Enum.valueOf(componentType, name);
                }
            } else if (componentType.isAnnotation()) {
                for (int i = 0; i < data.length; i++) {
                    JavaAnnotation ja = (JavaAnnotation) data[i];
                    InvocationHandler ih = new AnnotationProxy(ja);
                    tmp[i] = (Annotation) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { componentType }, ih);
                }
            }
        }

        return res;
    }

    private Object primitiveTypeArray(Class componentType, Object res, Object[] data) {
        if ("int".equals(componentType.getName())) {
            int[] tmp = (int[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Integer) data[i];
            }
        } else if ("char".equals(componentType.getName())) {
            char[] tmp = (char[]) res;
            for (int i = 0; i < data.length; i++) {
                if (data[i] instanceof Character) {
                    Character c = (Character) data[i];
                    tmp[i] = c.charValue();
                }
            }
        } else if ("byte".equals(componentType.getName())) {
            byte[] tmp = (byte[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Byte) data[i];
            }
        } else if ("short".equals(componentType.getName())) {
            short[] tmp = (short[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = ((Integer) data[i]).shortValue();
            }
        } else if ("long".equals(componentType.getName())) {
            long[] tmp = (long[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Long) data[i];
            }
        } else if ("float".equals(componentType.getName())) {
            float[] tmp = (float[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Float) data[i];
            }
        } else if ("double".equals(componentType.getName())) {
            double[] tmp = (double[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Double) data[i];
            }
        } else if ("boolean".equals(componentType.getName())) {
            boolean[] tmp = (boolean[]) res;
            for (int i = 0; i < data.length; i++) {
                tmp[i] = (Boolean) data[i];
            }
        }
        return res;
    }

}
