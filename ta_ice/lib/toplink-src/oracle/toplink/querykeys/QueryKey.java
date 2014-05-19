// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.querykeys;

import java.io.*;

import oracle.toplink.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: Define a Java appropriate alias to a database field or function.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the name of the alias.
 * <li> Define the descriptor of the alias.
 * </ul>
 */
public class QueryKey implements Cloneable, Serializable {
    protected String name;
    protected ClassDescriptor descriptor;

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        Object object = null;

        try {
            object = super.clone();
        } catch (Exception exception) {
            throw new InternalError(exception.toString());
        }

        return object;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this QueryKey to actual class-based
     * settings
     * Will be overridded by subclasses
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){}

    /**
     * INTERNAL:
     * Return the descriptor.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * PUBLIC:
     * Return the name for the query key.
     * This is the name that will be used in the expression.
     */
    public String getName() {
        return name;
    }

    /**
     * INTERNAL:
     * Initialize any information in the receiver that requires its descriptor.
     * Set the receiver's descriptor back reference.
     * @param aDescriptor is the owner descriptor of the receiver.
     */
    public void initialize(ClassDescriptor aDescriptor) {
        setDescriptor(aDescriptor);
    }

    /**
     * INTERNAL:
     * return whether this query key is abstract
     * @return boolean
     */
    public boolean isAbstractQueryKey() {
        return (this.getClass().equals(oracle.toplink.internal.helper.ClassConstants.QueryKey_Class));
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isCollectionQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isDirectCollectionQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isDirectQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isForeignReferenceQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isManyToManyQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isOneToManyQueryKey() {
        return false;
    }

    /**
     * PUBLIC::
     * Related query key should implement this method to return true.
     */
    public boolean isOneToOneQueryKey() {
        return false;
    }

    /**
     * INTERNAL:
     * This is a QueryKey.  return true.
     * @return boolean
     */
    public boolean isQueryKey() {
        return true;
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * PUBLIC:
     * Set the name for the query key.
     * This is the name that will be used in the expression.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * INTERNAL:
     * return a string representation of this instance of QueryKey
     */
    public String toString() {
        return oracle.toplink.internal.helper.Helper.getShortClassName(this) + "(" + getName() + ")";
    }
}