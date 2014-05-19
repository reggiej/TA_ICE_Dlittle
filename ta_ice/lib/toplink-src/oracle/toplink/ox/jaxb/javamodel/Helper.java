// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.javamodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;

import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.jaxb.javamodel.JavaAnnotation;
import oracle.toplink.ox.jaxb.javamodel.JavaClass;
import oracle.toplink.ox.jaxb.javamodel.JavaField;
import oracle.toplink.ox.jaxb.javamodel.JavaHasAnnotations;
import oracle.toplink.ox.jaxb.javamodel.JavaMethod;
import oracle.toplink.ox.jaxb.javamodel.JavaModel;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>To provide helper methods and constants to assist
 * in integrating TopLink JAXB 2.0 Generation with the JDEV JOT APIs.
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Make available a map of JOT - XML type pairs</li>
 * <li>Redirect method calls to the current JavaModel implementation as 
 * required</li>
 * <li>Provide methods for accessing generics, annotations, etc. on a
 * given implementaiton's classes</li>
 * <li>Provide a dynamic proxy instance for a given JavaAnnotation in 
 * the JOT implementation (for reflection a Java SDK annotation is 
 * returned)</li>
 * </ul>
 *  
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb20.javamodel.JavaModel
 * @see oracle.toplink.ox.jaxb20.javamodel.jot.AnnotationProxy
 *
 */
public class Helper {
    protected ClassLoader loader;
    protected JavaModel jModel;
    private HashMap xmlToJavaTypeMap;
    
    public final static String APBYTE = "byte[]";
    public final static String BIGDECIMAL = "java.math.BigDecimal";
    public final static String BIGINTEGER = "java.math.BigInteger";
    public final static String PBOOLEAN = "boolean";
    public final static String PBYTE = "byte";
    public final static String CALENDAR = "java.util.Calendar";
    public final static String PDOUBLE = "double";
    public final static String PFLOAT = "float";
    public final static String PINT = "int";
    public final static String PLONG = "long";
    public final static String PSHORT = "short";
    public final static String QNAME_CLASS = "javax.xml.namespace.QName";
    public final static String STRING = "java.lang.String";
    public final static String ABYTE = "java.lang.Byte[]";
    public final static String BOOLEAN = "java.lang.Boolean";
    public final static String BYTE = "java.lang.Byte";
    public final static String GREGORIAN_CALENDAR = "java.util.GregorianCalendar";
    public final static String DOUBLE = "java.lang.Double";
    public final static String FLOAT = "java.lang.Float";
    public final static String INTEGER = "java.lang.Integer";
    public final static String LONG = "java.lang.Long";
    public final static String SHORT = "java.lang.Short";

    /**
     * INTERNAL:
     * This is the preferred constructor.
     * 
     * This constructor builds the map of XML-Java type pairs,
     * and sets the JavaModel and ClassLoader.
     * 
     * @param model
     */
    public Helper(JavaModel model) {
        buildXMLToJavaTypeMap();
        setJavaModel(model);
        setClassLoader(model.getClassLoader());
    }

    /**
     * Builds a map of Java types to XML types.
     * 
     * @return
     */
    private HashMap buildXMLToJavaTypeMap() {
        HashMap javaTypes = new HashMap();
        // jaxb 1.0 spec pairs        
        javaTypes.put(APBYTE, XMLConstants.HEX_BINARY_QNAME);
        javaTypes.put(BIGDECIMAL, XMLConstants.DECIMAL_QNAME);
        javaTypes.put(BIGINTEGER, XMLConstants.INTEGER_QNAME);
        javaTypes.put(PBOOLEAN, XMLConstants.BOOLEAN_QNAME);
        javaTypes.put(PBYTE, XMLConstants.BYTE_QNAME);
        javaTypes.put(CALENDAR, XMLConstants.DATE_TIME_QNAME);
        javaTypes.put(PDOUBLE, XMLConstants.DOUBLE_QNAME);
        javaTypes.put(PFLOAT, XMLConstants.FLOAT_QNAME);
        javaTypes.put(PINT, XMLConstants.INT_QNAME);
        javaTypes.put(PLONG, XMLConstants.LONG_QNAME);
        javaTypes.put(PSHORT, XMLConstants.SHORT_QNAME);
        javaTypes.put(QNAME_CLASS, XMLConstants.QNAME_QNAME);
        javaTypes.put(STRING, XMLConstants.STRING_QNAME);
        // other pairs
        javaTypes.put(ABYTE, XMLConstants.HEX_BINARY_QNAME);
        javaTypes.put(BOOLEAN, XMLConstants.BOOLEAN_QNAME);
        javaTypes.put(BYTE, XMLConstants.BYTE_QNAME);
        javaTypes.put(GREGORIAN_CALENDAR, XMLConstants.DATE_TIME_QNAME);
        javaTypes.put(DOUBLE, XMLConstants.DOUBLE_QNAME);
        javaTypes.put(FLOAT, XMLConstants.FLOAT_QNAME);
        javaTypes.put(INTEGER, XMLConstants.INT_QNAME);
        javaTypes.put(LONG, XMLConstants.LONG_QNAME);
        javaTypes.put(SHORT, XMLConstants.SHORT_QNAME);
        return javaTypes;
    }
    
