// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.compiler;

import java.util.HashMap;
import javax.xml.namespace.QName;
import oracle.toplink.ox.jaxb.javamodel.Helper;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>A specialized TypeInfo that stores additional information for a 
 * Java 5 Enumeration type. 
 * <p><b>Responsibilities:</b><ul>
 * <li>Hold onto the restriction base type for schema generation</li>
 * <li>Hold onto a map of Object Enum values to String values for Mapping generation</li>
 * </ul>
 * 
 * @see oracle.toplink.ox.jaxb.compiler.TypeInfo
 * @see oracle.toplink.ox.jaxb.AnnotationsProcessor
 * @author mmacivor
 *
 */
public class EnumTypeInfo extends TypeInfo {
    private String m_className;
    private QName m_restrictionBase;
    private HashMap<Object, String> m_objectValuesToFieldValues;
    
    public EnumTypeInfo(Helper helper) {
        super(helper);
    }
    
    public String getClassName() {
        return m_className;
    }
    
    public void setClassName(String className) {
        m_className = className;
    }
    
    public QName getRestrictionBase() {
        return m_restrictionBase;
    }
    
    public void setRestrictionBase(QName restrictionBase) {
        m_restrictionBase = restrictionBase;
    }
    
    public HashMap<Object, String> getObjectValuesToFieldValues() {
        if(m_objectValuesToFieldValues == null) {
            m_objectValuesToFieldValues = new HashMap<Object, String>();
        }
        return m_objectValuesToFieldValues;
    }
    
    public boolean isEnumerationType() {
        return true;
    }
}
