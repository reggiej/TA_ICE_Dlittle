// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A superinterface for those interfaces which represent 
 * JDK Annotations.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Ensure that subinterfaces define methods for accessing both inherited
 * and direct annotations present on a given JavaAnnotation</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see java.lang.annotation.Annotation
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaAnnotation
 */
public interface JavaHasAnnotations {
    public JavaAnnotation getAnnotation(JavaClass arg0);
    public Collection getAnnotations();
    public JavaAnnotation getDeclaredAnnotation(JavaClass arg0);
    public Collection getDeclaredAnnotations();
}
