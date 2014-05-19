// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.persistence.QueryHint;

/** 
 * A NamedStoredProcedureQuery annotation allows the definition of queries that 
 * call stored procedures as named queries.
 * 
 * @see oracle.toplink.annotations.StoredProcedureParameter.
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamedStoredProcedureQuery {
    /**
     * (Required) Unique name that references this stored procedure query.
     */
    String name();

    /**
     * (Optional) Query hints.
     */
    QueryHint[] hints() default {};

    /**
     * (Optional) Refers to the class of the result.
     */
    Class resultClass() default void.class;

    /**
     * (Optional) The name of the SQLResultMapping.
     */
    String resultSetMapping() default "";

    /**
     * (Required) The name of the stored procedure.
     */
    String procedureName();

    /**
     * (Optional) Whether the query should return a result set. You should only 
     * set this flag to true if you expect a raw JDBC ResultSet to be returned 
     * from  your stored proceduce. Otherwise, you  should let the default apply.
     */
    boolean returnsResultSet() default false; 

    /**
     * (Optional) Defines arguments to the stored procedure.
     */
    StoredProcedureParameter[] procedureParameters() default {};
}
