// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>The JavaModel is the central access point to the TopLink
 * JAXB 2.0 Java model implementation's source/classes.  A JavaModel has an 
 * underlying source/classpath that defines its search path.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Return a JavaClass based on a Class or Class name</li>
 * <li>Return a JDK Annotation for a given JavaAnnotation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 */
public interface JavaModel {
    public JavaClass getClass(Class jClass);
    public JavaClass getClass(String classname);
    public ClassLoader getClassLoader();
    public java.lang.annotation.Annotation getAnnotation(JavaAnnotation janno, Class jClass);
}
