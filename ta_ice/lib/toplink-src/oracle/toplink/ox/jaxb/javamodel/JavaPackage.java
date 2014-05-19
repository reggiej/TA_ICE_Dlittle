// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A TopLink JAXB 2.0 Java model representation of a JDK Package.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide the qualified name of the underlying package object</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaHasAnnotations
 * @see java.lang.Package
 */
public abstract interface JavaPackage extends JavaHasAnnotations {
    public String getQualifiedName();
}
