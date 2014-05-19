// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;


/**
 * <p><b>Purpose</b>: Generic association object.
 * This can be used to map hashtable/map containers where the key and value are non-typed primitives.
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public class TypedAssociation extends Association {
    protected Class keyType;
    protected Class valueType;

    /**
     * Default constructor.
     */
    public TypedAssociation() {
        super();
    }

    /**
     * PUBLIC:
     * Create an association.
     */
    public TypedAssociation(Object key, Object value) {
        super(key, value);
        if (key != null) {
            this.keyType = key.getClass();
        }
        this.value = value;
        if (value != null) {
            this.valueType = value.getClass();
        }
    }

    /**
     * PUBLIC:
     * Return the class of the key.
     */
    public Class getKeyType() {
        return keyType;
    }

    /**
     * PUBLIC:
     * Return the class of the value.
     */
    public Class getValueType() {
        return valueType;
    }

    /**
     * INTERNAL:
     * Handler for the descriptor post build event.
     * Convert the key and values to their appropriate type.
     */
    public void postBuild(oracle.toplink.descriptors.DescriptorEvent event) {
        setKey(event.getSession().getDatasourceLogin().getDatasourcePlatform().getConversionManager().convertObject(getKey(), getKeyType()));
        setValue(event.getSession().getDatasourceLogin().getDatasourcePlatform().getConversionManager().convertObject(getValue(), getValueType()));
    }

    /**
     * PUBLIC:
     * Set the class of the key.
     */
    public void setKeyType(Class keyType) {
        this.keyType = keyType;
    }

    /**
     * PUBLIC:
     * Set the class of the value.
     */
    public void setValueType(Class valueType) {
        this.valueType = valueType;
    }
}