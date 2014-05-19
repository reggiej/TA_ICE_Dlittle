// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;

import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * <b>Purpose</b>: Maps an attribute to the corresponding database field type.
 * The list of field types that are supported by TopLink's direct to field mapping
 * is dependent on the relational database being used.
 * A converter can be used to convert between the object and data type if they do not match.
 *
 * @see oracle.toplink.mappings.converters.Converter
 * @see oracle.toplink.mappings.converters.ObjectTypeConverter
 * @see oracle.toplink.mappings.converters.TypeConversionConverter
 * @see oracle.toplink.mappings.converters.SerializedObjectConverter
 *
 * @author Sati
 * @since TopLink/Java 1.0
 */
public class DirectToFieldMapping extends AbstractDirectMapping implements RelationalMapping {

    /**
     * Default constructor.
     */
    public DirectToFieldMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Set the field name in the mapping.
     */
    public void setFieldName(String FieldName) {
        setField(new DatabaseField(FieldName));
    }

    protected void writeValueIntoRow(AbstractRecord row, DatabaseField field, Object fieldValue) {
        row.add(getField(), fieldValue);
    }
}