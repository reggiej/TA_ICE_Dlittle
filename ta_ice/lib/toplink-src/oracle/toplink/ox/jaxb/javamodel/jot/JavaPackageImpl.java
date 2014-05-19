// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.javatools.parser.java.v2.model.JavaAnnotation;
import oracle.javatools.parser.java.v2.model.JavaPackage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JOT JavaPackage.  Method
 * calls are redirected to the underlying JOT JavaPackage - JOT types
 * are converted to TopLink JAXB 2.0 Java model types when required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying package's qualified name, 
 * annotations, etc.</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaPackage
 * @see oracle.javatools.parser.java.v2.model.JavaPackage
 */
public class JavaPackageImpl implements oracle.toplink.ox.jaxb.javamodel.JavaPackage {
    protected JavaPackage jPkg;
    
    public JavaPackageImpl(JavaPackage javaPackage) {
        jPkg = javaPackage;
    }
    
    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        JavaAnnotation ja = jPkg.getAnnotation(((JavaClassImpl)arg0).getJavaClass());
        if (ja == null)  {
            return null;
        }
        return new JavaAnnotationImpl(ja);
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotationImpl> annotationCollection = new ArrayList<JavaAnnotationImpl>();
        Collection<JavaAnnotation> annotations = jPkg.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public String getName() {
        return jPkg.getName();
    }

    public String getQualifiedName() {
        return jPkg.getName();
    }

//  ---------------- unimplemented methods ----------------//
    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getDeclaredAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }
}
