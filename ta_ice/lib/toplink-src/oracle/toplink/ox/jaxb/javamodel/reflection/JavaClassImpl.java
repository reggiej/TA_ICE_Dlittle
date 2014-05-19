// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.reflection;

import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaField;
import oracle.toplink.ox.jaxb.javamodel.JavaMethod;
import oracle.toplink.ox.jaxb.javamodel.JavaPackage;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JDK Class.  This implementation
 * of the TopLink JAXB 2.0 Java model simply makes reflective calls on the 
 * underlying JDK object. 
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying JDK Class' name, package, 
 * method/field names and parameters, annotations, etc.</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaClass
 * @see java.lang.Class
 */
public class JavaClassImpl implements JavaClass {
    protected ParameterizedType jType;
    protected Class jClass;

    public JavaClassImpl(Class javaClass) {
        jClass = javaClass;
    }

    public JavaClassImpl(ParameterizedType javaType, Class javaClass) {
        jType = javaType;
        jClass = javaClass;
    }
    
    public Collection getActualTypeArguments() {
        ArrayList<JavaClassImpl> argCollection = new ArrayList<JavaClassImpl>();
        if (jType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) jType;
            Type[] params = pType.getActualTypeArguments();
            for (Type type : params) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    argCollection.add(new JavaClassImpl(pt, (Class) pt.getRawType()));
                } else if (type instanceof Class) {
                    argCollection.add(new JavaClassImpl((Class) type));
                }
            }
        }
        return argCollection;
    }

    public String toString() {
        return getName();
    }
    
    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public JavaAnnotation getAnnotation(JavaClass arg0) {
        if (arg0 != null) {
            Class annotationClass = ((JavaClassImpl) arg0).getJavaClass();
            if (jClass.isAnnotationPresent(annotationClass)) {
                return new JavaAnnotationImpl(jClass.getAnnotation(annotationClass));
            }
        }
        return null;
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotation> annotationCollection = new ArrayList<JavaAnnotation>();
        Annotation[] annotations = jClass.getAnnotations();
        for (Annotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public Collection getDeclaredClasses() {
        ArrayList<JavaClass> classCollection = new ArrayList<JavaClass>();
        Class[] classes = jClass.getDeclaredClasses();
        for (Class javaClass : classes) {
            classCollection.add(new JavaClassImpl(javaClass));
        }
        return classCollection;
    }

    public JavaField getDeclaredField(String arg0) {
        try {
            return new JavaFieldImpl(jClass.getDeclaredField(arg0));
        } catch (NoSuchFieldException nsfe) {
            // TODO: should we return an empty field here, throw an exception, or return null?
            return null;
        }
    }

    public Collection getDeclaredFields() {
        ArrayList<JavaField> fieldCollection = new ArrayList<JavaField>();
        Field[] fields = jClass.getDeclaredFields();
        for (Field field : fields) {
            fieldCollection.add(new JavaFieldImpl(field));
        }
        return fieldCollection;
    }

    /**
     * Assumes JavaType[] contains JavaClassImpl instances
     */
    public JavaMethod getDeclaredMethod(String arg0, JavaClass[] arg1) {
        if (arg1 == null) {
            arg1 = new JavaClass[0];
        }
        Class[] params = new Class[arg1.length];
        for (int i=0; i<arg1.length; i++) {
            JavaClass jType = arg1[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            return new JavaMethodImpl(jClass.getDeclaredMethod(arg0, params));
        } catch (NoSuchMethodException nsme) {
            // TODO: should we return an empty method here, throw an exception, or return null?
            return null;
        }
    }

    public Collection getDeclaredMethods() {
        ArrayList<JavaMethod> methodCollection = new ArrayList<JavaMethod>();
        Method[] methods = jClass.getDeclaredMethods();
        for (Method method : methods) {
            methodCollection.add(new JavaMethodImpl(method));
        }
        return methodCollection;
    }

    public JavaField getField(String arg0) {
        try { 
            return new JavaFieldImpl(jClass.getField(arg0));
        } catch (NoSuchFieldException nsfe) {
            // TODO: should we return an empty field here, throw an exception, or return null?
            return null;
        }
    }

    public Collection getFields() {
        ArrayList<JavaField> fieldCollection = new ArrayList<JavaField>();
        Field[] fields = jClass.getFields();
        for (Field field : fields) {
            fieldCollection.add(new JavaFieldImpl(field));
        }
        return fieldCollection;
    }

    public Class getJavaClass() {
        return jClass;
    }
    
    /**
     * Assumes JavaType[] contains JavaClassImpl instances
     */
    public JavaMethod getMethod(String arg0, JavaClass[] arg1) {
        if (arg1 == null) {
            arg1 = new JavaClass[0];
        }
        Class[] params = new Class[arg1.length];
        for (int i=0; i<arg1.length; i++) {
            JavaClass jType = arg1[i];
            if (jType != null) {
                params[i] = ((JavaClassImpl) jType).getJavaClass();
            }
        }
        try {
            return new JavaMethodImpl(jClass.getMethod(arg0, params));
        } catch (NoSuchMethodException nsme) {
            // TODO: should we return an empty method here, throw an exception, or return null?
            return null;
        }
    }

    public Collection getMethods() {
        ArrayList<JavaMethod> methodCollection = new ArrayList<JavaMethod>();
        Method[] methods = jClass.getMethods();
        for (Method method : methods) {
            methodCollection.add(new JavaMethodImpl(method));
        }
        return methodCollection;
    }

    public String getName() {
        return jClass.getName();
    }

    public JavaPackage getPackage() {
        return new JavaPackageImpl(jClass.getPackage());
    }

    public String getPackageName() {
        return jClass.getPackage().getName();
    }

    public String getQualifiedName() {
        return jClass.getName();
    }

    public String getRawName() {
        return jClass.getCanonicalName();
    }

    public JavaClass getSuperclass() {
        return new JavaClassImpl(jClass.getSuperclass());
    }

    public boolean hasActualTypeArguments() {
        if (jType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) jType;
            if (pType.getActualTypeArguments() != null && pType.getActualTypeArguments().length > 0) {
                return true;
            }
        }
        return false;
    }

    public JavaClass getOwningClass() {
        return new JavaClassImpl(jClass.getEnclosingClass());
    }

    public boolean isAnnotation() {
        return jClass.isAnnotation();
    }

    public boolean isArray() {
        return jClass.isArray();
    }

    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public boolean isAssignableFrom(JavaClass arg0) {
        return jClass.isAssignableFrom(((JavaClassImpl) arg0).getJavaClass());
    }

    public boolean isEnum() {
        return jClass.isEnum();
    }

    public boolean isInterface() {
        return jClass.isInterface();
    }

    public boolean isMemberClass() {
        return jClass.isMemberClass();
    }

    public boolean isPrimitive() {
        return jClass.isPrimitive();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public int getModifiers() {
        return jClass.getModifiers();
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isSynthetic() {
        return jClass.isSynthetic();
    }

//---------------- unimplemented methods ----------------//
    public JavaClass getComponentType() {
        return null;
    }

    public JavaAnnotation getDeclaredAnnotation(JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }    
}
