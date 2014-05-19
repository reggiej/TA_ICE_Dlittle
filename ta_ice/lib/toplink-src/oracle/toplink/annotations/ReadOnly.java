// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * The ReadOnly annotation is used to specify that a class is read only. It 
 * may be defined on an Entity or MappedSuperclass. In the case of inheritance, 
 * a ReadOnly annotation can only be defined on the root of the inheritance 
 * hierarchy
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface ReadOnly {}
