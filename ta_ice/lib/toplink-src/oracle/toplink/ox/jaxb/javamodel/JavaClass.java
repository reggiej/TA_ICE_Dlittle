// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

import java.util.Collection;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A TopLink JAXB 2.0 Java model representation of a JDK Class.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide information about a given implementation's underlying class, such 
 * as name, package, method/field names and parameters, annotations, etc.</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaHasAnnotations
 * @see java.lang.Class
 */
public interface JavaClass extends JavaHasAnnotations {
    public Collection getActualTypeArguments();
    public JavaClass getComponentType();
    public String getQualifiedName();
    public String getRawName();
    public boolean hasActualTypeArguments();
    public Collection getDeclaredClasses();
    public JavaField getDeclaredField(String arg0);
    public Collection getDeclaredFields();
    public JavaMethod getDeclaredMethod(String arg0, JavaClass[] arg1);
    public Collection getDeclaredMethods();
    public JavaMethod getMethod(String arg0, JavaClass[] arg1);
    public Collection getMethods();
    public int getModifiers();
    public String getName();
    public JavaPackage getPackage();
    public String getPackageName();
    public JavaClass getSuperclass();
    public boolean isAbstract();
    public boolean isAnnotation();
    public boolean isArray();
    public boolean isAssignableFrom(JavaClass arg0);
    public boolean isEnum();
    public boolean isFinal();
    public boolean isInterface();
    public boolean isMemberClass();
    public boolean isPrimitive();
    public boolean isPrivate();
    public boolean isProtected();
    public boolean isPublic();
    public boolean isStatic();
    public boolean isSynthetic();
}
