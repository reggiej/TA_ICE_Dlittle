// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import java.util.*;
import oracle.toplink.sessions.Session;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.mappings.converters.ObjectTypeConverter;
import oracle.toplink.internal.security.PrivilegedAccessHelper;

/**
 * INTERNAL:
 * <p><b>Purpose</b>:Provide a means to Convert an Enumeration type to/from either a string representation
 * of the enum facet or a user defined value. 
 * 
 * <p><b>Responsibilities:</b><ul>
 * <li>Initialize the conversion values to be the Enum facets</li>
 * <li>Don't overwrite any existing, user defined conversion value</li>
 * 
 */
public class JAXBEnumTypeConverter extends ObjectTypeConverter {
    private Class m_enumClass;
    private String m_enumClassName;
	private boolean m_usesOrdinalValues;

    /**
     * PUBLIC:
     */
    public JAXBEnumTypeConverter(DatabaseMapping mapping, String enumClassName, boolean usesOrdinalValues) {
        super(mapping);

        m_enumClassName = enumClassName;
		m_usesOrdinalValues = usesOrdinalValues;
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this converter to actual 
     * class-based settings. This method is used when converting a project 
     * that has been built with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(){
        try {
            m_enumClass = PrivilegedAccessHelper.getClassForName(m_enumClassName);
        } catch (ClassNotFoundException exception){
            //throw new ValidationException(exception.getMessage(), exception);
        }
    }
    
    /**
     * INTERNAL:
     */
    public void initialize(DatabaseMapping mapping, Session session) {
        convertClassNamesToClasses();
        
        Iterator<Enum> i = EnumSet.allOf(m_enumClass).iterator();
        while (i.hasNext()) {
            Enum theEnum = i.next();
            if (this.getAttributeToFieldValues().get(theEnum) == null) {
                // TODO:  verify this workaround for JOT implementation
                // - we may have set the name as opposed to the actual object,
                // if so, look for the name, and replace the object as required
                Object existingVal = this.getAttributeToFieldValues().get(theEnum.name()); 
                if (existingVal != null) {
                    this.getAttributeToFieldValues().remove(theEnum.name());
                    addConversionValue(existingVal, theEnum);
                } else {
                    // if there's no user defined value, create a default
                    if (m_usesOrdinalValues) {
                        addConversionValue(theEnum.ordinal(), theEnum);
                    } else {
                        addConversionValue(theEnum.name(), theEnum);
                    }
                }
            }
        }
        
        super.initialize(mapping, session);
    }
    
    /**
     * PUBLIC:
     * Returns true if this converter uses ordinal values for the enum
     * conversion.
     */
	public boolean usesOrdinalValues() {
		return m_usesOrdinalValues;   
    }
}
