// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.publicinterface;

import java.io.Serializable;

import oracle.toplink.descriptors.ClassDescriptor;


/**
 * <p><b>Purpose</b>: TopLink has been designed to take advantage of the similarities between
 * relational databases and objects while accommodating for their differences, providing an object
 * oriented wrapper for relational databases. This is accomplished through the use of Descriptors.
 * A descriptor is a pure specification class with all its behaviour deputized to DescriptorEventManager,
 * DescriptorQueryManager and ObjectBuilder. Look at the following variables for the list
 * of specification on the descriptor.
 *
 * A Descriptor is a set of mappings that describe how an objects's data is to be represented in a
 * relational database. It contains mappings from the class instance variables to the table's fields,
 * as well as the transformation routines necessary for storing and retrieving attributes. As such
 * the descriptor acts as the link between the Java object and its database representaiton.
 *
 * Every descripor is initialized with the following information:
 * <ul>
 * <li> The Java class its describes, and the corresponding table(s) for storing instances of the class.
 * <li> The primary key of the table.
 * <li> A list of query keys for field names.
 * <li> A description of the objects's attributes and relationships. This information is stored in mappings.
 * <li> A set of user selectable properties for tailoring the behaviour of the descriptor.
 * </ul>
 *
 * @see DescriptorEventManager
 * @see DescriptorQueryManager
 * @see InheritancePolicy
 * @see InterfacePolicy
 * @see OptimisticLockingPolicy
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.descriptors.ClassDescriptor}, and
 *         {@link oracle.toplink.descriptors.RelationalDescriptor}
 */
public class Descriptor extends ClassDescriptor implements Cloneable, Serializable {
    
    /**
     * PUBLIC:
     * Return a new descriptor.
     */
    public Descriptor() {
        super();
    }

}
