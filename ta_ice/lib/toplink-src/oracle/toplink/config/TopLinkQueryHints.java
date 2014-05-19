// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * The class defines TopLink query hints' names.
 * These query hints allow a JPA Query to be customized or optimized beyond
 * what is available in the JPA specification.
 * 
 * <p>JPA Query Hint Usage:
 * 
 * <p><code>query.setHint(TopLinkQueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);</code>
 * <p>or 
 * <p><code>@QueryHint(name=TopLinkQueryHints.CACHE_USAGE, value=CacheUsage.CheckCacheOnly)</code>
 * 
 * <p>Hint values are case-insensitive; "" could be used instead of default value.
 * 
 * @see HintValues
 * @see CacheUsage
 * @see PessimisticLock
 * @see QueryType
 */
public class TopLinkQueryHints {
   /**
    * Configures parameter binding to be disabled or enabled just for this query (overrides persistent unit setting, which default to true).
    * Valid values are:  HintValues.PERSISTENCE_UNIT_DEFAULT, HintValues.TRUE, HintValues.FALSE,
    * "" could be used instead of default value HintValues.PERSISTENCE_UNIT_DEFAULT
    */
    public static final String BIND_PARAMETERS = "toplink.jdbc.bind-parameters";

   /**
    * Configures the query to utilize the TopLink cache, by default the cache is not checked on queries before accessing the database.
    * Valid values are all declared in CacheUsage class.
    */
    public static final String CACHE_USAGE = "toplink.cache-usage";
    
   /**
    * Configures the TopLink query type to use for the query.
    * By default TopLink ReportQuery or ReadAllQuery are used for most JPQL queries, this allows other query types to be used,
    * such as ReadObjectQuery which can be used for queries that are know to return a single object, and has different caching semantics.
    * Valid values are all declared in QueryType class.
    */
    public static final String QUERY_TYPE = "toplink.query-type";
    
   /**
    * Configures  the query to acquire a pessimisitc lock (write-lock) on the resulting rows in the database.
    * Valid values are all declared in PessimisticLock class.
    */
    public static final String PESSIMISTIC_LOCK = "toplink.pessimistic-lock";
    
   /**
    * Configures the query to refresh the resulting objects in the cache and persistent context with the current state of the database.
    * Valid values are:  HintValues.FALSE, HintValues.TRUE,
    * "" could be used instead of default value HintValues.FALSE
    */
    public static final String REFRESH = "toplink.refresh";
    
    /**
     * Configures the query to optimize the retrieval of the related objects,
     * the related objects for all the resulting objects will be read in a single query (instead of n queries).
     * Valid values are strings that represent JPQL style navigations to a relationship.
     * <p>e.g. e.manager.phoneNumbers
     */
    public static final String BATCH = "toplink.batch";
    
    /**
     * Configures the query to optimize the retrieval of the related objects,
     * the related objects will be joined into the query instead of being queried independently.
     * This allow for nested join fetching which is not supported in JPQL.
     * Valid values are strings that represent JPQL style navigations to a relationship.
     * <p>e.g. e.manager.phoneNumbers
     */
    public static final String FETCH = "toplink.join-fetch";
    
    /**
     * Configures the query to return shared (read-only) objects from the cache,
     * instead of objects registered with the persistence context.
     * This improves performance by avoiding the persistence context registration and change tracking overhead to read-only objects.
     * Valid values are:  HintValues.FALSE, HintValues.TRUE,
     * "" could be used instead of default value HintValues.FALSE
     */
    public static final String RETURN_SHARED = "toplink.return-shared";
    
    /**
     * Configures the JDBC timeout of the query execution, if the database query exceeds the timeout
     * the database will terminate the query and return an exception.
     * Valid values are Integer or Strings that can be parsed to int values.
     */
    public static final String JDBC_TIMEOUT = "toplink.jdbc.timeout";
    
    /**
     * Configures the JDBC fetch-size for the queries result-set.
     * This can improve the performance for queries that return large result-sets.
     * Valid values are Integer or Strings that can be parsed to int values.
     */
    public static final String JDBC_FETCH_SIZE = "toplink.jdbc.fetch-size";
    
     /**
      * Configures the JDBC max-rows, if the query returns more rows than the max-rows
      * the trailing rows will not be returned by the database.
      * Valid values are Integer or Strings that can be parsed to int values.
      */
    public static final String JDBC_MAX_ROWS = "toplink.jdbc.max-rows";
    
    /**
     * Configures the collection class implementation for the queries result.
     * The fully qualified class name must be used, without the .class.     * 
     * Valid values are a Class representing a collection type or a String representing the classname 
     * the colleciton type.
     * <p>e.g. java.util.ArrayList
     */
    public static final String RESULT_COLLECTION_TYPE = "toplink.result-collection-type";
    
    /**
     * Valid values are all declared in CascadePolicy class.
     */
    public static final String REFRESH_CASCADE = "toplink.refresh.cascade";
}
