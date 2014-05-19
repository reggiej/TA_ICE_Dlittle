// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.mappings;

import oracle.toplink.internal.queryframework.*;

/**
 * Interface used by clients to interact
 * with the assorted mappings that use <code>ContainerPolicy</code>.
 *
 * @see oracle.toplink.internal.queryframework.ContainerPolicy
 *
 * @author Big Country
 * @since TOPLink/Java 4.0
 */
public interface ContainerMapping {

    /**
     * PUBLIC:
     * Return the mapping's container policy.
     */
    ContainerPolicy getContainerPolicy();

    /**
     * PUBLIC:
     * Set the mapping's container policy.
     */
    void setContainerPolicy(ContainerPolicy containerPolicy);

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.Collection</code> interface.
     */
    void useCollectionClass(Class concreteClass);

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects. The key used to index a value in the
     * <code>Map</code> is the value returned by a call to the specified
     * zero-argument method.
     * The method must be implemented by the class (or a superclass) of any
     * value to be inserted into the <code>Map</code>.
     * <p>The container class must implement (directly or indirectly) the
     * <code>java.util.Map</code> interface.
     * <p>To facilitate resolving the method, the mapping's referenceClass
     * must set before calling this method.
     */
    void useMapClass(Class concreteClass, String methodName);
}