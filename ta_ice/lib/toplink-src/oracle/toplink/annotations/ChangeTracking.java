// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static oracle.toplink.annotations.ChangeTrackingType.AUTO;

/** 
 * The ChangeTracking annotation is used to specify the 
 * oracle.toplink.descriptors.changetracking.ObjectChangePolicy which computes 
 * changes sets for TopLink's UnitOfWork commit process. An ObjectChangePolicy 
 * is stored on an Entity's descriptor.
 * 
 * @see oracle.toplink.annotations.ChangeTrackingType
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface ChangeTracking {
    /**
     * (Optional) The type of change tracking to use.
     */ 
    ChangeTrackingType value() default AUTO;
}
