// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.*;
import java.sql.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.indirection.ProxyIndirectionPolicy;
import oracle.toplink.internal.descriptors.*;
import oracle.toplink.internal.remote.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.helper.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.descriptors.DescriptorQueryManager;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.remote.*;
import oracle.toplink.tools.profiler.QueryMonitor;

/**
 * <p><b>Purpose</b>:
 * Concrete class for all read queries involving a single object.
 * <p>
 * <p><b>Responsibilities</b>:
 * Return a single object for the query.
 * Implements the inheritance feature when dealing with abstract descriptors.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class ReadObjectQuery extends ObjectLevelReadQuery {
    
    /** Object that can be used in place of a selection criteria. */
    protected transient Object selectionObject;

    /** Key that can be used in place of a selection criteria. */
    protected Vector selectionKey;

    /** Can be used to refresh a specific non-cached instance from the database. */
    protected boolean shouldLoadResultIntoSelectionObject = false;    
    
    /**
     * PUBLIC:
     * Return a new read object query.
     * A reference class must be specified before execution.
     * It is better to provide the class and expression builder on construction to esnure a single expression builder is used.
     * If no selection criteria is specified this will reads the first object found in the database.
     */
    public ReadObjectQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * By default, the query has no selection criteria. Executing this query without
     * selection criteria will always result in a database access to read the first
     * instance of the specified Class found in the database. This is true no
     * matter how cache usage is configured and even if an instance of the
     * specified Class exists in the cache.
     * Executing a query with selection criteria allows you to avoid a database
     * access if the selected instance is in the cache. For this reason, you may whish to use a ReadObjectQuery constructor that takes selection criteria, such as: {@link #ReadObjectQuery(Class, Call)}, {@link #ReadObjectQuery(Class, Expression)}, {@link #ReadObjectQuery(Class, ExpressionBuilder)}, {@link #ReadObjectQuery(ExpressionBuilder)}, {@link #ReadObjectQuery(Object)}, or {@link #ReadObjectQuery(Object, QueryByExamplePolicy)}.
     */
    public ReadObjectQuery(Class classToRead) {
        this();
        this.referenceClass = classToRead;
    }

    /**
     * PUBLIC:
     * Return a new read object query for the class and the selection criteria.
     */
    public ReadObjectQuery(Class classToRead, Expression selectionCriteria) {
        this();
        this.referenceClass = classToRead;
        setSelectionCriteria(selectionCriteria);
    }

    /**
     * PUBLIC:
     * Return a new read object query for the class.
     * The expression builder must be used for all associated expressions used with the query.
     */
    public ReadObjectQuery(Class classToRead, ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
        this.referenceClass = classToRead;
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * The call represents a database interaction such as SQL, Stored Procedure.
     */
    public ReadObjectQuery(Class classToRead, Call call) {
        this();
        this.referenceClass = classToRead;
        setCall(call);
    }

    /**
     * PUBLIC:
     * Return a new read object query.
     * The call represents a database interaction such as SQL, Stored Procedure.
     */
    public ReadObjectQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * PUBLIC:
     * Return a query to read the object with the same primary key as the provided object.
     * Note: This is not a query by example object, only the primary key will be used for the selection criteria.
     */
    public ReadObjectQuery(Object objectToRead) {
        this();
        setSelectionObject(objectToRead);
    }

    /**
     * PUBLIC:
     * Return a query by example query to find an object matching the attributes of the example object.
     */
    public ReadObjectQuery(Object exampleObject, QueryByExamplePolicy policy) {
        this();
        setExampleObject(exampleObject);
        setQueryByExamplePolicy(policy);
    }

    /**
     * PUBLIC:
     * The expression builder should be provide on creation to ensure only one is used.
     */
    public ReadObjectQuery(ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
    }

    /**
     * INTERNAL:
     * <P> This method is called by the object builder when building an original.
     * It will cause the original to be cached in the query results if the query
     * is set to do so.
     */
    public void cacheResult(Object unwrappedOriginal) {
        Object cachableObject = unwrappedOriginal;
        if (shouldUseWrapperPolicy()){
            cachableObject = getSession().wrapObject(unwrappedOriginal);
        }
        setTemporaryCachedQueryResults(cachableObject);
    }

    /**
     * OBSOLETE:
     * This can be used to explicitly disable the cache hit.
     * The cache hit may not be desired in some cases, such as
     * stored procedures that accept the primary key but do not query on it.
     * @deprecated replaced by checkCacheByPrimaryKey
     */
    public void checkCache() {
        setCacheUsage(CheckCacheByPrimaryKey);
    }

    /**
     * PUBLIC:
     * The cache will be checked only if the query contains exactly the primary key.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheByExactPrimaryKey() {
        setCacheUsage(CheckCacheByExactPrimaryKey);
    }

    /**
     * PUBLIC:
     * This is the default, the cache will be checked only if the query contains the primary key.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheByPrimaryKey() {
        setCacheUsage(CheckCacheByPrimaryKey);
    }

    /**
     * PUBLIC:
     * The cache will be checked completely, then if the object is not found or the query too complex the database will be queried.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheThenDatabase() {
        setCacheUsage(CheckCacheThenDatabase);
    }

    /**
     * INTERNAL:
     * Ensure that the descriptor has been set.
     */
    public void checkDescriptor(AbstractSession session) throws QueryException {
        if (getReferenceClass() == null) {
            throw QueryException.referenceClassMissing(this);
        }

        if (getDescriptor() == null) {
            ClassDescriptor referenceDescriptor;
            //Bug#3947714  In case getSelectionObject() is proxy            
            if (getSelectionObject() != null && session.getProject().hasProxyIndirection()) {
                referenceDescriptor = session.getDescriptor(getSelectionObject());            
            } else {
                referenceDescriptor = session.getDescriptor(getReferenceClass());                
            }
            if (referenceDescriptor == null) {
                throw QueryException.descriptorIsMissing(getReferenceClass(), this);
            }
            setDescriptor(referenceDescriptor);
        }
    }

    /**
     * INTERNAL:
     * The cache check is done before the prepare as a hit will not require the work to be done.
     */
    protected Object checkEarlyReturnImpl(AbstractSession session, AbstractRecord translationRow) {
        if (shouldCheckCache() && shouldMaintainCache() && (!shouldRefreshIdentityMapResult()) && (!(session.isRemoteSession() && (shouldRefreshRemoteIdentityMapResult() || getDescriptor().shouldDisableCacheHitsOnRemote()))) && (!(shouldCheckDescriptorForCacheUsage() && getDescriptor().shouldDisableCacheHits())) && (!getDescriptor().isDescriptorForInterface())) {
            Object cachedObject = getQueryMechanism().checkCacheForObject(translationRow, session);

            // Optimization: If find deleted object by exact primary
            // key expression or selection object/key just abort.
            if (cachedObject == InvalidObject.instance) {
                return cachedObject;
            }
            if (cachedObject != null) {
                if (shouldLoadResultIntoSelectionObject()) {
                    ObjectBuilder builder = getDescriptor().getObjectBuilder();
                    builder.copyInto(cachedObject, getSelectionObject());
                    //put this object into the cache.  This may cause some loss of identity
                    session.getIdentityMapAccessorInstance().putInIdentityMap(getSelectionObject());
                    cachedObject = getSelectionObject();
                }

                // check locking.  If clone has not been locked, do not early return cached object
                if (isLockQuery() && (session.isUnitOfWork() && !((UnitOfWorkImpl)session).isPessimisticLocked(cachedObject))) {
                    if (QueryMonitor.shouldMonitor()) {
                        QueryMonitor.incrementReadObjectMisses(this);
                    }
                    return null;
                }
                if (QueryMonitor.shouldMonitor()) {
                    QueryMonitor.incrementReadObjectHits(this);
                }
            } else {
                if (QueryMonitor.shouldMonitor()) {
                    QueryMonitor.incrementReadObjectMisses(this);
                }
            }
            if (shouldUseWrapperPolicy()) {
                cachedObject = getDescriptor().getObjectBuilder().wrapObject(cachedObject, session);
            }            
            return cachedObject;
        } else {
            if (QueryMonitor.shouldMonitor()) {
                QueryMonitor.incrementReadObjectMisses(this);
            }
            return null;
        }
    }

    /**
     * INTERNAL:
     * Check to see if a custom query should be used for this query.
     * This is done before the query is copied and prepared/executed.
     * null means there is none.
     */
    protected DatabaseQuery checkForCustomQuery(AbstractSession session, AbstractRecord translationRow) {
        checkDescriptor(session);

        if (isCustomQueryUsed() == null) {
            // Check if user defined a custom query in the query manager.
            if (!isUserDefined()) {
                if (!isCallQuery()) {
                    DescriptorQueryManager descriptorQueryManager = getDescriptor().getQueryManager();
        
                    // By default all descriptors have a custom ("static") read-object query.
                    // This allows the read-object query and SQL to be prepare once.
                    if (descriptorQueryManager.hasReadObjectQuery()) {
                        // If the query require special SQL generation or execution do not use the static read object query.
                        // PERF: the read-object query should always be static to ensure no regeneration of SQL.
                        if ((!hasJoining() || !getJoinedAttributeManager().hasJoinedAttributeExpressions()) && (!hasPartialAttributeExpressions()) && (!hasAsOfClause()) && (!hasNonDefaultFetchGroup()) && (getHintString() == null) && wasDefaultLockMode() && shouldIgnoreBindAllParameters() && (!hasFetchGroup()) && (getFetchGroupName() == null) && shouldUseDefaultFetchGroup()) {
                            if ((getSelectionKey() != null) || (getSelectionObject() != null)) {// Must be primary key.
                                setIsCustomQueryUsed(true);
                            } else {            
                                if (getSelectionCriteria() != null) {
                                    AbstractRecord primaryKeyRow = getDescriptor().getObjectBuilder().extractPrimaryKeyRowFromExpression(getSelectionCriteria(), translationRow, session);
                
                                    // Only execute the query if the selection criteria has the primary key fields set
                                    if (primaryKeyRow != null) {
                                        setIsCustomQueryUsed(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isCustomQueryUsed() == null) {
                setIsCustomQueryUsed(false);
            }
        }
        
        if (isCustomQueryUsed().booleanValue()) {
            return getDescriptor().getQueryManager().getReadObjectQuery();
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Conform the result in the UnitOfWork.
     */
    protected Object conformResult(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord databaseRow, boolean buildDirectlyFromRows) {
        // Note that if the object does not conform even though other objects might exist on the database null is returned.
        // Note that new objects is checked before the read is executed so does not have to be re-checked.
        // Must unwrap as the built object is always wrapped.
        // Note the object is unwrapped on the parent which it belongs to, as we
        // do not want to trigger a registration just yet.
        Object implementation = null;
        if (buildDirectlyFromRows) {
            implementation = result;
        } else {
            implementation = getDescriptor().getObjectBuilder().unwrapObject(result, unitOfWork.getParent());
        }

        if ((getSelectionCriteria() != null) && (getSelectionKey() == null) && (getSelectionObject() == null)) {
            ExpressionBuilder builder = getSelectionCriteria().getBuilder();
            builder.setSession(unitOfWork.getRootSession(null));
            builder.setQueryClass(getReferenceClass());
        }

        Object clone = conformIndividualResult(implementation, unitOfWork, databaseRow, getSelectionCriteria(), null, buildDirectlyFromRows);
        if (clone == null) {
            return clone;
        }

        if (shouldUseWrapperPolicy()) {
            return getDescriptor().getObjectBuilder().wrapObject(clone, unitOfWork);
        } else {
            return clone;
        }
    }

    /**
     * PUBLIC:
     * Do not refesh/load into the selection object, this is the default.
     * This property allows for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void dontLoadResultIntoSelectionObject() {
        setShouldLoadResultIntoSelectionObject(false);
    }

    /**
     * INTERNAL:
     * Execute the query. If there are cached results return those.
     * This must override the super to support result caching.
     *
     * @param session the session in which the receiver will be executed.
     * @return An object or vector, the result of executing the query.
     * @exception DatabaseException - an error has occurred on the database
     */
    public Object execute(AbstractSession session, AbstractRecord row) throws DatabaseException {
        if (shouldCacheQueryResults()) {
            if (shouldConformResultsInUnitOfWork()) {
                throw QueryException.cannotConformAndCacheQueryResults(this);
            }
            if (isPrepared()) {// only prepared queries can have cached results.
                Object result = getQueryResults(session, row, true);
                // Bug6138532 - if result is "cached no results", return null immediately
                if (result == InvalidObject.instance) {
                    return null;
                }
                if (result != null) {
                    if (session.isUnitOfWork()) {
                        result = ((UnitOfWorkImpl)session).registerExistingObject(result);
                    }
                    return result;
                }
            }
        }
        return super.execute(session, row);
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Do a cache lookup and build object from row if required.
     * @exception  DatabaseException - an error has occurred on the database
     * @return object - the first object found or null if none.
     */
    protected Object executeObjectLevelReadQuery() throws DatabaseException {
        if (getDescriptor().isDescriptorForInterface()) {
            Object returnValue = getDescriptor().getInterfacePolicy().selectOneObjectUsingMultipleTableSubclassRead(this);
            setExecutionTime(System.currentTimeMillis());
            return returnValue;
        }

        AbstractRecord row = null;
        AbstractSession session = getSession();
        // If using 1-m joins, must select all rows.
        if (hasJoining() && getJoinedAttributeManager().isToManyJoin()) {
            List rows = getQueryMechanism().selectAllRows();
            if (rows.size() > 0) {
                row = (AbstractRecord)rows.get(0);
            }
            getJoinedAttributeManager().setDataResults(rows, session);
        } else {
            row = getQueryMechanism().selectOneRow();
        }
        setExecutionTime(System.currentTimeMillis());
        Object result = null;

        if (session.isUnitOfWork()) {
            result = registerResultInUnitOfWork(row, (UnitOfWorkImpl)session, getTranslationRow(), true);
        } else {
            if (row != null) {
                result = buildObject(row);
            }
        }
        if ((result == null) && shouldRefreshIdentityMapResult()) {
            // bug5955326, should invalidate the shared cached if refreshed object no longer exists.
            if (getSelectionKey() != null) {
                session.getParentIdentityMapSession(this, true, true).getIdentityMapAccessor().invalidateObject(getSelectionKey(), getReferenceClass());
            } else if (getSelectionObject() != null) {
                session.getParentIdentityMapSession(this, true, true).getIdentityMapAccessor().invalidateObject(getSelectionObject());
            }
        }                

        if (shouldIncludeData()) {
            ComplexQueryResult complexResult = new ComplexQueryResult();
            complexResult.setResult(result);
            complexResult.setData(row);
            return complexResult;
        }

        return result;
    }
    
    /**
     * INTERNAL:
     * Execute the query building the objects directly from the database result-set.
     * @exception  DatabaseException - an error has occurred on the database
     * @return object - the first object found or null if none.
     */
    protected Object executeObjectLevelReadQueryFromResultSet() throws DatabaseException {
        UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();
        DatabaseAccessor accessor = (DatabaseAccessor)unitOfWork.getAccessor();
        DatabasePlatform platform = accessor.getPlatform();
        DatabaseCall call = (DatabaseCall)getCall().clone();
        call.setQuery(this);
        AbstractRecord translationRow = getTranslationRow();
        call.translate(getTranslationRow(), null, unitOfWork);
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        boolean exceptionOccured = false;
        try {
            accessor.incrementCallCount(unitOfWork);
            statement = (PreparedStatement)call.prepareStatement(accessor, translationRow, unitOfWork);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            ResultSetMetaData metaData = resultSet.getMetaData();
            return getDescriptor().getObjectBuilder().buildWorkingCopyCloneFromResultSet(this, null, resultSet, unitOfWork, accessor, metaData, platform);
        } catch (SQLException exception) {
            exceptionOccured = true;
            DatabaseException commException = accessor.processExceptionForCommError(session, exception, call);
            if (commException != null) throw commException;
            throw DatabaseException.sqlException(exception, call, accessor, unitOfWork, false);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    accessor.releaseStatement(statement, call.getSQLString(), call, unitOfWork);
                }
            } catch (SQLException exception) {
                if (!exceptionOccured) {
                    //in the case of an external connection pool the connection may be null after the statement release
                    // if it is null we will be unable to check the connection for a comm error and
                    //therefore must return as if it was not a comm error.
                    DatabaseException commException = accessor.processExceptionForCommError(session, exception, call);
                    if (commException != null) throw commException;
                    throw DatabaseException.sqlException(exception, call, accessor, session, false);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Extract the correct query result from the transporter.
     */
    public Object extractRemoteResult(Transporter transporter) {
        return ((RemoteSession)getSession()).getObjectCorrespondingTo(transporter.getObject(), transporter.getObjectDescriptors(), new IdentityHashtable(), this);
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     * If composite the primary must be in the same order as defined in the descriptor.
     */
    public Vector getSelectionKey() {
        return selectionKey;

    }

    /**
     * PUBLIC:
     * Return the selection object of the query.
     * This can be used instead of a where clause expression for single object primary key queries.
     * The selection object given should have a primary key defined,
     * this primary key will be used to query the database instance of the same object.
     * This is a basic form of query by example where only the primary key is required,
     * it can be used for simple query forms, or testing.
     */
    public Object getSelectionObject() {
        return selectionObject;
    }

    /**
     * PUBLIC:
     * Return if this is a read object query.
     */
    public boolean isReadObjectQuery() {
        return true;
    }

    /**
     * PUBLIC:
     * Allow for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void loadResultIntoSelectionObject() {
        setShouldLoadResultIntoSelectionObject(true);
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
        if (query.isReadObjectQuery()) {
            ReadObjectQuery readQuery = (ReadObjectQuery)query;
            this.selectionKey = readQuery.selectionKey;
            this.selectionObject = readQuery.selectionObject;
            this.shouldLoadResultIntoSelectionObject = readQuery.shouldLoadResultIntoSelectionObject;
        }
    }
    
    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() throws QueryException {
        if (prepareFromCachedQuery()) {
            return;
        }
        super.prepare();
        
        if ((getSelectionKey() != null) || (getSelectionObject() != null)) {
            // The expression is set in the prepare as params.
            setSelectionCriteria(getDescriptor().getObjectBuilder().getPrimaryKeyExpression());
            // For bug 2989998 the translation row is required to be set at this point.
            if (!shouldPrepare()) {
                if (getSelectionKey() != null) {
                    // Row must come from the key.
                    setTranslationRow(getDescriptor().getObjectBuilder().buildRowFromPrimaryKeyValues(getSelectionKey(), getSession()));
                } else {//(getSelectionObject() != null)
                    setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(getSelectionObject(), getSession()));
                }
            }
        }

        if (getDescriptor().isDescriptorForInterface()) {
            return;
        }

        // If using 1-m joining select all rows.
        if (hasJoining() && getJoinedAttributeManager().isToManyJoin()) {
            getQueryMechanism().prepareSelectAllRows();
        } else {
            getQueryMechanism().prepareSelectOneRow();
        }
    }

    /**
     * INTERNAL:
     * Set the properties needed to be cascaded into the custom query inlucding the translation row.
     */
    protected void prepareCustomQuery(DatabaseQuery customQuery) {
        ReadObjectQuery customReadQuery = (ReadObjectQuery)customQuery;
        customReadQuery.setShouldRefreshIdentityMapResult(shouldRefreshIdentityMapResult());
        customReadQuery.setCascadePolicy(getCascadePolicy());
        customReadQuery.setShouldMaintainCache(shouldMaintainCache());
        customReadQuery.setShouldUseWrapperPolicy(shouldUseWrapperPolicy());
        // CR... was missing some values, execution could cause infinite loop.
        customReadQuery.setQueryId(getQueryId());
        customReadQuery.setExecutionTime(getExecutionTime());
        customReadQuery.setShouldLoadResultIntoSelectionObject(shouldLoadResultIntoSelectionObject());
        AbstractRecord primaryKeyRow;
        if (getSelectionObject() != null) {
            // CR#... Must also set the selection object as may be loading into the object (refresh)
            customReadQuery.setSelectionObject(getSelectionObject());
            // The translation/primary key row will be set in prepareForExecution.
        } else if (getSelectionKey() != null) {
            customReadQuery.setSelectionKey(getSelectionKey());
        } else {
            // The primary key row must be used.
            primaryKeyRow = customQuery.getDescriptor().getObjectBuilder().extractPrimaryKeyRowFromExpression(getSelectionCriteria(), customQuery.getTranslationRow(), customReadQuery.getSession());
            customReadQuery.setTranslationRow(primaryKeyRow);
        }
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        // For bug 2989998 the translation row now sometimes set earlier in prepare.
        if (shouldPrepare()) {
            if (getSelectionKey() != null) {
                // Row must come from the key.
                setTranslationRow(getDescriptor().getObjectBuilder().buildRowFromPrimaryKeyValues(getSelectionKey(), getSession()));
            } else if (getSelectionObject() != null) {
                // The expression is set in the prepare as params.
                setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(getSelectionObject(), getSession()));
            }
        }
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prePrepare() throws QueryException {
        super.prePrepare();
        //Bug#3947714  In case getSelectionObject() is proxy            
        if (getSelectionObject() != null && getSession().getProject().hasProxyIndirection()) {
            setSelectionObject(ProxyIndirectionPolicy.getValueFromProxy(getSelectionObject()));
        }
    }

    /**
     * INTERNAL:
     * All objects queried via a UnitOfWork get registered here.  If the query
     * went to the database.
     * <p>
     * Involves registering the query result individually and in totality, and
     * hence refreshing / conforming is done here.
     * @param result may be collection (read all) or an object (read one),
     * or even a cursor.  If in transaction the shared cache will
     * be bypassed, meaning the result may not be originals from the parent
     * but raw database rows.
     * @param unitOfWork the unitOfWork the result is being registered in.
     * @param arguments the original arguments/parameters passed to the query
     * execution.  Used by conforming
     * @param buildDirectlyFromRows If in transaction must construct
     * a registered result from raw database rows.
     * @return the final (conformed, refreshed, wrapped) UnitOfWork query result
     */
    public Object registerResultInUnitOfWork(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord arguments, boolean buildDirectlyFromRows) {
        if (result == null) {
            return null;
        }
        if (shouldConformResultsInUnitOfWork() || getDescriptor().shouldAlwaysConformResultsInUnitOfWork()) {
            return conformResult(result, unitOfWork, arguments, buildDirectlyFromRows);
        }
        Object clone = null;
        if (buildDirectlyFromRows) {
            clone = buildObject((AbstractRecord)result);
        } else {
            clone = registerIndividualResult(result, unitOfWork, null);
        }

        if (shouldUseWrapperPolicy()) {
            clone = getDescriptor().getObjectBuilder().wrapObject(clone, unitOfWork);
        }
        return clone;
    }

    protected Object remoteExecute() {
        // Do a cache lookup.
        checkDescriptor(session);
        // As the selection object is transient, compute the key.
        if (getSelectionObject() != null) {
            // Must be checked seperately as the expression and row is not yet set.
            setSelectionKey(getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(getSelectionObject(), session));
        }

        Object cacheHit = checkEarlyReturn(getSession(), getTranslationRow());
        if ((cacheHit != null) || shouldCheckCacheOnly()) {
            return cacheHit;
        }

        return super.remoteExecute();
    }

    /**
     * INTERNAL:
     * replace the value holders in the specified result object(s)
     */
    public IdentityHashtable replaceValueHoldersIn(Object object, RemoteSessionController controller) {
        return controller.replaceValueHoldersIn(object);
    }

    /**
     * OBSOLETE:
     * This can be used to explicitly disable the cache hit.
     * The cache hit may not be desired in some cases, such as
     * stored procedures that accept the primary key but do not query on it.
     * @deprecated replaced by setCacheUsage
     */
    public void setCheckCache(boolean checkCache) {
        if (checkCache) {
            setCacheUsage(CheckCacheByPrimaryKey);
        } else {
            setCacheUsage(DoNotCheckCache);
        }
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     * If composite the primary must be in the same order as defined in the descriptor.
     */
    public void setSelectionKey(Vector selectionKey) {
        if (selectionKey == null) {
            this.selectionKey = null;
        } else {
            this.selectionKey = new NonSynchronizedVector(selectionKey);
        }
        // setIsPrepared(false); PERF: This cause the findByPrimaryKey query to unprepare,
        // Since this is an argument, and not a query modifier do not unprepare.
    }

    /**
     * PUBLIC:
     * Used to set the where clause of the query.
     * This can be used instead of a where clause expression for single object primary key queries.
     * The selection object given should have a primary key defined,
     * this primary key will be used to query the database instance of the same object.
     * This is a basic form of query by example where only the primary key is required,
     * it can be used for simple query forms, or testing.
     */
    public void setSelectionObject(Object selectionObject) {
        if (selectionObject == null) {
            throw QueryException.selectionObjectCannotBeNull(this);
        }
        setSelectionKey(null);
        // Check if the query needs to be unprepared.
        if ((this.selectionObject == null) || (this.selectionObject.getClass() != selectionObject.getClass())) {
            setIsPrepared(false);
        }
        setReferenceClass(selectionObject.getClass());
        this.selectionObject = selectionObject;
    }

    /**
     * PUBLIC:
     * Allow for the selection object of the query to be refreshed or put into the TopLink cache.
     * By default on a read or refresh the object in the cache is refreshed and returned or a new object is built from the database,
     * in some cases such as EJB BMP it is desirable to refresh or load into the object passed into the read object query.
     * <p>Note: This forces the selection object into the cache a replaces any existing object that may already be there,
     * this is a strict violation of object identity and other objects can still be refering to the old object.
     */
    public void setShouldLoadResultIntoSelectionObject(boolean shouldLoadResultIntoSelectionObject) {
        this.shouldLoadResultIntoSelectionObject = shouldLoadResultIntoSelectionObject;
    }

    /**
     * PUBLIC:
     * The primary key can be specified if used instead of an expression or selection object.
     */
    public void setSingletonSelectionKey(Object selectionKey) {
        Vector key = new Vector();
        key.addElement(selectionKey);
        setSelectionKey(key);

    }

    /**
     * PUBLIC:
     * Return if the cache should be checked.
     */
    public boolean shouldCheckCache() {
        return getCacheUsage() != DoNotCheckCache;
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheByExactPrimaryKey() {
        return getCacheUsage() == CheckCacheByExactPrimaryKey;
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheByPrimaryKey() {
        return (getCacheUsage() == CheckCacheByPrimaryKey) || (getCacheUsage() == UseDescriptorSetting);
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheThenDatabase() {
        return getCacheUsage() == CheckCacheThenDatabase;
    }

    /**
     * PUBLIC:
     * return true if the result should be loaded into the passed in selection Object
     */
    public boolean shouldLoadResultIntoSelectionObject() {
        return shouldLoadResultIntoSelectionObject;
    }

    /**
     * INTERNAL:
     * Return if the query has an non-default fetch group defined for itself.
     */
    protected boolean hasNonDefaultFetchGroup() {
        return getDescriptor().hasFetchGroupManager() && ((this.getFetchGroup() != null) || (this.getFetchGroupName() != null) || (!this.shouldUseDefaultFetchGroup()));

    }
}
