// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.converters;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.mappings.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.sessions.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Allows a class name to be converter to and from a new instance of the class.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class ClassInstanceConverter implements Converter {
    protected DatabaseMapping mapping;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public ClassInstanceConverter() {
    }

    /**
     * INTERNAL:
     * Convert the class name to a class, then create an instance of the class.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) {
        Object attributeValue = null;
        if (fieldValue != null) {
            Class attributeClass = (Class)((AbstractSession)session).getDatasourcePlatform().convertObject(fieldValue, ClassConstants.CLASS);
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        attributeValue = AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(attributeClass));
                    } catch (PrivilegedActionException exception) {
                        throw ConversionException.couldNotBeConverted(fieldValue, attributeClass, exception.getException());
                    }
                } else {
                    attributeValue = PrivilegedAccessHelper.newInstanceFromClass(attributeClass);
                }
            } catch (Exception exception) {
                throw ConversionException.couldNotBeConverted(fieldValue, attributeClass, exception);
            }
        }

        return attributeValue;
    }

    /**
     *  INTERNAL:
     *  Convert to the field class.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        if (attributeValue == null) {
            return null;
        }
        return attributeValue.getClass().getName();
    }

    /**
     * INTERNAL:
     * Set the mapping.
     */
    public void initialize(DatabaseMapping mapping, Session session) {
        this.mapping = mapping;
        // CR#... Mapping must also have the field classification.
        if (getMapping().isDirectToFieldMapping()) {
            AbstractDirectMapping directMapping = (AbstractDirectMapping)getMapping();

            // Allow user to specify field type to override computed value. (i.e. blob, nchar)
            if (directMapping.getFieldClassification() == null) {
                directMapping.setFieldClassification(ClassConstants.STRING);
            }
        }
    }

    /**
     * INTERNAL:
     * Return the mapping.
     */
    protected DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * INTERNAL:
     * If the converter converts the value to a non-atomic value, i.e.
     * a value that can have its' parts changed without being replaced,
     * then it must return false, serialization can be non-atomic.
     */
    public boolean isMutable() {
        return false;
    }
}