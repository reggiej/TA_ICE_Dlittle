// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

import java.util.Map;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A TopLink JAXB 2.0 Java model representation of a JDK Annotation.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide a map of components (declared members) for this annotation type</li>
 * </ul>
 *
 * @since Oracle TopLink 11.1.1.0.0
 * @see java.lang.annotation.Annotation
 * 
 */
public interface JavaAnnotation {
    public Map getComponents();
}