    public JavaAnnotation getAnnotaion(JavaHasAnnotations element, Class annotationClass) {
        return element.getAnnotation(jModel.getClass(annotationClass));
    }
    
    /**
     * Return a given field's generic type.
     * 
     * @param field
     * @return
     */
    public JavaClass getGenericType(JavaField field) {
        JavaClass result = (JavaClass) field.getResolvedType();
        JavaClass jClass = null;
        
        if (result.hasActualTypeArguments()) {
            ArrayList typeArgs =  (ArrayList) result.getActualTypeArguments();
            jClass = (JavaClass) typeArgs.get(0);
        }
        return jClass;
    }
    
    /**
     * Return a given method's generic return type as a JavaClass.
     * 
     * @param meth
     * @return
     */
    public JavaClass getGenericReturnType(JavaMethod meth) {
        JavaClass result = meth.getReturnType();
        JavaClass jClass = null;
        if (result == null) { return null; }
        
        if (result.hasActualTypeArguments()) {
            ArrayList typeArgs =  (ArrayList) result.getActualTypeArguments();
            jClass = (JavaClass) typeArgs.get(0);
        }
        return jClass;
    }

    /**
     * Return a JavaClass instance created based the provided class.
     * This assumes that the provided class exists on the classpath 
     * - null is returned otherwise.
     * 
     * @param javaClass
     * @return
     */
    public JavaClass getJavaClass(Class javaClass) {
        return jModel.getClass(javaClass);
    }
    
    /**
     * Return a JavaClass instance created based on fully qualified
     * class name.  This assumes that a class with the provided name
     * exists on the classpath - null is returned otherwise.
     * 
     * @param javaClassName
     * @return
     */
    public JavaClass getJavaClass(String javaClassName) {
        return jModel.getClass(javaClassName);
    }
    
    /**
     * Return a map of default Java types to XML types.
     * @return
     */
    public HashMap getXMLToJavaTypeMap() {
        if (xmlToJavaTypeMap == null) {
            xmlToJavaTypeMap = buildXMLToJavaTypeMap();
        }
        return xmlToJavaTypeMap;
    }

    /**
     * Returns a either a dynamic proxy instance that allows an element 
     * to be treated as an annotation (for JOT), or a Java annotation 
     * (for Reflection), or null if the specified annotation does not 
     * exist.  
     * Intended to be used in conjunction with isAnnotationPresent.
     *  
     * @param element
     * @param annotationClass
     * @return
     * @see isAnnotationPresent
     */
    public Annotation getAnnotation(JavaHasAnnotations element, Class annotationClass) {
        JavaAnnotation janno = getAnnotaion(element, annotationClass);
        if (janno == null) {
            return null;
        }
        return jModel.getAnnotation(janno, annotationClass);
    }
    
    /**
     * Returns a JavaClass instance wrapping the provided field's resolved
     * type.
     * 
     * @param field
     * @return
     */
    public JavaClass getType(JavaField field) {
        JavaClass type = (JavaClass) field.getResolvedType();
        try {
            return jModel.getClass(type.getRawName());
        } catch (Exception x) {}
        return null;
    }

    /**
     * Indicates if element contains a given annotation.
     * 
     * @param element
     * @param annotationClass
     * @return
     */
    public boolean isAnnotationPresent(JavaHasAnnotations element, Class annotationClass) {
        if(element == null || annotationClass == null) {
            return false;
        }
        return (this.getAnnotaion(element, annotationClass) != null);
    }

    /**
     * Indicates if the javaType map contains a key equal to
     * the provided JavaClass' raw name.
     * 
     * @param jClass
     * @return
     */
    public boolean isBuiltInJavaType(JavaClass jClass) {
        return getXMLToJavaTypeMap().containsKey(jClass.getRawName());
    }

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }
    
    public void setJavaModel(JavaModel model) {
        jModel = model;
    }
}
