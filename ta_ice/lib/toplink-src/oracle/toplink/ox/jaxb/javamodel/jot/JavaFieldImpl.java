// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.javatools.parser.java.v2.model.JavaAnnotation;
import oracle.javatools.parser.java.v2.model.JavaClass;
import oracle.javatools.parser.java.v2.model.JavaField;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JOT JavaField. Method calls
 * are redirected to  the underlying JOT JavaField - JOT types are
 * converted to TopLink JAXB 2.0 Java model types when required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying field's name, type, 
 * modifiers, annotations, etc.</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaField
 * @see oracle.javatools.parser.java.v2.model.JavaField
 */
public class JavaFieldImpl implements oracle.toplink.ox.jaxb.javamodel.JavaField {
    protected JavaField jField;
    
    public JavaFieldImpl(JavaField javaField) {
        jField = javaField;
    }
    
    public JavaField getJavaField() {
        return jField;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        JavaAnnotation ja = jField.getAnnotation(((JavaClassImpl)arg0).getJavaClass());
        if (ja == null)  {
            return null;
        }
        return new JavaAnnotationImpl(ja);
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotationImpl> annotationCollection = new ArrayList<JavaAnnotationImpl>();
        Collection<JavaAnnotation> annotations = jField.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public int getModifiers() {
        return jField.getModifiers();
    }

    public String getName() {
        return jField.getName();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getOwningClass() {
        return new JavaClassImpl(jField.getOwningClass());
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getResolvedType() {
        try {
            return new JavaClassImpl((JavaClass)jField.getResolvedType());
        } catch (Exception x) {}
        return null;
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isSynthetic() {
        return jField.isSynthetic();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }
    
//  ---------------- unused methods ----------------//
    public boolean isEnumConstant() {
        return jField.isEnumConstant();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getDeclaredAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }
}
