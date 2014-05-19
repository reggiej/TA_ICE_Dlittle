// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.javatools.parser.java.v2.model.JavaAnnotation;
import oracle.javatools.parser.java.v2.model.JavaClass;
import java.util.Map;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JOT JavaAnnotation.  This 
 * implementation of the TopLink JAXB 2.0 Java model redirects method
 * calls on the underlying JOT JavaAnnotation, converting JOT types
 * to TopLink JAXB 2.0 Java model types when required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide a map of components (declared members) for the underlying
 * JOT JavaAnnotation type</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaAnnotation
 * @see oracle.javatools.parser.java.v2.model.JavaAnnotation
 * @see oracle.javatools.parser.java.v2.model.JavaClass
 */
public class JavaAnnotationImpl implements oracle.toplink.ox.jaxb.javamodel.JavaAnnotation {
    JavaAnnotation jAnnotation;
    
    public JavaAnnotationImpl(JavaAnnotation javaAnnotation) {
        jAnnotation = javaAnnotation;
    }
    
    public JavaAnnotation getJavaAnnotation() {
        return jAnnotation;
    }
    
    public Map getComponents() {
        java.util.HashMap components = new java.util.HashMap();
        Map comps = jAnnotation.getComponents();
        java.util.Collection<Object> keys = comps.keySet();
        for (Object key : keys) {
            Object val = comps.get(key);
            JavaClass jClass;
            try {
                jClass = (JavaClass) val;
                components.put(key, new JavaClassImpl(jClass));
            } catch (Exception ex) {
                components.put(key, val);
            }
        }
        return components;
    }
}
