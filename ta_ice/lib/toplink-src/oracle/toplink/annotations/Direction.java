// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

/** 
 * An enum that is used within the StoredProcedureParameter annotation.
 * 
 * @see oracle.toplink.annotations.StoredProcedureParameter
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
public enum Direction {
    /**
     * Input parameter
     */
    IN,

    /**
     * Output parameter
     */
    OUT,

    /**
     * Input and output parameter
     */
    IN_OUT,

    /**
     * Output cursor
     */
    OUT_CURSOR
}
