// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel.jot;

import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaModel;
import oracle.toplink.ox.jaxb.javamodel.JavaModelInput;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide access to an array of JavaClass instances 
 * and their associated JavaModel.  This class will transform an array 
 * of JOT JavaClass objects to an array of JavaClasses.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Create an array of JavaClass instances from an array of JOT JavaClasses</li>
 * <li>Return the array of JavaClass objects to be used by the generator</li>
 * <li>Return the JavaModel to be used during generation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.javatools.parser.java.v2.model.JavaClass
 * @see oracle.jdeveloper.java.JavaModel
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaClass 
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaModel 
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaModelInput 
 */
public class JavaModelInputImpl implements JavaModelInput {
    private JavaClass[] jClasses;
    private JavaModel jModel;
    
    public JavaModelInputImpl(oracle.javatools.parser.java.v2.model.JavaClass[] javaClasses, oracle.jdeveloper.java.JavaModel javaModel) {
        // convert each oracle.javatools.parser.java.v2.model.JavaClass 
        // to an oracle.toplink.ox.jaxb20.javamodel.JavaClass
        jClasses = new JavaClass[javaClasses.length]; 
        for (int i=0; i<javaClasses.length; i++) {
            jClasses[i] = new JavaClassImpl(javaClasses[i]);
        }
        jModel = new JavaModelImpl(javaModel);
    }
    
    public JavaClass[] getJavaClasses() {
        return jClasses;
    }
    
    public JavaModel getJavaModel() {
        return jModel;
    }
}
