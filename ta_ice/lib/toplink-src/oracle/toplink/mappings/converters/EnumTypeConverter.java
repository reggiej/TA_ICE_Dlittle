// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings.converters;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.EnumSet;
import java.util.Iterator;
import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;

/**
 * <b>Purpose</b>: Object type converter is used to match a fixed number of 
 * database data values to a Java enum object value. It can be used when the 
 * values on the database and in the Java differ. To create an object type 
 * converter, simply specify the set of conversion value pairs. A default value 
 * and one-way conversion are also supported for legacy data situations.
 *
 * @author Guy Pelletier
 * @since Toplink 10.1.4RI
 */
public class EnumTypeConverter extends ObjectTypeConverter {
    private Class m_enumClass;
    private String m_enumClassName;
    
    /**
     * PUBLIC:
     * Creating an enum converter this way will create the conversion values
     * for you using ordinal or name values.
     */
    public EnumTypeConverter(DatabaseMapping mapping, Class enumClass, boolean useOrdinalValues) {
        super(mapping);
        m_enumClassName = enumClass.getName();
        m_enumClass = enumClass;
        EnumSet theEnums = EnumSet.allOf(enumClass);
        Iterator<Enum> i = theEnums.iterator();
        
        while (i.hasNext()) {
            Enum theEnum = i.next();
            
            if (useOrdinalValues) {
                addConversionValue(theEnum.ordinal(), theEnum.name());
            } else {
                addConversionValue(theEnum.name(), theEnum.name());
            }
        }
    }
    
    /**
     * PUBLIC:
     * Creating an enum converter this way expects that you will provide
     * the conversion values separately.
     */
    public EnumTypeConverter(DatabaseMapping mapping, String enumClassName) {
        super(mapping);
        m_enumClassName = enumClassName;
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this converter to actual 
     * class-based settings. This method is used when converting a project 
     * that has been built with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    m_enumClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(m_enumClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(m_enumClassName, exception.getException());
                }
            } else {
                m_enumClass = oracle.toplink.internal.security.PrivilegedAccessHelper.getClassForName(m_enumClassName, true, classLoader);
            }
        } catch (ClassNotFoundException exception){
            throw ValidationException.classNotFoundWhileConvertingClassNames(m_enumClassName, exception);
        }
    }
    
    /**
     * INTERNAL:
     * Returns the corresponding attribute value for the specified field value.
     * Wraps the super method to return an Enum type from the string conversion.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object obj = super.convertDataValueToObjectValue(fieldValue, session);
        
        if (fieldValue == null || obj == null) {
            return obj;
        } else {
            return Enum.valueOf(m_enumClass, (String) obj);
        }
    }
    
    /**
     * INTERNAL:
     * Convert Enum object to the data value. Internal enums are stored as
     * strings (names) so this method wraps the super method in that if
     * breaks down the enum to a string name before converting it.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        if (attributeValue == null) {
            return super.convertObjectValueToDataValue(null, session);
        } else {
            return super.convertObjectValueToDataValue((Enum.class.cast(attributeValue)).name(), session);
        }
    }
}
