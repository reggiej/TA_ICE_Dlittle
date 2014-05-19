// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.converters;

import java.io.*;
import oracle.toplink.mappings.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.sessions.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The serialized object converter can be used to store an arbitrary object or set of objects into a database blob field.
 * It uses the Java serializer so the target must be serializable.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class SerializedObjectConverter implements Converter {
    protected DatabaseMapping mapping;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public SerializedObjectConverter() {
    }

    /**
     * PUBLIC:
     * Default constructor.
     */
    public SerializedObjectConverter(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     * The fieldValue will be a byte array.  Create a ByteArrayInputStream
     * on the fieldValue.  Create an ObjectInputStream on the ByteArrayInputStream
     * to read in the objects.
     */
    public Object convertDataValueToObjectValue(Object fieldValue, Session session) throws DescriptorException {
        byte[] bytes;
        try {
            bytes = (byte[])((AbstractSession)session).getDatasourcePlatform().convertObject(fieldValue, ClassConstants.APBYTE);
        } catch (ConversionException e) {
            throw ConversionException.couldNotBeConverted(mapping, mapping.getDescriptor(), e);
        }

        if ((bytes == null) || (bytes.length == 0)) {
            return null;
        }
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        Object object = null;
        try {
            // BUG# 2813583
            CustomObjectInputStream objectIn = new CustomObjectInputStream(byteIn, session);
            object = objectIn.readObject();
        } catch (Exception exception) {
            throw DescriptorException.notDeserializable(getMapping(), exception);
        }

        return object;
    }

    /**
     *  INTERNAL:
     *  Convert the object to a byte array through serialize.
     */
    public Object convertObjectValueToDataValue(Object attributeValue, Session session) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(attributeValue);
            objectOut.flush();
        } catch (IOException exception) {
            throw DescriptorException.notSerializable(getMapping(), exception);
        }
        return byteOut.toByteArray();
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
                directMapping.setFieldClassification(ClassConstants.APBYTE);
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
        return true;
    }
}