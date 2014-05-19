// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * A PrivateOwned annotation is used to specify a relationship is privately 
 * owned. A privately owned relationship means the target object is a dependent 
 * part of the source object and is not referenced by any other object and 
 * cannot exist on its own. Private ownership causes many operations to be 
 * cascaded across the relationship, including, deletion, insertion, refreshing, 
 * locking (when cascaded). It also ensures that private objects removed from 
 * collections are deleted and object added are inserted.
 * 
 * A PrivateOwned annotation can be used in conjunction with a BasicCollection, 
 * BasicMap, OneToOne, and OneToMany annotation.
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface PrivateOwned {}
