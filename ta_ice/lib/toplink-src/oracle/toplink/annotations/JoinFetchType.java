// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

/**
 * An enum type that is used within the JoinFetch annotation.
 * 
 * @see oracle.toplink.annotations.JoinFetch
 * @author James Sutherland
 * @since Oracle TopLink 11.1.1.0.0 
 */
public enum JoinFetchType {
    /**
     * An inner join is used to fetch the related object.
     * This does not allow for null/empty values.
     */
    INNER,

    /**
     * An inner join is used to fetch the related object.
     * This allows for null/empty values.
     */
    OUTER,
}
