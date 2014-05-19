// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import java.io.Serializable;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The wrapper policy can be used to wrap all objects read from the database in another object.
 * This allows for TopLink to utilize one version of the class for its purposes and allows for the
 * application to deal with another version of the object.
 * The wrapper policy is used for things such as EJB Entity Beans and is directly used by the
 * TopLink for WebLogic product for EJB Container Managed Persistence.
 *
 * It is assumed that relationships must be through the wrapper objects.
 * Object identity is not maintained on the wrapper objects, only the wrapped object.
 */
public interface WrapperPolicy extends Serializable {

    /**
     * PUBLIC:
     * Required: Lets the policy perform initialization.
     * @param session the session to initialize against
     */
    void initialize(AbstractSession session) throws DescriptorException;

    /**
     * PUBLIC:
     * Required: Return true if the wrapped value should be traversed.
     * Normally the wrapped value is looked after independently, it is not required to be traversed.
     */
    boolean isTraversable();

    /**
     * PUBLIC:
     * Required: Return true if the object is already wrapped.
     */
    boolean isWrapped(Object object);

    /**
     * PUBLIC:
     * Required: Set the descriptor.
     * @param descriptor the descriptor for the object being wrapped
     */
    void setDescriptor(ClassDescriptor descriptor);

    /**
     * PUBLIC:
     * Required: Unwrap the object to return the implementation that is meant to be used by TopLink.
     * The object may already be unwrapped in which case the object should be returned.
     * @param proxy the wrapped object
     * @param session the session to unwrap into
     */
    Object unwrapObject(Object proxy, AbstractSession session);

    /**
     * PUBLIC:
     * Required: Wrap the object to return the implementation that the application requires.
     * The object may already be wrapped in which case the object should be returned.
     * @param original, the object to be wrapped
     * @param session the session to wrap the object against.
     * @return java.lang.Object the wrapped object
     */
    Object wrapObject(Object original, AbstractSession session);
}