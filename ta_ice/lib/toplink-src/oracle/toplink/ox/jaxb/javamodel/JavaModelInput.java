// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide a pluggable method for implementations of the 
 * TopLink JAXB 2.0 Java model to be used with the TopLinkJAXB20Generator.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Return an array of JavaClass objects to be used by the generator</li>
 * <li>Return the JavaModel to be used during generation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaClass 
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaModel 
 */
public interface JavaModelInput {
    public JavaClass[] getJavaClasses();
    public JavaModel getJavaModel();
}