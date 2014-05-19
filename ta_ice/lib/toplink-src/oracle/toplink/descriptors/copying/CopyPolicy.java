// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.copying;

import java.io.*;
import java.util.Vector;

import oracle.toplink.exceptions.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import oracle.toplink.sessions.*;

/**
 * <p><b>Purpose</b>: Allows customization of how an object is cloned.
 * An implementer of CopyPolicy can be set on a descriptor to provide
 * special cloning routine for how an object is cloned in a unit of work.
 * By default the InstantiationCopyPolicy is used which creates a new instance of
 * the class to be copied into.
 * The MethodBasedCopyPolicy can also be used that uses a clone method in the object
 * to clone the object.  When a clone method is used it avoid the requirement of having to
 * copy over each of the direct attributes.
 */
public interface CopyPolicy extends Cloneable, Serializable {

    /**
     * Return a shallow clone of the object for usage with object copying, or unit of work backup cloning.
     */
    Object buildClone(Object object, Session session) throws DescriptorException;

    /**
     * Return a shallow clone of the object for usage with the unit of work working copy.
     */
    Object buildWorkingCopyClone(Object object, Session session) throws DescriptorException;

    /**
     * Return an instance with the primary key, used for building a working copy during a unit of work transactional read.
     */
    Object buildWorkingCopyCloneFromPrimaryKeyObject(Object primaryKeyObject, ObjectBuildingQuery query, oracle.toplink.sessions.UnitOfWork uow) throws DescriptorException;
            
    /**
     * Return an instance with the primary key set from the row, used for building a working copy during a unit of work transactional read.
     */
    Object buildWorkingCopyCloneFromRow(Record row, ObjectBuildingQuery query, Vector primaryKey, UnitOfWork uow) throws DescriptorException;
           
    /**
     * Clone the CopyPolicy.
     */
    Object clone();

    /**
     * Allow for any initialization or validation required.
     */
    void initialize(Session session) throws DescriptorException;

    /**
     * Set the descriptor.
     */
    void setDescriptor(ClassDescriptor descriptor);

    /**
     * Return if this copy policy creates a new instance, vs a clone.
     */
    boolean buildsNewInstance();
}