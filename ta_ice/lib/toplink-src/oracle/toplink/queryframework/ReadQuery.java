// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.Vector;
import oracle.toplink.internal.helper.NonSynchronizedVector;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.sessions.DatabaseRecord;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.databaseaccess.DatabaseCall;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all read queries.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Caches result of query if flag is set.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class ReadQuery extends DatabaseQuery {

    /** Used for retrieve limited rows through the query. */
    protected int maxRows;

    /** Used to start query results at a specific result */
    protected int firstResult;

    /* used on read queries to stamp the object to determine the last time it was refreshed to
     * reduce work and prevent inifinite recursion on Refreshes
     *CR #4365 - used to prevent infinit recursion on refresh object cascade all
     * CR #2698903 - fix for the previous fix. No longer using millis but ids now.
     */
    protected long queryId;

    /** Used to set statement fetch size */
    protected int fetchSize;

    /** Used to specify how query results are cached */
    protected QueryResultsCachePolicy queryResultCachingPolicy = null;

    /** Optimization: temporarily stores cached query results while they are being built in a cloned query */
    protected transient Object temporaryCachedQueryResults = null;

    /**
     * PUBLIC:
     * Initialize the state of the query
     */
    public ReadQuery() {
        this.maxRows = 0;
        this.firstResult = 0;
        this.fetchSize = 0;
        this.queryId = 0;
    }

    /**
     * INTERNAL:
     * By default return the row.
     * Used by cursored stream.
     */
    public Object buildObject(AbstractRecord row) {
        return row;
    }

    /**
      * ADVANCED:
      * <P>This method will instruct the query to cache the results returned by its
      * next execution. All subsequent executions of this query will return this
      * cached result set even if new query parameters are specified. This method
      * provides a performance enhancement for queries known to always return the
      * same result set. Oracle recommends that you use this method only for such
      * queries.</P>
      * <P>To disable this behaviour, call {@link #doNotCacheQueryResults} or
      * {@link #setQueryResultsCachePolicy} passing in null.</P>
      */
    public void cacheQueryResults() {
        setQueryResultsCachePolicy(new QueryResultsCachePolicy());
    }

    /**
     * INTERNAL:
     * <P> This method is called by the object builder when building an original.
     * It will cause the original to be cached in the query results if the query
     * is set to do so.
     */
    public abstract void cacheResult(Object object);

    /**
     * PUBLIC:
     * Clears the current cached results, the next execution with
     * read from the database.
     *
     * @deprecated
     * @since 10.1.3
     * @see #clearQueryResults(Session)
     */
    public void clearQueryResults() {
        throw QueryException.clearQueryResultsNotSupported(this);
    }

    /**
     * INTERNAL
     * Used to give the subclasses oportunity to copy aspects of the cloned query
     * to the original query.
     */
    protected void clonedQueryExecutionComplete(DatabaseQuery query, AbstractSession session) {
        if (shouldCacheQueryResults()) {
            // Cached query results must exist on the original query rather than the cloned one
            setQueryResults(((ReadQuery)query).getTemporaryCachedQueryResults(), query.getTranslationRow(), query.getSession());
        }
    }

    /**
     * PUBLIC:
     * Clears the current cached results, the next execution with
     * read from the database.
     *
     */
    public void clearQueryResults(AbstractSession session) {
        session.getIdentityMapAccessor().clearQueryCache(this);
    }

    /**
      * ADVANCED:
      * <P>This method will instruct the query not to cache results. All subsequent
      * executions return result sets according to the current configuration of
      * query parameters. After calling this method, any previously cached result
      * set will be discarded the next time the query is executed.</P>
      * <P>This is equivalent to calling {@link #setShouldCacheQueryResults}
      * passing in a boolean false.</P>
      * <P>To enable this behaviour, call {@link #cacheQueryResults} or
      * {@link #setQueryResultsCachePolicy} passing in a valid QueryResultsCachePolicy.</P>
      * Note: If this method is called on a query that initially cached query results, 
      * clearQueryResults(Session) should also be called.  Otherwise, the results of
      * this query will remain in the cache and cause extra memory use
      */
    public void doNotCacheQueryResults() {
        setQueryResultsCachePolicy(null);
    }

    /**
     * PUBLIC:
     * Return the QueryResultsCachePolicy for this query.
     *
     * @see oracle.toplink.queryframework.QueryResultsCachePolicy
     */
    public QueryResultsCachePolicy getQueryResultsCachePolicy() {
        return queryResultCachingPolicy;
    }

    /**
     * PUBLIC:
     * Return the value that will be set for the firstResult in the returned result set
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * INTERNAL:
     * This method is used to get the time in millis that this query is being executed at.
     * it is set just prior to executing the SQL and will be used to determine which objects should be refreshed.
     * CR #4365
     * CR #2698903 ... instead of using millis we will now use id's instead. Method
     * renamed appropriately.
     */
    public long getQueryId() {
        return this.queryId;
    }

    /**
     * PUBLIC:
     * Return the limit for the maximum number of rows that any ResultSet can contain to the given number.
     */
    public int getMaxRows() {
        return this.maxRows;
    }

    /**
     * PUBLIC:
     * Return the fetchSize setting that this query will set on the JDBC Statement
     * NB - a value of zero means that no call to statement.setFetchSize() will be made.
     */
    public int getFetchSize() {
        return this.fetchSize;
    }

    /**
     * INTERNAL:
     * To any user of this object with some knowledge of what the query's results may contain.
     * Return the results of the query.
     * If the query has never been executed, or does not cache results,
     * the results will be null.
     */
    protected Object getQueryResults(AbstractSession session) {
        return getQueryResults(session, getTranslationRow(), true);
    }

    /**
     * INTERNAL:
     * To any user of this object with some knowledge of what the query's results may contain.
     * Return the results of the query.
     * If the query has never been executed, or does not cache results,
     * the results will be null.
     */
    protected Object getQueryResults(AbstractSession session, boolean checkExpiry) {
        return getQueryResults(session, getTranslationRow(), checkExpiry);
    }

    /**
     * INTERNAL:
     * To any user of this object with some knowledge of what the query's results may contain.
     * Return the results of the query.
     * If the query has never been executed, or does not cache results,
     * the results will be null.
     */
    protected Object getQueryResults(AbstractSession session, AbstractRecord row, boolean checkExpiry) {
        // Check for null translation row.
        Vector arguments = null;
        if (row == null) {
            arguments =  new NonSynchronizedVector(1);
        } else {
            arguments =  row.getValues();
        }
        return session.getIdentityMapAccessorInstance().getQueryResult(this, arguments, checkExpiry);
    }

    /**
     * INTERNAL:
     * Get results from the remporary cache.
     * Used when caching query results on a clone so they can be copied to the original
     * query
     */
    public Object getTemporaryCachedQueryResults(){
        return temporaryCachedQueryResults;
    }

    /**
     * INTERNAL:
     * Return true if the query uses default properties.
     * This is used to determine if this query is cacheable.
     * i.e. does not use any properties that may conflict with another query
     * with the same EJBQL or selection criteria.
     */
    public boolean isDefaultPropertiesQuery() {
        return super.isDefaultPropertiesQuery()
            && (getMaxRows() == 0)
            && (getFirstResult() == 0)
            && (getFetchSize() == 0);
    }
    
    /**
     * PUBLIC:
     * Return if this is a read query.
     */
    public boolean isReadQuery() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Copy all setting from the query.
     * This is used to morph queries from one type to the other.
     * By default this calls prepareFromQuery, but additional properties may be required
     * to be copied as prepareFromQuery only copies properties that affect the SQL.
     */
    public void copyFromQuery(DatabaseQuery query) {
        super.copyFromQuery(query);
        if (query.isReadQuery()) {
            ReadQuery readQuery = (ReadQuery)query;
            this.fetchSize = readQuery.fetchSize;
            this.firstResult = readQuery.firstResult;
            this.maxRows = readQuery.maxRows;
            this.queryResultCachingPolicy = readQuery.queryResultCachingPolicy;
        }
    }
    
    /**
     * INTERNAL:
     * This is different from 'prepareForExecution' in that this is called on the original query,
     * and the other is called on the copy of the query.
     * This query is copied for concurrency so this prepare can only setup things that
     * will apply to any future execution of this query.
     *
     * Clear the query cache when a query is prepared.
     */
    protected void prepare() throws QueryException {
        super.prepare();
	if (shouldCacheQueryResults()){
            clearQueryResults(getSession());
	}
    }
    
    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();
        DatabaseCall databaseCall = this.getCall();
        if ( databaseCall !=null && databaseCall.shouldIgnoreFirstRowMaxResultsSettings() ){
            AbstractRecord parameters = this.getTranslationRow();
            if (parameters.isEmpty()){
                parameters = new DatabaseRecord();
            }
            parameters.add(DatabaseCall.FIRSTRESULT_FIELD, this.getFirstResult());
            parameters.add(DatabaseCall.MAXROW_FIELD, this.getMaxRows());
            this.setTranslationRow(parameters);
        }
        
    }

    /**
     * INTERNAL:
     * Return if this is a read query.
     */
    public Object remoteExecute(AbstractSession session) throws DatabaseException {
        if (shouldCacheQueryResults()) {
            AbstractRecord arguments = new DatabaseRecord();
            if (translationRow != null){
                arguments = translationRow;
            }
            Object queryResults = getQueryResults(session, arguments, true);
            if (queryResults != null) {
                return queryResults;
            }
            queryResults = super.remoteExecute(session);
            if (queryResults != null){
                setQueryResults(queryResults, arguments, session);
            }
            return queryResults;
        }
        return super.remoteExecute(session);
    }

    /**
     * Set the QueryResultsCachePolicy.
     *
     * @see oracle.toplink.queryframework.QueryResultsCachePolicy
     */
    public void setQueryResultsCachePolicy(QueryResultsCachePolicy policy) {
        queryResultCachingPolicy = policy;
        // ensure the cache is cleared if the caching policy is changed
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Used to set the first result in any result set that is returned for this query.
     * This method should only be set once per query.  To change the firstReslt use another query.
     * This method will call the absolute method on the JDBC result set to move the initial row
     * used by TopLink.  Note: The set of results returned from the database will still include
     * the results before the first result.  TopLink will just not use them for object building.
     */
    public void setFirstResult(int firstResult) {
        if ( isPrepared() && this.firstResult != firstResult){
            if ( this.getCall()!=null && this.getCall().shouldIgnoreFirstRowMaxResultsSettings() ){
                //don't need to reprepare as firstResult is already built into the sql if ignoreFirstRowMaxResultsSettings is set
            }else{
                setIsPrepared(false);
            }
        }
        this.firstResult = firstResult;
        shouldCloneCall=true;
    }

    /**
     * INTERNAL:
     * This method is used to set the current system time in millis that this query is being executed at.
     * it is set just prior to executing the SQL and will be used to determine which objects should be refreshed.
     * CR #4365
     * CR #2698903 ... instead of using millis we will now use id's instead. Method
     * renamed appropriately.
     */
    public void setQueryId(long id) {
        this.queryId = id;
    }

    /**
    * PUBLIC:
    * Used to set the limit for the maximum number of rows that any ResultSet can contain to the given number.
    * This method should only be set once per query.  To change the max rows use another query.
    * This method limits the number of candidate results returned to TopLink that can be used to build objects
    */
    public void setMaxRows(int maxRows) {
        if ( isPrepared() && this.maxRows != maxRows){
            if ( this.getCall()!=null && this.getCall().shouldIgnoreFirstRowMaxResultsSettings() && this.maxRows>0 ){
            }else{
                setIsPrepared(false);
            }
        }
        this.maxRows = maxRows;
        shouldCloneCall=true;
    }

    /**
     * PUBLIC:
     * Set the fetchSize setting that this query will set on the JDBC Statement
     * NB - a value of zero means that no call to statement.setFetchSize() will be made.
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * INTERNAL:
     * Set the cached results of the query.
     * This will only be set if the query caches results.
     */
    protected void setQueryResults(Object resultFromQuery, AbstractSession session) {
        session.getIdentityMapAccessorInstance().putQueryResult(this, getTranslationRow().getValues(), resultFromQuery);
    }

    /**
     * INTERNAL:
     * Set the cached results of the query.
     * This will only be set if the query caches results.
     */
    protected void setQueryResults(Object resultFromQuery, AbstractRecord row, AbstractSession session) {
        Vector arguments = null;
        if (row == null) {
            arguments =  new NonSynchronizedVector(1);
        } else {
            arguments =  row.getValues();
        }
        session.getIdentityMapAccessorInstance().putQueryResult(this, arguments, resultFromQuery);
    }

    /**
     * ADVANCED:
     * Use this method to configure whether or not the query should cache the
     * results returned by its next execution.
     * <P>When shouldCacheQueryResults is true, this method instructs the query
     * to cache the results returned by its next execution. All subsequent
     * executions of this query will return this cached result set even if you
     * specify new query parameters. This provides a performance enhancement for
     * queries known to always return the same result set. Oracle recommends that
     * you set this parameter to true only for such queries.</P>
     * <P>When shouldCacheQueryResults is false, this method instructs the
     * query not to cache results. All subsequent executions of this query return
     * result sets according to the current configuration of query parameters.
     * After setting this parameter to false, any previously cached result set will
     * be discarded the next time the query is executed.</P>
     * @param shouldCacheQueryResults boolean true, the query caches results of its
     * next execution; false, the query does not cache results
     *
     * Note: If calling setShouldCacheQueryResults(false) on a query that currently
     * caches it's results, clearQueryResults(Session) should also be called
     * This will avoid using extra memory to store query results of a query that
     * no longer caches.
     *
     * @deprecated since 10.1.3, replaced by setQueryResultsCachingPolicy(QueryResultsCachePolicy)
     * @see #setQueryResultsCachePolicy(QueryResultsCachePolicy)
     */
    public void setShouldCacheQueryResults(boolean shouldCacheQueryResults) {
        if (shouldCacheQueryResults) {
            setQueryResultsCachePolicy(new QueryResultsCachePolicy());
        } else {
            setQueryResultsCachePolicy(null);
        }
    }

    /**
     * PUBLIC:
     * Return if the query should cache the results of the next execution or not.
     */
    public boolean shouldCacheQueryResults() {
        return queryResultCachingPolicy != null;
    }
    
    /**
     * INTERNAL:
     * Put results in the remporary cache.
     * Used when caching query results on a clone so they can be copied to the original
     * query
     */
    public void setTemporaryCachedQueryResults(Object queryResults){
        temporaryCachedQueryResults = queryResults;
    }
}
