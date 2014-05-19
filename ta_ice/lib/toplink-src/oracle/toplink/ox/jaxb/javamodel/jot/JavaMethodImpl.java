// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.javatools.parser.java.v2.model.JavaAnnotation;
import oracle.javatools.parser.java.v2.model.JavaClass;
import oracle.javatools.parser.java.v2.model.JavaMethod;
import oracle.javatools.parser.java.v2.model.JavaType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper for a JOT JavaMethod.  Method calls 
 * are redirected to the underlying JOT JavaMethod - JOT types are 
 * converted to TopLink JAXB 2.0 Java model types when required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying method's name, parameters, 
 * modifiers, annotations, etc.</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaMethod
 * @see oracle.javatools.parser.java.v2.model.JavaMethod
 */
public class JavaMethodImpl implements oracle.toplink.ox.jaxb.javamodel.JavaMethod {
    protected JavaMethod jMethod;
    
    public JavaMethodImpl(JavaMethod javaMethod) {
        jMethod = javaMethod;
    }

    public Collection getActualTypeArguments() {
        ArrayList<JavaClassImpl> typeCollection = new ArrayList<JavaClassImpl>();
        Collection<JavaClass> types = jMethod.getActualTypeArguments();
        for (JavaClass type : types) {
            typeCollection.add(new JavaClassImpl(type));
        }
        return typeCollection;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        JavaAnnotation ja = jMethod.getAnnotation(((JavaClassImpl)arg0).getJavaClass());
        if (ja == null) {
            return null;
        }
        return new JavaAnnotationImpl(ja);
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotationImpl> annotationCollection = new ArrayList<JavaAnnotationImpl>();
        Collection<JavaAnnotation> annotations = jMethod.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public String getName() {
        return jMethod.getName();
    }
    
    public JavaMethod getJavaMethod() {
        return jMethod;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass[] getParameterTypes() {
        // TODO:  here we are assuming that the types are JavaClass - could
        // be something else - may need to support JavaType...
        JavaType[] params = jMethod.getParameterTypes();
        oracle.toplink.ox.jaxb.javamodel.JavaClass[] paramArray = new oracle.toplink.ox.jaxb.javamodel.JavaClass[params.length];
        for (int i=0; i<params.length; i++) {
            paramArray[i] = new JavaClassImpl((JavaClass)params[i]);
        }
        return paramArray;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getResolvedType() {
        return new JavaClassImpl((JavaClass)jMethod.getResolvedType());
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getReturnType() {
        return new JavaClassImpl((JavaClass)jMethod.getReturnType());
    }

    public boolean hasActualTypeArguments() {
        return jMethod.hasActualTypeArguments();
    }

    public int getModifiers() {
        return jMethod.getModifiers();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getOwningClass() {
        return new JavaClassImpl(jMethod.getOwningClass());
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

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isSynthetic() {
        return jMethod.isSynthetic();
    }

//  ---------------- unimplemented methods ----------------//
    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getDeclaredAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }
}
