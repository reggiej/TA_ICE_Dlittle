// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A JoinFetch annotation can be used on any relationship mapping,
 * (OneToOne, ManyToOne, OneToMany, ManyToMany, BasicCollection, BasicMap).
 * It allows the related objects to be joined and read in the same query as the 
 * source object. Join fetching can also be set at the query level, and it is 
 * normally recommended to do so as all queries may not require joining.
 * Batch reading should be considered as an alternative to join fetching, 
 * especially for collection relationships as it is typically more efficient.
 * 
 * @author James Sutherland
 * @since Oracle TopLink 11.1.1.0.0 
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JoinFetch {
    /**
     * (Optional) The type of join-fetch to use.
     * Either an inner or outer-join,
     * an outer-join allows for null/empty values, where as inner does not.
     */ 
    JoinFetchType value() default JoinFetchType.INNER;
}
