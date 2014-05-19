// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.descriptors.invalidation.*;

/**
 * PUBLIC:
 *
 * A QueryResultsCache policy dicates how a query's results will be cached.
 *
 * It allows an invalidation policy and a maximum number of results to be set.
 * Query results are cached based on the parameter values of a query, and the maximum number
 * of results refers to the maxmium number of parameter sets results will be ached for.
 *
 * @see oracle.toplink.queryframework.ReadQuery#setQueryCachePolicy(QueryResultsCachePolicy)
 */
public class QueryResultsCachePolicy implements java.io.Serializable {
    protected CacheInvalidationPolicy invalidationPolicy;
    protected int maximumResultSets = 0;

    /**
     * PUBLIC:
     * Build a QueryResultsCachePolicy with the default settings
     * By default there is no invalidation of query results and the maximum
     * number of results sets is 100.
     */
    public QueryResultsCachePolicy() {
        this(new NoExpiryCacheInvalidationPolicy(), 100);
    }

    /**
     * PUBLIC:
     * Build a QueryResultsCachePolicy and supply a CacheInvalidationPolicy and a maximum
     * number of results sets.
     *
     * @see oracle.toplink.descriptors.invalidation.CacheInvalidationPolicy
     */
    public QueryResultsCachePolicy(CacheInvalidationPolicy policy, int maximumResultSets) {
        this.invalidationPolicy = policy;
        this.maximumResultSets = maximumResultSets;
    }

    /**
     * PUBLIC:
     * Build a QueryResultsCachePolicy and supply a CacheInvalidationPolicy. The default
     * value of 100 will be used for the maximum number of result sets
     *
     * @see oracle.toplink.descriptors.invalidation.CacheInvalidationPolicy
     */
    public QueryResultsCachePolicy(CacheInvalidationPolicy policy) {
        this(policy, 100);
    }

    /**
     * PUBLIC:
     * Build a QueryResultsCachePolicy and supply a maximum for the number of results sets.
     * Results will be set not to expire in the cache.
     */
    public QueryResultsCachePolicy(int maximumResultSets) {
        this(new NoExpiryCacheInvalidationPolicy(), maximumResultSets);
    }

    /**
     * PUBLIC:
     * Return the query cache invalidation policy.
     * The cache invalidation policy defines how the query results are invalidated.
     */
    public CacheInvalidationPolicy getCacheInvalidationPolicy() {
        return invalidationPolicy;
    }

    /**
     * PUBLIC:
     * Set the query cache invalidation policy.
     * The cache invalidation policy defines how the query results are invalidated.
     */
    public void setCacheInvalidationPolicy(CacheInvalidationPolicy invalidationPolicy) {
        this.invalidationPolicy = invalidationPolicy;
    }

    /**
     * PUBLIC:
     * Return the maximum cached results.
     * This defines the number of query result sets that will be cached.
     * The LRU query results will be discarded when the max size is reached.
     */
    public int getMaximumCachedResults() {
        return maximumResultSets;
    }
    
    /**
     * PUBLIC:
     * Set the maximum cached results.
     * This defines the number of query result sets that will be cached.
     * The LRU query results will be discarded when the max size is reached.
     */
    public void setMaximumCachedResults(int maximumResultSets) {
        this.maximumResultSets = maximumResultSets;
    }
}