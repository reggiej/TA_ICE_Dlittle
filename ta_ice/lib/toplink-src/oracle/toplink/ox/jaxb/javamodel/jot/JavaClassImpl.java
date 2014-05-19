// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.javatools.parser.java.v2.model.JavaAnnotation;
import oracle.javatools.parser.java.v2.model.JavaClass;
import oracle.javatools.parser.java.v2.model.JavaField;
import oracle.javatools.parser.java.v2.model.JavaMethod;
import oracle.javatools.parser.java.v2.model.JavaPackage;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A wrapper class for a JOT JavaClass.  Method calls
 * are redirected to the underlying JOT JavaClass - JOT types are
 * converted to TopLink JAXB 2.0 Java model types when required.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide access to the underlying JOT JavaClass' name, package, 
 * method/field names and parameters, annotations, etc.</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.javatools.parser.java.v2.model.JavaClass
 * @see oracle.toplink.ox.jaxb.javamodel.JavaClass
 */
public class JavaClassImpl implements oracle.toplink.ox.jaxb.javamodel.JavaClass {
    protected JavaClass jClass;

    public JavaClassImpl(JavaClass javaClass) {
        jClass = javaClass;
    }
    
    public Collection getActualTypeArguments() {
        ArrayList<JavaClassImpl> typeCollection = new ArrayList<JavaClassImpl>();
        Collection<JavaClass> types = jClass.getActualTypeArguments();
        for (JavaClass type : types) {
            typeCollection.add(new JavaClassImpl(type));
        }
        return typeCollection;
    }

    public String toString() {
        return getName();
    }
    
