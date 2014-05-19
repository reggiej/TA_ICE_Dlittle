// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * Query type hint values.
 * 
 * The class contains all the valid values for TopLinkQueryHints.QUERY_TYPE query hint.
 * 
 * JPA Query Hint Usage:
 * 
 * <p><code>query.setHint(TopLinkQueryHints.QueryType, QueryType.ReadObject);</code>
 * <p>or 
 * <p><code>@QueryHint(name=TopLinkQueryHints.QueryType, value=QueryType.ReadObject)</code>
 * 
 * <p>Hint values are case-insensitive.
 * "" could be used instead of default value CacheUsage.DEFAULT.
 * 
 * @see TopLinkQueryHints
 */
public class QueryType {
    public static final String  Auto = "Auto";
    public static final String  ReadObject = "ReadObject";
    public static final String  ReadAll = "ReadAll";
    public static final String  Report = "Report";
 
    public static final String DEFAULT = Auto;
}
