// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.mappings.transformers;

import oracle.toplink.sessions.Session;

/**
 * <p><b>Purpose</b>: Allows a field to always be mapped to a constant value.
 * This allows default values to be provided for un-mapped fields.
 * @see oracle.toplink.mappings.FieldTransformer
 * @author  James Sutherland
 * @since   10.1.3
 */
public class ConstantTransformer extends FieldTransformerAdapter {
    protected Object value;
    
    public ConstantTransformer() {
        super();
    }
    
    /**
     * PUBLIC:
     * Return a constant transformer for the constant value.
     */
    public ConstantTransformer(Object value) {
        this.value = value;
    }
    
    /**
     * PUBLIC:
     * Return the value of the constant.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * PUBLIC:
     * Set the value of the constant.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * INTERNAL:
     * Always return the constant value.
     */
    public Object buildFieldValue(Object object, String fieldName, Session session) {
        return value;
    }
}