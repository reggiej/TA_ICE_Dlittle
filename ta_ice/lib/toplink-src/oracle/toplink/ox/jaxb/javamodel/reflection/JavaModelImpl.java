// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.reflection;

import java.lang.annotation.Annotation;

import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaModel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>The JavaModel is the central access point to the TopLink
 * JAXB 2.0 Java model implementation's source/class files.  A JavaModel has 
 * an underlying source/classpath that defines its search path.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Return a JavaClass based on a Class or Class name</li>
 * <li>Return a JDK Annotation for a given JavaAnnotation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaModel
 */
public class JavaModelImpl implements JavaModel {
    public JavaModelImpl() {
    }
    
    public JavaClass getClass(Class jClass) {
        try {
            return new JavaClassImpl(jClass);
        } catch (Exception x) {
            return null;
        }
    }
    
    public JavaClass getClass(String classname) {
        try {
            return new JavaClassImpl(Class.forName(classname));
        } catch (Exception x) {
            return null;
        }
    }
    
    public ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    public Annotation getAnnotation(JavaAnnotation janno, Class jClass) {
        return ((JavaAnnotationImpl) janno).getJavaAnnotation();
    }
}