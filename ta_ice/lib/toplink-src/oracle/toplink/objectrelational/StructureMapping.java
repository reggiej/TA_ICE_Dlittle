// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.objectrelational;

import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose:</b>
 * A structure is an object-relational user-defined data-type or object-type. A structure is similar to a Java class
 * as it defines attributes or fields where each attribute is either a primitive data-type, another structure, an
 * array, or a reference to another structure.
 * The mapping is similar to an AggregateObjectMapping, as multiple objects are stored in a single table.
 */
public class StructureMapping extends oracle.toplink.sdk.SDKAggregateObjectMapping {

    /**
     * Default constructor.
     */
    public StructureMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isStructureMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Return the name of the structure.
     * This is the name of the user-defined data type as defined on the database.
     */
    public String getStructureName() {
        if (getReferenceDescriptor() instanceof ObjectRelationalDescriptor) {
            return ((ObjectRelationalDescriptor)getReferenceDescriptor()).getStructureName();
        } else {
            return "";
        }
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        // For bug 2730536 convert the field to be an ObjectRelationalDatabaseField.
        ObjectRelationalDatabaseField field = (ObjectRelationalDatabaseField)getField();
        field.setSqlType(java.sql.Types.STRUCT);
        field.setSqlTypeName(getStructureName());
    }

    public void setFieldName(String fieldName) {
        this.setField(new ObjectRelationalDatabaseField(fieldName));
    }
}