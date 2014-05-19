// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A TopLink JAXB 2.0 Java model representation of a JDK Method.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide information about a given implementations underlying method, such 
 * as name, parameters, modifiers, annotations, etc.</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaHasAnnotations
 * @see java.lang.reflect.Method
 */
public interface JavaMethod extends JavaHasAnnotations {
    public int getModifiers();
    public String getName();
    public JavaClass getOwningClass();
    public JavaClass[] getParameterTypes();
    public JavaClass getReturnType();
    public boolean isAbstract();
    public boolean isFinal();
    public boolean isPrivate();
    public boolean isProtected();
    public boolean isPublic();
    public boolean isStatic();
    public boolean isSynthetic();
}
