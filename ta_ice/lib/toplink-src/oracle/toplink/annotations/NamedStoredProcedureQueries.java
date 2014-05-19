// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * A NamedStoredProcedureQueries annotation allows the definition of multiple
 * NamedStoredProcedureQuery.
 * 
 * @see oracle.toplink.annotations.NamedStoredProcedureQuery
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamedStoredProcedureQueries {
    /**
     * (Required) An array of named stored procedure query.
     */
    NamedStoredProcedureQuery[] value();
}
