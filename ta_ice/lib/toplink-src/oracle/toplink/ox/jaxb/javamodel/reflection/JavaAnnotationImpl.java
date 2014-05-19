// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.reflection;

import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JDK Annotation.  This implementation
 * of the TopLink JAXB 2.0 Java model simply makes reflective calls on the 
 * underlying JDK object - in this case the Annotation itself is returned.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying JDK Annotation</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaAnnotation
 * @see java.lang.annotation.Annotation
 */
public class JavaAnnotationImpl implements JavaAnnotation {
    Annotation jAnnotation;
    
    public JavaAnnotationImpl(Annotation javaAnnotation) {
        jAnnotation = javaAnnotation;
    }
    
    public Annotation getJavaAnnotation() {
        return jAnnotation;
    }
    
//  ---------------- unimplemented methods ----------------//
    public Map getComponents() {
        // the reflection implementation uses the underlying 
        // annotation directly - not needed
        return null;
    }
}
