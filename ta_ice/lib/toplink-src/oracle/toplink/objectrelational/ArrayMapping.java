// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.objectrelational;

import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose:</b>
 * In an object-relational data model, structures can contain "Arrays" or collections of other data-types.
 * In Oracle 8i, a "VARRAY" is typically used to represent a collection of primitive data or aggregate structures.
 * These arrays are stored with their parent structure in the same table.
 *
 * @see StructureMapping
 * @see NestedTableMapping
 * @see ReferenceMapping
 */
public class ArrayMapping extends oracle.toplink.sdk.SDKDirectCollectionMapping {

    /**
     * Default constructor.
     */
    public ArrayMapping() {
        super();
    }

    /**
     * PUBLIC:
     * Return the name of the structure.
     * This is the name of the user-defined data type as defined on the database.
     */
    public String getStructureName() {
        return this.getElementDataTypeName();
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        if (this.getStructureName().length() == 0) {
            throw DescriptorException.structureNameNotSetInMapping(this);
        }

        // For bug 2730536 convert the field to be an ObjectRelationalDatabaseField.
        ObjectRelationalDatabaseField field = (ObjectRelationalDatabaseField)getField();
        field.setSqlType(java.sql.Types.ARRAY);
        field.setSqlTypeName(getStructureName());
    }

    /**
     * PUBLIC:
     * Set the name of the field that holds the nested collection.
     */
    public void setFieldName(String fieldName) {
        this.setField(new ObjectRelationalDatabaseField(fieldName));
    }

    /**
     * PUBLIC:
     * Set the name of the structure.
     * This is the name of the user-defined data type as defined on the database.
     */
    public void setStructureName(String structureName) {
        this.setElementDataTypeName(structureName);
    }
}