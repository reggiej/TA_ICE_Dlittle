// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * Hint values.
 * 
 * The class defines boolean values used by some TopLink query hint.
 * 
 * <p>JPA Query Hint Usage:
 * 
 * <p><code>query.setHint(TopLinkQueryHints.REFRESH, HintValues.TRUE);</code>
 * <p>or 
 * <p><code>@QueryHint(name=TopLinkQueryHints.REFRESH, value=HintValues.TRUE)</code>
 * 
 * <p>Hint values are case-insensitive.
 * 
 * @see TopLinkQueryHints
 */
public class HintValues {
    public static final String TRUE = "True";
    public static final String FALSE = "False";
    public static final String PERSISTENCE_UNIT_DEFAULT = "PersistenceUnitDefault";
}
