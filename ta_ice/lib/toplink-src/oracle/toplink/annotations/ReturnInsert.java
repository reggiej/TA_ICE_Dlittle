// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * A ReturnInsert annotation allows for INSERT operations to return values back 
 * into the object being written. This allows for table default values, trigger 
 * or stored procedures computed values to be set back into the object.
 * 
 * A ReturnInsert annotation can only be specified on a Basic mapping. 
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ReturnInsert {
    boolean returnOnly() default false;
}
