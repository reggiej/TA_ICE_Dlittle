// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import java.lang.annotation.Annotation;

import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import oracle.toplink.ox.jaxb.javamodel.JavaModel;
import oracle.toplink.ox.jaxb.javamodel.JavaClass;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>The JavaModel is the central access point to the TopLink 
 * JAXB 2.0 Java model implementation's source/class files.  A JavaModel has 
 * an underlying source/classpath that defines its search path.
 * 
 * Method calls are redirected to the underlying JOT JavaModel - JOT types 
 * are converted to TopLink JAXB 2.0 Java model types as required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Return a JavaClass based on a Class or Class name</li>
 * <li>Return an AnnotationProxy for a given JavaAnnotation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.jdeveloper.java.JavaModel
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaModel
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaClass
 * @see oracle.toplink.ox.jaxb20.javamodel.jot.AnnotationProxy
 */
public class JavaModelImpl implements JavaModel {
    protected oracle.jdeveloper.java.JavaModel jModel;
    
    public JavaModelImpl(oracle.jdeveloper.java.JavaModel javaModel) {
        jModel = javaModel;
    }
    
    public JavaClass getClass(Class jClass) {
        return new JavaClassImpl(jModel.getClass(jClass.getName()));
    }
    
    public JavaClass getClass(String classname) {
        return new JavaClassImpl(jModel.getClass(classname));
    }
    
    public ClassLoader getClassLoader() {
        return jModel.getClassLoader();
    }
    
    public Annotation getAnnotation(JavaAnnotation janno, Class jClass) {
        return AnnotationProxy.getProxy(janno, jClass, getClassLoader());
    }
}