    /**
     * Assumes JavaClass is a JavaClassImpl instance
     */
    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        JavaAnnotation ja = jClass.getAnnotation(((JavaClassImpl)arg0).getJavaClass());
        if (ja == null)  {
            return null;
        }
        return new JavaAnnotationImpl(ja);
    }

    public Collection getAnnotations() {
        ArrayList<JavaAnnotationImpl> annotationCollection = new ArrayList<JavaAnnotationImpl>();
        Collection<JavaAnnotation> annotations = jClass.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            annotationCollection.add(new JavaAnnotationImpl(annotation));
        }
        return annotationCollection;
    }

    public Collection getDeclaredClasses() {
        ArrayList<JavaClassImpl> classCollection = new ArrayList<JavaClassImpl>();
        Collection<JavaClass> classes = jClass.getDeclaredClasses();
        for (JavaClass javaClass : classes) {
            classCollection.add(new JavaClassImpl(javaClass));
        }
        return classCollection;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaField getDeclaredField(String arg0) {
        JavaField jfld = jClass.getDeclaredField(arg0);
        if (jfld == null) {
            return null;
        }
        return new JavaFieldImpl(jfld);
    }

    public Collection getDeclaredFields() {
        ArrayList<JavaFieldImpl> fieldCollection = new ArrayList<JavaFieldImpl>();
        Collection<JavaField> fields = jClass.getDeclaredFields();
        for (JavaField field : fields) {
            fieldCollection.add(new JavaFieldImpl(field));
        }
        return fieldCollection;
    }

    /**
     * Assumes JavaClass[] contains JavaClassImpl instances
     */
    public oracle.toplink.ox.jaxb.javamodel.JavaMethod getDeclaredMethod(String name, oracle.toplink.ox.jaxb.javamodel.JavaClass[] params) {
        // convert each oracle.toplink.ox.jaxb.javamodel.JavaClass 
        // to an oracle.javatools.parser.java.v2.model.JavaClass
        JavaClass[] jci = new JavaClass[params.length]; 
        for (int i=0; i<params.length; i++) {
            jci[i] = ((JavaClassImpl)params[i]).getJavaClass();
        }
        
        JavaMethod jm = jClass.getDeclaredMethod(name, jci);
        if (jm == null) {
            return null;
        }
        return new JavaMethodImpl(jm);
    }

    public Collection getDeclaredMethods() {
        ArrayList<JavaMethodImpl> methodCollection = new ArrayList<JavaMethodImpl>();
        Collection<JavaMethod> methods = jClass.getDeclaredMethods();
        for (JavaMethod method : methods) {
            methodCollection.add(new JavaMethodImpl(method));
        }
        return methodCollection;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaField getField(String arg0) {
        JavaField jf = jClass.getField(arg0);
        if (jf == null) {
            return null;
        }
        return new JavaFieldImpl(jf);
    }

    public Collection getFields() {
        ArrayList<JavaFieldImpl> fieldCollection = new ArrayList<JavaFieldImpl>();
        Collection<JavaField> fields = jClass.getFields();
        for (JavaField field : fields) {
            fieldCollection.add(new JavaFieldImpl(field));
        }
        return fieldCollection;
    }

    public JavaClass getJavaClass() {
        return jClass;
    }
    
    /**
     * Assumes JavaClass[] contains JavaClassImpl instances
     */
    public oracle.toplink.ox.jaxb.javamodel.JavaMethod getMethod(String name, oracle.toplink.ox.jaxb.javamodel.JavaClass[] params) {
        // convert each oracle.toplink.ox.jaxb.javamodel.JavaClass 
        // to an oracle.javatools.parser.java.v2.model.JavaClass
        JavaClass[] jci = new JavaClass[params.length]; 
        for (int i=0; i<params.length; i++) {
            jci[i] = ((JavaClassImpl)params[i]).getJavaClass();
        }
        
        JavaMethod jm = jClass.getMethod(name, jci);
        if (jm == null) {
            return null;
        }
        return new JavaMethodImpl(jm);
    }

    public Collection getMethods() {
        ArrayList<JavaMethodImpl> methodCollection = new ArrayList<JavaMethodImpl>();
        Collection<JavaMethod> methods = jClass.getMethods();
        for (JavaMethod method : methods) {
            methodCollection.add(new JavaMethodImpl(method));
        }
        return methodCollection;
    }

    public String getName() {
        return jClass.getName();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaPackage getPackage() {
        JavaPackage jp = jClass.getPackage();
        if (jp == null) {
            return null;
        }
        return new JavaPackageImpl(jp);
    }

    public String getPackageName() {
        return jClass.getPackage().getName();
    }

    public String getQualifiedName() {
        return jClass.getName();
    }

    public String getRawName() {
        return jClass.getRawName();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getSuperclass() {
        return new JavaClassImpl((JavaClass)jClass.getSuperclass());
    }

    public boolean hasActualTypeArguments() {
        return jClass.hasActualTypeArguments();
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaClass getOwningClass() {
        return new JavaClassImpl(jClass.getOwningClass());
    }

    public boolean isAnnotation() {
        return jClass.isAnnotation();
    }

    public boolean isArray() {
        return jClass.isArray();
    }

    /**
     * Assumes JavaType is a JavaClassImpl instance
     */
    public boolean isAssignableFrom(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        return jClass.isAssignableFrom(((JavaClassImpl) arg0).getJavaClass());
    }

    public boolean isEnum() {
        return jClass.isEnum();
    }

    public boolean isInterface() {
        return jClass.isInterface();
    }

    public boolean isMemberClass() {
        return jClass.isMemberClass();
    }

    public boolean isPrimitive() {
        return jClass.isPrimitive();
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

    public int getModifiers() {
        return jClass.getModifiers();
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isSynthetic() {
        return jClass.isSynthetic();
    }

//---------------- unimplemented methods ----------------//
    public oracle.toplink.ox.jaxb.javamodel.JavaClass getComponentType() {
        return null;
    }

    public oracle.toplink.ox.jaxb.javamodel.JavaAnnotation getDeclaredAnnotation(oracle.toplink.ox.jaxb.javamodel.JavaClass arg0) {
        return null;
    }

    public Collection getDeclaredAnnotations() {
        return null;
    }    
}
