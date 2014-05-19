// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.persistence.Column;

import static oracle.toplink.annotations.OptimisticLockingType.VERSION_COLUMN;

/** 
 * The OptimisticLocking annotation is used to specify the type of optimistic 
 * locking TopLink should use when updating or deleting entities.
 * 
 * @see oracle.toplink.annotations.OptimisticLockingType.
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface OptimisticLocking {
    /**
     * (Optional) The type of optimistic locking policy to use.
     */
    OptimisticLockingType type() default VERSION_COLUMN;

    /**
     * (Optional) For an optimistic locking policy of type SELECTED_COLUMNS, 
     * this annotation member becomes a (Required) field.
     */
    Column[] selectedColumns() default {};

    /**
     * (Optional) Specify where the optimistic locking policy should cascade 
     * lock. Currently only supported with VERSION_COLUMN locking.
     */
    boolean cascade() default false;
}
