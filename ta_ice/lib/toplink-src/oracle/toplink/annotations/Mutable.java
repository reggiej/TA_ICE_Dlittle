// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A Mutable annotation can be used on a @Basic mapping.
 * It can be used on complex field types to indicate that the value itself can be changed or not changed (instead of being replaced).
 * Most basic types are not mutable, i.e. int, long, float, double, String, BigDecimal.
 * Date or Calendar types are assumed not to be mutable by default, if it is desired to call
 * the set methods on the Date or Calendar, then the mapping must be set to be @Mutable.
 * For Date and Calendar types the global persistence property "toplink.temporal.mutable" can also be set to "true".
 * For serialized types, by default they are assumed to be mutable, if they are not mutable this annoation can be set to false.
 * Mutable basic mappings affect the overhead of change tracking, attribute change tracking can only be weaved with non-mutable mappings.
 */
 
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Mutable {
    /**
     * (Optional) Set the mapping to be mutable, or not mutable.
     */
    boolean value() default true;
}
