// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.reflection;

import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaModel;
import oracle.toplink.ox.jaxb.javamodel.JavaModelInput;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide access to an array of JavaClass instances 
 * and their associated JavaModel.  This class will transform an array 
 * of Class objects to an array of JavaClasses.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Create an array of JavaClass instances from an array of Classes</li>
 * <li>Return an array of JavaClass objects to be used by the generator</li>
 * <li>Return the JavaModel to be used during generation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.javamodel.JavaClass 
 * @see oracle.toplink.ox.jaxb.javamodel.JavaModel 
 * @see oracle.toplink.ox.jaxb.javamodel.JavaModelInput 
 */
public class JavaModelInputImpl implements JavaModelInput {
    private JavaClass[] jClasses;
    private JavaModel jModel;
    
    public JavaModelInputImpl(Class[] classes, JavaModel javaModel) {
        jClasses = new JavaClass[classes.length];
        for (int i=0; i<classes.length; i++) {
            jClasses[i] = new JavaClassImpl(classes[i]);            
        }
        jModel = javaModel;
    }
    
    public JavaClass[] getJavaClasses() {
        return jClasses;
    }
    
    public JavaModel getJavaModel() {
        return jModel;
    }
}