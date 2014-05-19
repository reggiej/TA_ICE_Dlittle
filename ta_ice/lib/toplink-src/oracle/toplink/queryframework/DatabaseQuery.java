// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.descriptors.DescriptorQueryManager;
import oracle.toplink.expressions.*;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.queryframework.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.remote.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.remote.*;
import oracle.toplink.sessions.DatabaseRecord;
import oracle.toplink.sessions.Record;
import oracle.toplink.sessions.SessionProfiler;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all database query objects.
 * DatabaseQuery is a visible class to the TopLink user. Users create an appropriate
 * query by creating an instance of a concrete subclasses of DatabaseQuery.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Provide a common protocol for query objects.
 * <li> Defines a generic execution interface.
 * <li> Provides  query property values
 * <li> Holds arguments to the query
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class DatabaseQuery implements Cloneable, Serializable, FalseUndefinedTrue {

    /** Queries can be given a name and registered with a descriptor to allow common queries to be reused. */
    protected String name;

    /** Arguments can be given and specified to predefined queries to allow reuse. */
    protected Vector arguments;

    /** Arguments values can be given and specified to predefined queries to allow reuse. */
    protected Vector argumentValues;

    /** Needed to differentiate queries with the same name. */
    protected Vector argumentTypes;

    /** Used to build a list of argumentTypes by name pre-initialization     */
    protected Vector argumentTypeNames;

    /** The descriptor cached on the prepare for object level queries. */
    protected transient ClassDescriptor descriptor;

    /** The query mechanism determines the mechanism on how the database will be accessed. */
    protected DatabaseQueryMechanism queryMechanism;

    /** A redirector allows for a queries execution to be the execution of a piece of code. */
    protected QueryRedirector redirector;

    /** Flag used for a query to bypass the identitymap and unit of work. */

    // Bug#3476483 - Restore shouldMaintainCache to previous state after reverse of bug fix 3240668
    protected boolean shouldMaintainCache;

    /** Internallay used by the mappings as a temporary store. */
    protected Hashtable properties;

    /** Only used after the query is cloned for execution to store the session under which the query was executed. */
    protected transient AbstractSession session;
    
    /** Only used after the query is cloned for execution to store the execution session under which the query was executed. */
    protected transient AbstractSession executionSession;

    /** Connection to use for database access, required for server session connection pooling. */
    protected transient Accessor accessor;

    /** Mappings and the descriptor use parameterized mechanisms that will be translated with the data from the row. */
    protected AbstractRecord translationRow;

    /** Internal flag used to bypass user define queries when executing one for custom sql/query support. */
    protected boolean isUserDefined;

    /** Policy that determines how the query will cascade to its object's parts. */
    protected int cascadePolicy;

    /** Used to override the default session in the session broker. */
    protected String sessionName;

    /** Queries prepare common stated in themselves. */
    protected boolean isPrepared;

    /** Used to indicate whether or not the call needs to be cloned. */
    protected boolean shouldCloneCall;
    
    
    /** Allow for the prepare of queries to be turned off, this allow for dynamic non-pre SQL generated queries. */
    protected boolean shouldPrepare;

    /** Bind all arguments to the SQL statement. */

    // Has False, Underfined or True value. In case of Undefined -
    // Session's shouldBindAllParameters() defines whether to bind or not.
    protected int shouldBindAllParameters;

    /** Cache the prepared statement, this requires full parameter binding as well. */

    // Has False, Underfined or True value. In case of Undefined -
    // Session's shouldCacheAllStatements() defines whether to cache or not.
    protected int shouldCacheStatement;

    /** Use the WrapperPolicy for the objects returned by the query */
    protected boolean shouldUseWrapperPolicy;

    /**
     * queryTimeout has three possible settings: DefaultTimeout, NoTimeout, and 1..N
     * This applies to both DatabaseQuery.queryTimeout and DescriptorQueryManager.queryTimeout
     *
     * DatabaseQuery.queryTimeout:
     * - DefaultTimeout: get queryTimeout from DescriptorQueryManager
     * - NoTimeout, 1..N: overrides queryTimeout in DescriptorQueryManager
     *
     * DescriptorQueryManager.queryTimeout:
     * - DefaultTimeout: get queryTimeout from parent DescriptorQueryManager. If there is no
     * parent, default to NoTimeout
     * - NoTimeout, 1..N: overrides parent queryTimeout
     */
    protected int queryTimeout;

    /* Used as default for read, means shallow write for modify. */
    public static final int NoCascading = 1;

    /* Used as default for write, used for refreshing to refresh the whole object. */
    public static final int CascadePrivateParts = 2;

    /* Currently not supported, used for deep write/refreshes/reads in the future. */
    public static final int CascadeAllParts = 3;

    /* Used by the unit of work. */
    public static final int CascadeDependentParts = 4;

    /* Used by aggregate Collections:  As aggregates delete at update time, cascaded deletes
     * must know to stop when entering postDelete for a particular mapping.  Only used by the
     * aggregate collection when update is occuring in a UnitOfWork
     * CR 2811
     */
    public static final int CascadeAggregateDelete = 5;

    /*
     * Used when refreshing should check the mappings to determin if a particular
     * mapping should be cascaded.
     */
    public static final int CascadeByMapping = 6;

    /** Used for adding hints to the query string in oracle */
    protected String hintString;

    /*
     * Stores the FlushMode of this Query.  This is only applicable when executed
     * in a flushable UnitOfWork and will be ignored otherwise.
     */
    protected Boolean flushOnExecute;

    /** PERF: Determines if the query has already been cloned for execution, to avoid duplicate cloning. */
    protected boolean isExecutionClone;
    
    /** PERF: Store if this query will use the descriptor custom query. */
    protected Boolean isCustomQueryUsed;    
    
    /** PERF: Store if the query if from the JPQL parse cache, to avoid corruption of the cache query. */
    protected boolean isFromParseCache = false;

    /** Allow connection unwrapping to be configured. */ 
    protected boolean isNativeConnectionRequired; 

    /**
     * PUBLIC:
     * Initialize the state of the query
     */
    public DatabaseQuery() {
        this.shouldMaintainCache = true;
        // bug 3524620: lazy-init query mechanism
        //this.queryMechanism = new ExpressionQueryMechanism(this);
        this.isUserDefined = false;
        this.cascadePolicy = NoCascading;
        this.isPrepared = false;
        this.shouldUseWrapperPolicy = true;
        this.queryTimeout = DescriptorQueryManager.DefaultTimeout;
        this.shouldPrepare = true;
        this.shouldCloneCall = false;
        this.shouldBindAllParameters = Undefined;
        this.shouldCacheStatement = Undefined;
        this.isExecutionClone = false;
    }

    /**
     * INTERNAL:
     * Return if the query if from the JPQL parse cache, to avoid corruption of the cache query.
     */
    public boolean isFromParseCache() {
        return isFromParseCache;
    }

    /**
     * INTERNAL:
     * Set if the query if from the JPQL parse cache, to avoid corruption of the cache query.
     */
    public void setIsFromParseCache(boolean isFromParseCache) {
        this.isFromParseCache = isFromParseCache;
    }
    
    /**
     * PUBLIC:
     * Add the argument named argumentName.
     * This will cause the translation of references of argumentName in the receiver's expression,
     * with the value of the argument as supplied to the query in order from executeQuery()
     */
    public void addArgument(String argumentName) {
        // CR#3545 - Changed the default argument type to make argument types work more consistently
        // with the SDK
        addArgument(argumentName, java.lang.Object.class);
    }

    /**
     * PUBLIC:
     * Add the argument named argumentName and its class type.
     * This will cause the translation of references of argumentName in the receiver's expression,
     * with the value of the argument as supplied to the query in order from executeQuery().
     * Specifying the class type is important if identically named queries are used but with
     * different argument lists.
     */
    public void addArgument(String argumentName, Class type) {
        getArguments().addElement(argumentName);
        getArgumentTypes().addElement(type);
        getArgumentTypeNames().addElement(type.getName());
    }

    /**
     * PUBLIC:
     * Add the argument named argumentName and its class type.
     * This will cause the translation of references of argumentName in the receiver's expression,
     * with the value of the argument as supplied to the query in order from executeQuery().
     * Specifying the class type is important if identically named queries are used but with
     * different argument lists.
     */
    public void addArgument(String argumentName, String typeAsString) {
        getArguments().addElement(argumentName);
        //bug 3197587
        getArgumentTypes().addElement(Helper.getObjectClass(ConversionManager.loadClass(typeAsString)));
        getArgumentTypeNames().addElement(typeAsString);
    }

    /**
     * INTERNAL:
     * Add an argument to the query, but do not resovle the class yet.
     * This is useful for building a query without putting the domain classes
     * on the classpath for the Mapping Workbench.
     */
    public void addArgumentByTypeName(String argumentName, String typeAsString) {
        getArguments().addElement(argumentName);
        getArgumentTypeNames().addElement(typeAsString);
    }

    /**
     * PUBLIC:
     * Add the argumentValue.
     * Argument values must be added in the same order the arguments are defined.
     */
    public void addArgumentValue(Object argumentValue) {
        getArgumentValues().addElement(argumentValue);
    }

    /**
     * PUBLIC:
     * Add the argumentValues to the query.
     * Argument values must be added in the same order the arguments are defined.
     */
    public void addArgumentValues(Vector theArgumentValues) {
        setArgumentValues(theArgumentValues);
    }

    /**
     * PUBLIC:
     * Used to define a store procedure or SQL query.
     * This may be used for multiple SQL executions to be mapped to a single query.
     * This cannot be used for cursored selects, delete alls or does exists.
     */
    public void addCall(Call call) {
        setQueryMechanism(call.buildQueryMechanism(this, getQueryMechanism()));
        // Must un-prepare is prepare as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Used to define a statement level query.
     * This may be used for multiple SQL executions to be mapped to a single query.
     * This cannot be used for cursored selects, delete alls or does exists.
     */
    public void addStatement(SQLStatement statement) {
        // bug 3524620: lazy-init query mechanism
        if (!hasQueryMechanism()) {
            setQueryMechanism(new StatementQueryMechanism(this));
        } else if (!getQueryMechanism().isStatementQueryMechanism()) {
            setQueryMechanism(new StatementQueryMechanism(this));
        }
        ((StatementQueryMechanism)getQueryMechanism()).getSQLStatements().addElement(statement);
        // Must un-prepare is prepare as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Bind all arguments to any SQL statement.
     */
    public void bindAllParameters() {
        setShouldBindAllParameters(true);
    }

    /**
     * INTERNAL:
     * In the case of EJBQL, an expression needs to be generated. Build the required expression.
     */
    protected void buildSelectionCriteria(AbstractSession session) {
        this.getQueryMechanism().buildSelectionCriteria(session);
    }

    /**
     * PUBLIC:
     * Cache the prepared statements, this requires full parameter binding as well.
     */
    public void cacheStatement() {
        setShouldCacheStatement(true);
    }

    /**
     * PUBLIC:
     * Cascade the query and its properties on the queries object(s) and all objects related to the queries object(s).
     * This includes private and independent relationships, but not read-only relationships.
     * This will still stop on uninstantiated indirection objects except for deletion.
     * Great caution should be used in using the property as the query may effect a large number of objects.
     * This policy is used by the unit of work to ensure persistence by reachability.
     */
    public void cascadeAllParts() {
        setCascadePolicy(CascadeAllParts);
    }

    /**
     * PUBLIC:
     * Cascade the query and its properties on the queries object(s) and all related objects where the mapping has
     * been set to cascade the merge.
     */
    public void cascadeByMapping() {
        setCascadePolicy(CascadeByMapping);
    }

    /**
     * INTERNAL:
     * Used by unit of work, only cascades constraint dependecies.
     */
    public void cascadeOnlyDependentParts() {
        setCascadePolicy(CascadeDependentParts);
    }

    /**
     * PUBLIC:
     * Cascade the query and its properties on the queries object(s)
     * and all privately owned objects related to the queries object(s).
     * This is the default for write and delete queries.
     * This policy should normally be used for refreshing, otherwise you could refresh half of any object.
     */
    public void cascadePrivateParts() {
        setCascadePolicy(CascadePrivateParts);
    }

    /**
     * INTERNAL:
     * Ensure that the descriptor has been set.
     */
    public void checkDescriptor(AbstractSession session) throws QueryException {
    }

    /**
     * INTERNAL:
     * Check to see if this query already knows the return vale without preforming any further work.
     */
    public Object checkEarlyReturn(AbstractSession session, AbstractRecord translationRow) {
        return null;
    }

    /**
     * INTERNAL:
     * Check to see if a custom query should be used for this query.
     * This is done before the query is copied and prepared/executed.
     * null means there is none.
     */
    protected DatabaseQuery checkForCustomQuery(AbstractSession session, AbstractRecord translationRow) {
        return null;
    }

    /**
     * INTERNAL:
     * Check to see if this query needs to be prepare and prepare it.
     * The prepare is done on the original query to ensure that the work is not repeated.
     */
    public void checkPrepare(AbstractSession session, AbstractRecord translationRow) {
        this.checkPrepare(session, translationRow, false);
    }
    
    /**
     * INTERNAL:
     * Check to see if this query needs to be prepare and prepare it.
     * The prepare is done on the original query to ensure that the work is not repeated.
     */
    public void checkPrepare(AbstractSession session, AbstractRecord translationRow, boolean force) {
        // This query is first prepared for global common state, this must be synced.
        if (!isPrepared()) {// Avoid the monitor is already prepare, must check again for concurrency.
            // Profile the query preparation time.
            session.startOperationProfile(SessionProfiler.QUERY_PREPARE, this, SessionProfiler.ALL);
            // If this query will use the custom query, do not prepare.
            if ((!force) && shouldPrepare() && (checkForCustomQuery(session, translationRow) != null)) {
                // Profile the query preparation time.
                session.endOperationProfile(SessionProfiler.QUERY_PREPARE, this, SessionProfiler.ALL);
                return;
            }
            // Prepared queries cannot be custom as then they would never have been prepared.
            synchronized (this) {
                if (!isPrepared()) {
                    // When custom SQL is used there is a possibility that the SQL contains the # token.
                    // Avoid this by telling the call if this is custom SQL with parameters.
                    // This must not be called for SDK calls.
                    if ((isReadQuery() || isDataModifyQuery()) && isCallQuery() && (getQueryMechanism() instanceof CallQueryMechanism) && ((translationRow == null) || translationRow.isEmpty())) {
                        // Must check for read object queries as the row will be empty until the prepare.
                        if (isReadObjectQuery() || isUserDefined()) {
                            ((CallQueryMechanism)getQueryMechanism()).setCallHasCustomSQLArguments();
                        }
                    } else if (isCallQuery() && (getQueryMechanism() instanceof CallQueryMechanism)) {
                        ((CallQueryMechanism)getQueryMechanism()).setCallHasCustomSQLArguments();
                    }
                    setSession(session);// Session is required for some init stuff.
                    prepare();
                    setSession(null);
                    setIsPrepared(true);// MUST not set prepare until done as other thread may hit before finihsing the prepare.
                }
            }
            // Profile the query preparation time.
            session.endOperationProfile(SessionProfiler.QUERY_PREPARE, this, SessionProfiler.ALL);
        }
    }

    /**
     * INTERNAL:
     * Clone the query
     */
    public Object clone() {
        try {
            DatabaseQuery cloneQuery = (DatabaseQuery)super.clone();

            // partial fix for 3054240 
            // need to pay attention to other components of the query, too  MWN
            if (cloneQuery.properties != null) {
                if (cloneQuery.properties.isEmpty()) {
                    cloneQuery.setProperties(null);
                } else {
                    cloneQuery.setProperties((Hashtable)getProperties().clone());
                }
            }

            // bug 3524620: now that the query mechanism is lazy-init'd,
            // only clone the query mechanism if we have one.
            if (hasQueryMechanism()) {
                cloneQuery.setQueryMechanism(getQueryMechanism().clone(cloneQuery));
            }
            cloneQuery.setIsPrepared(isPrepared());// Setting some things will trigger unprepare.
            return cloneQuery;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * INTERNAL
     * Used to give the subclasses oportunity to copy aspects of the cloned query
     * to the original query.
     */
    protected void clonedQueryExecutionComplete(DatabaseQuery query, AbstractSession session) {
        //no-op for this class
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this query to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        // note: normally we would fix the argument types here, but they are already
        // lazily instantiated
    };

    /**
     * INTERNAL:
     * Added for backwards compatibility.  shouldMaintainCache used to be tri-state and was converted to boolean.
     * This method is used by deployment XML to properly convert the tri-state variable to a boolean
     * Added for Bug 4034159
     */
    public void deploymentSetShouldMaintainCache(int maintainCache) {
        // FalseUndefinedTrue.Undefined is intentionally left ignored so it will map to the default.
        if (maintainCache == FalseUndefinedTrue.True) {
            setShouldMaintainCache(true);
        } else if (maintainCache == FalseUndefinedTrue.False) {
            setShouldMaintainCache(false);
        }
    }

    /**
     * INTERNAL:
     * Added for backwards compatibility.  shouldMaintainCache used to be tri-state and was converted to boolean.
     * This method is used by deployment XML to properly convert the tri-state variable to a boolean
     * Added for Bug 4034159
     */
    public int deploymentShouldMaintainCache() {
        if (shouldMaintainCache()) {
            return FalseUndefinedTrue.True;
        } else {
            return FalseUndefinedTrue.False;
        }
    }

    /**
     * PUBLIC:
     * Do not Bind all arguments to any SQL statement.
     */
    public void dontBindAllParameters() {
        setShouldBindAllParameters(false);
    }

    /**
     * PUBLIC:
     * Dont cache the prepared statements, this requires full parameter binding as well.
     */
    public void dontCacheStatement() {
        setShouldCacheStatement(false);
    }

    /**
     * PUBLIC:
     * Do not cascade the query and its properties on the queries object(s) relationships.
     * This does not effect the queries private parts but only the object(s) direct row-level attributes.
     * This is the default for read queries and can be used in writting if it is known that only
     * row-level attributes changed, or to resolve circular foreign key dependencies.
     */
    public void dontCascadeParts() {
        setCascadePolicy(NoCascading);
    }

    /**
     * PUBLIC:
     * Set for the identity map (cache) to be ignored completely.
     * The cache check will be skipped and the result will not be put into the identity map.
     * This can be used to retreive the exact state of an object on the database.
     * By default the identity map is always maintained.
     */
    public void dontMaintainCache() {
        setShouldMaintainCache(false);
    }

    /**
     * INTERNAL:
     * Execute the query
     *
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return - the result of executing the query.
     */
    public abstract Object executeDatabaseQuery() throws DatabaseException, OptimisticLockException;

    /**
     * INTERNAL:
     * Override query execution where Session is a UnitOfWork.
     * <p>
     * If there are objects in the cache return the results of the cache lookup.
     *
     * @param unitOfWork - the session in which the receiver will be executed.
     * @param translationRow - the arguments
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return An object, the result of executing the query.
     */
    public Object executeInUnitOfWork(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        return execute(unitOfWork, translationRow);
    }

    /**
     * INTERNAL:
     * Execute the query. If there are objects in the cache  return the results
     * of the cache lookup.
     *
     * @param session - the session in which the receiver will be executed.
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return An object, the result of executing the query.
     */
    public Object execute(AbstractSession session, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        DatabaseQuery queryToExecute = this;

        // refactored redirection for bug 3241138
        if (getRedirector() != null) {
            return redirectQuery(queryToExecute, session, translationRow);
        }

        // Bug 5529564 - If this is a user defined selection query (custom SQL), 
        // prepare the query before hand so that we may look up the correct fk 
        // values from the query parameters when checking early return.
        if (queryToExecute.isCustomSelectionQuery() && queryToExecute.shouldPrepare()) {
            queryToExecute.checkPrepare(session, translationRow);    
        }
        
        // This allows the query to check the cache or return early without doing any work.
        Object earlyReturn = queryToExecute.checkEarlyReturn(session, translationRow);
        if ((earlyReturn != null) || (isReadObjectQuery() && ((ReadObjectQuery)this).shouldCheckCacheOnly())) {
            return earlyReturn;
        }
        
        boolean hasCustomQuery = false;
        if (!isPrepared() && shouldPrepare()) {
            // Prepared queries cannot be custom as then they would never have been prepared.
            DatabaseQuery customQuery = checkForCustomQuery(session, translationRow);
            if (customQuery != null) {
                hasCustomQuery = true;
                // The custom query will be used not the original.
                queryToExecute = customQuery;

                // Check for redirection.
                // bug 3241138 - ensure all of the redirector aflow executes on the customQuery
                if (queryToExecute.getRedirector() != null) {                
                    return redirectQuery(queryToExecute, session, translationRow);
                }
            }
        }

        // PERF: Queries need to be cloned for execution as they may be
        // concurrently reused, and execution specific state is stored in the clone.
        // In some case the query is known to be a one off, or cloned elsewhere
        // so the query keeps track if it has been cloned already.
        queryToExecute = session.prepareDatabaseQuery(queryToExecute);

        if (queryToExecute.shouldPrepare()) {
            queryToExecute.checkPrepare(session, translationRow);
        }

        // Then cloned for concurrency and repeatable execution.
        if (!queryToExecute.isExecutionClone()) {
            queryToExecute = (DatabaseQuery)queryToExecute.clone();
        }
        queryToExecute.setTranslationRow(translationRow);
        // If the prepare has been disbale the clone is prepare dynamically to not parameterize the SQL.
        if (!queryToExecute.shouldPrepare()) {
            queryToExecute.checkPrepare(session, translationRow);
        }
        queryToExecute.setSession(session);
        if (hasCustomQuery) {
            prepareCustomQuery(queryToExecute);
        }
        queryToExecute.prepareForExecution();

        // Then executed.
        Object result = queryToExecute.executeDatabaseQuery();

        // Give the subclasses the oportunity to retreive aspects of the cloned query.
        clonedQueryExecutionComplete(queryToExecute, session);
        return result;
    }

    /**
     * INTERNAL:
     * Extract the correct query result from the transporter.
     */
    public Object extractRemoteResult(Transporter transporter) {
        return transporter.getObject();
    }

    /**
     * INTERNAL:
     * Return the accessor.
     */
    public Accessor getAccessor() {
        return accessor;
    }

    /**
     * INTERNAL:
     * Return the arguments for use with the pre-defined query option
     */
    public Vector getArguments() {
        if (arguments == null) {
            arguments = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        }
        return arguments;
    }

    /**
     * INTERNAL:
     * Return the argumentTypes for use with the pre-defined query option
     */
    public Vector getArgumentTypes() {
        if ((argumentTypes == null) || argumentTypes.isEmpty()) {
            argumentTypes = new Vector();
            // Bug 3256198 - lazily initialize the argument types from their class names
            if (argumentTypeNames != null) {
                Iterator args = argumentTypeNames.iterator();
                while (args.hasNext()) {
                    String argumentTypeName = (String)args.next();
                    argumentTypes.addElement(Helper.getObjectClass(ConversionManager.loadClass(argumentTypeName)));
                }
            }
        }
        return argumentTypes;
    }

    /**
     * INTERNAL:
     * Return the argumentTypeNames for use with the pre-defined query option
     * These are used pre-initialization to construct the argumentTypes list.
     */
    public Vector getArgumentTypeNames() {
        if (argumentTypeNames == null) {
            argumentTypeNames = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        }
        return argumentTypeNames;
    }

    /**
     * INTERNAL:
     * Set the argumentTypes for use with the pre-defined query option
     */
    public void setArgumentTypes(Vector argumentTypes) {
        this.argumentTypes = argumentTypes;
        // bug 3256198 - ensure the list of type names matches the argument types.
        getArgumentTypeNames().clear();
        Iterator types = argumentTypes.iterator();
        while (types.hasNext()) {
            argumentTypeNames.addElement(((Class)types.next()).getName());
        }
    }

    /**
     * INTERNAL:
     * Set the argumentTypes for use with the pre-defined query option
     */
    public void setArgumentTypeNames(Vector argumentTypeNames) {
        this.argumentTypeNames = argumentTypeNames;
    }

    /**
     * INTERNAL:
     * Set the arguments for use with the pre-defined query option.
     * Maintain the argumentTypes as well.
     */
    public void setArguments(Vector arguments) {
        for (Enumeration enumtr = arguments.elements(); enumtr.hasMoreElements();) {
            // Maintain the argumentTypes as well
            addArgument((String)enumtr.nextElement());
        }
    }

    /**
     * INTERNAL:
     * Return the argumentValues for use with argumented queries.
     */
    public Vector getArgumentValues() {
        if (argumentValues == null) {
            argumentValues = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        }
        return argumentValues;
    }

    /**
     * INTERNAL:
     * Set the argumentValues for use with argumented queries.
     */
    public void setArgumentValues(Vector theArgumentValues) {
        argumentValues = theArgumentValues;
    }

    /**
     * OBSOLETE:
     * Return the call for this query.
     * This call contains the SQL and argument list.
     * @see #getDatasourceCall()
     */
    public DatabaseCall getCall() {
        Call call = getDatasourceCall();
        if (call instanceof DatabaseCall) {
            return (DatabaseCall)call;
        } else {
            return null;
        }
    }

    /**
     * OBSOLETE:
     * Return the calls for this query.  Used for queries with multiple calls.
     * This call contains the SQL and argument list.
     * @deprecated
     * @see #getDatasourceCalls();
     */
    public List getCalls() {
        return getDatasourceCalls();
    }

    /**
     * ADVANCED:
     * Return the call for this query.
     * This call contains the SQL and argument list.
     * @see #prepareCall(Session, Record);
     */
    public Call getDatasourceCall() {
        Call call = null;
        if (getQueryMechanism() instanceof DatasourceCallQueryMechanism) {
            DatasourceCallQueryMechanism mechanism = (DatasourceCallQueryMechanism)getQueryMechanism();
            call = mechanism.getCall();
            // If has multiple calls return the first one.
            if (mechanism.hasMultipleCalls()) {
                call = (Call)mechanism.getCalls().get(0);
            }
        }
        if ((call == null) && getQueryMechanism().isEJBQLCallQueryMechanism()) {
            call = ((EJBQLCallQueryMechanism)getQueryMechanism()).getEJBQLCall();
        }
        return call;
    }

    /**
     * ADVANCED:
     * Return the calls for this query.  This method can be called for queries with multiple calls
     * This call contains the SQL and argument list.
     * @see #prepareCall(Session, Record);
     */
    public List getDatasourceCalls() {
        List calls = new Vector();
        if (getQueryMechanism() instanceof DatasourceCallQueryMechanism) {
            DatasourceCallQueryMechanism mechanism = (DatasourceCallQueryMechanism)getQueryMechanism();

            // If has multiple calls return the first one.
            if (mechanism.hasMultipleCalls()) {
                calls = mechanism.getCalls();
            } else {
                calls.add(mechanism.getCall());
            }
        }
        if ((calls.isEmpty()) && getQueryMechanism().isEJBQLCallQueryMechanism()) {
            calls.add(((EJBQLCallQueryMechanism)getQueryMechanism()).getEJBQLCall());
        }
        return calls;
    }

    /**
     * INTERNAL:
     * Return the cascade policy.
     */
    public int getCascadePolicy() {
        return cascadePolicy;
    }

    /**
     * INTERNAL:
     * Return the descriptor assigned with the reference class
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     *  PUBLIC:
     *  Return the name of the query
     */
    public String getName() {
        return name;
    }

    /**
     * INTERNAL:
     * Property support for use by mappings.
     */
    public Hashtable getProperties() {
        if (properties == null) {//Lazy initialize to conserve space and allocation time.
            properties = new Hashtable(5);
        }
        return properties;
    }

    /**
     * INTERNAL:
     * Property support used by mappings to stach temporary stuff in the query.
     */
    public synchronized Object getProperty(Object property) {
        if (properties == null) {
            return null;
        }
        return getProperties().get(property);
    }

    /**
     * INTERNAL:
     * Return the mechanism assigned to the query
     */
    public DatabaseQueryMechanism getQueryMechanism() {
        // Bug 3524620 - lazy init
        if (queryMechanism == null) {
            queryMechanism = new ExpressionQueryMechanism(this);
        }
        return queryMechanism;
    }

    /**
     * INTERNAL:
     * Check if the mechanism has been set yet, used for lazy init.
     */
    public boolean hasQueryMechanism() {
        return (queryMechanism != null);
    }

    /**
     * PUBLIC:
     * Return the number of seconds the driver will wait for a Statement to execute to the given number of seconds.
     *
     * @see DescriptorQueryManager#getQueryTimeout()
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * PUBLIC:
     * Return the query redirector.
     * A redirector can be used in a query to replace its execution with the execution of code.
     * This can be used for named or parameterized queries to allow dynamic configuration of the query base on the query arguments.
     * @see QueryRedirector
     */
    public QueryRedirector getRedirector() {
        return redirector;
    }

    /**
     * PUBLIC:
     * Return the domain class associated with this query.
     * By default this is null, but should be overridden in subclasses.
     */
    public Class getReferenceClass() {
        return null;
    }

    /**
     * INTERNAL:
     * return the name of the reference class.  Added for Mapping Workbench removal
     * of classpath dependancy.  Overriden by subclasses.
     */
    public String getReferenceClassName() {
        return null;
    }

    /**
     * PUBLIC:
     * Return the selection criteria of the query.
     * This should only be used with expression queries, null will be returned for others.
     */
    public Expression getSelectionCriteria() {
        return getQueryMechanism().getSelectionCriteria();
    }

    /**
     * INTERNAL:
     * Return the current session.
     */
    public AbstractSession getSession() {
        return session;
    }
    
    /**
     * INTERNAL:
     * Return the execution session.
     * This is the session used to build objects returned by the query.
     */
    public AbstractSession getExecutionSession() {
        if (this.executionSession == null) {
            if (getSession() != null) {
                this.executionSession = getSession().getExecutionSession(this);
            }            
        }
        return this.executionSession;
    }
    
    /**
     * INTERNAL:
     * Set the execution session.
     * This is the session used to build objects returned by the query.
     */
    protected void setExecutionSession(AbstractSession executionSession) {
        this.executionSession = executionSession;
    }

    /**
     * PUBLIC:
     * Return the name of the session that the query should be executed under.
     * This can be with the session broker to override the default session.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * PUBLIC:
     * Return the SQL statement of the query.
     * This can only be used with statement queries.
     */
    public SQLStatement getSQLStatement() {
        return ((StatementQueryMechanism)getQueryMechanism()).getSQLStatement();
    }

    /**
     * PUBLIC:
     * Return the JPQL string of the query.
     */
    public String getJPQLString() {
        return getJPQLString();
    }
    
    /**
     * PUBLIC:
     * Return the EJBQL string of the query.
     */
    public String getEJBQLString() {
        if (!(getQueryMechanism().isEJBQLCallQueryMechanism())) {
            return null;
        }
        EJBQLCall call = ((EJBQLCallQueryMechanism)getQueryMechanism()).getEJBQLCall();
        return call.getEjbqlString();
    }

    /**
     * PUBLIC:
     * Return the current database hint string of the query.
     */
    public String getHintString() {
        return hintString;
    }

    /**
     * ADVANCED:
     * Return the SQL string of the query.
     * This can be used for SQL queries.
     * This can also be used for normal queries if they have been prepared, (i.e. query.prepareCall()).
     * @see #prepareCall(Session, Record)
     */
    public String getSQLString() {
        Call call = getDatasourceCall();
        if (call == null) {
            return null;
        }
        if (!(call instanceof SQLCall)) {
            return null;
        }

        return ((SQLCall)call).getSQLString();
    }

    /**
     * ADVANCED:
     * Return the SQL strings of the query.  Used for queries with multiple calls
     * This can be used for SQL queries.
     * This can also be used for normal queries if they have been prepared, (i.e. query.prepareCall()).
     * @see #prepareCall(Session, Record)
     */
    public List getSQLStrings() {
        List calls = getDatasourceCalls();
        if ((calls == null) || calls.isEmpty()) {
            return null;
        }
        Vector returnSQL = new Vector(calls.size());
        Iterator iterator = calls.iterator();
        while (iterator.hasNext()) {
            Call call = (Call)iterator.next();
            if (!(call instanceof SQLCall)) {
                return null;
            }
            returnSQL.addElement(((SQLCall)call).getSQLString());
        }
        return returnSQL;
    }

    /**
     * INTERNAL:
     * Returns the internal tri-state calue of shouldBindParameters
     * used far cascading these settings
     */
    public int getShouldBindAllParameters() {
        return this.shouldBindAllParameters;
    }

    /**
     * ADVANCED:
     * This can be used to access a queries translated SQL if they have been prepared, (i.e. query.prepareCall()).
     * The Record argument is one of (Record, XMLRecord) that contains the query arguments.
     * @see #prepareCall(oracle.toplink.sessions.Session, Record)
     */
    public String getTranslatedSQLString(oracle.toplink.sessions.Session session, Record translationRow) {
        prepareCall((AbstractSession)session, (AbstractRecord)translationRow);        
        //CR#2859559 fix to use Session and Record interfaces not impl classes.
        CallQueryMechanism queryMechanism = (CallQueryMechanism)getQueryMechanism();
        if (queryMechanism.getCall() == null) {
            return null;
        }
        SQLCall call = (SQLCall)queryMechanism.getCall().clone();
        call.setUsesBinding(false);
        call.translate((AbstractRecord)translationRow, queryMechanism.getModifyRow(), (AbstractSession)session);
        return call.getSQLString();
    }

    /**
     * ADVANCED:
     * This can be used to access a queries translated SQL if they have been prepared, (i.e. query.prepareCall()).
     * This method can be used for queries with multiple calls.
     * @see #prepareCall(Session, Record)
     */
    public List getTranslatedSQLStrings(oracle.toplink.sessions.Session session, Record translationRow) {
        prepareCall((AbstractSession)session, (AbstractRecord)translationRow);        
        CallQueryMechanism queryMechanism = (CallQueryMechanism)getQueryMechanism();
        if ((queryMechanism.getCalls() == null) || queryMechanism.getCalls().isEmpty()) {
            return null;
        }
        Vector calls = new Vector(queryMechanism.getCalls().size());
        Iterator iterator = queryMechanism.getCalls().iterator();
        while (iterator.hasNext()) {
            SQLCall call = (SQLCall)iterator.next();
            call = (SQLCall)call.clone();
            call.setUsesBinding(false);
            call.translate((AbstractRecord)translationRow, queryMechanism.getModifyRow(), (AbstractSession)session);
            calls.add(call.getSQLString());
        }
        return calls;
    }

    /**
     * INTERNAL:
     * Return the row for translation
     */
    public AbstractRecord getTranslationRow() {
        return translationRow;
    }

    /**
     * INTERNAL:
     * returns true if the accessor has already been set. The getAccessor() will attempt to
     * lazily initialzie it.
     */
    public boolean hasAccessor() {
        return accessor != null;
    }

    /**
     * INTERNAL:
     * Return if any properties exist in the query.
     */
    public boolean hasProperties() {
        return (properties != null) && (!properties.isEmpty());
    }

    /**
     * INTERNAL:
     * Return if any arguments exist in the query.
     */
    public boolean hasArguments() {
        return (arguments != null) && (!arguments.isEmpty());
    }

    /**
     * PUBLIC:
     * Return if a name of the session that the query should be executed under has been specified.
     * This can be with the session broker to override the default session.
     */
    public boolean hasSessionName() {
        return sessionName != null;
    }

    /**
     * PUBLIC:
     * Session's shouldBindAllParameters() defines whether to bind or not
     * (default setting)
     */
    public void ignoreBindAllParameters() {
        this.shouldBindAllParameters = Undefined;
    }

    /**
     * PUBLIC:
     * Session's shouldCacheAllStatements() defines whether to cache or not
     * (default setting)
     */
    public void ignoreCacheStatement() {
        this.shouldCacheStatement = Undefined;
    }

    /**
     * PUBLIC:
     * Return true if this query uses an SQL or stored procedure, or SDK call.
     */
    public boolean isCallQuery() {
        return getQueryMechanism().isCallQueryMechanism();
    }

    /**
     * INTERNAL:
     * Returns true if this query has been created as the result of cascading a delete of an aggregate collection
     * in a UnitOfWork
     * CR 2811
     */
    public boolean isCascadeOfAggregateDelete() {
        return getCascadePolicy() == CascadeAggregateDelete;
    }

    /**
     * PUBLIC:
     * Return if this is a data modify query.
     */
    public boolean isDataModifyQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this is a data read query.
     */
    public boolean isDataReadQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this is a value read query.
     */
    public boolean isValueReadQuery() {
        return false;
    }
    
    /**
     * PUBLIC:
     * Return if this is a direct read query.
     */
    public boolean isDirectReadQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this is a delete all query.
     */
    public boolean isDeleteAllQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this is a delete object query.
     */
    public boolean isDeleteObjectQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this query uses an expression query mechanism
     */
    public boolean isExpressionQuery() {
        return getQueryMechanism().isExpressionQueryMechanism();
    }

    /**
     * PUBLIC:
     * Return true if this is a modify all query.
     */
    public boolean isModifyAllQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is a modify query.
     */
    public boolean isModifyQuery() {
        return false;
    }

    /**
       * PUBLIC:
       * Return true if this is an update all query.
       */
    public boolean isUpdateAllQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is an update object query.
     */
    public boolean isUpdateObjectQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * If executed against a RepeatableWriteUnitOfWork if this attribute is true
     * TopLink will write changes to the database before executing the query.
     */
    public Boolean getFlushOnExecute(){
        return this.flushOnExecute;
    }
    
    /**
     * PUBLIC:
     * Return true if this is an insert object query.
     */
    public boolean isInsertObjectQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is an object level modify query.
     */
    public boolean isObjectLevelModifyQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is an object level read query.
     */
    public boolean isObjectLevelReadQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return if this is an object building query.
     */
    public boolean isObjectBuildingQuery() {
        return false;
    }

    /**
     * INTERNAL:
     * Queries are prepared when they are executed and then do not need to be
     * prepared on subsequent executions. This method returns true if this
     * query has been prepared.  Updating the settings on a query will 'un-prepare'
     * the query.
     */
    public boolean isPrepared() {
        return isPrepared;
    }

    /**
     * PUBLIC:
     * Return true if this is a read all query.
     */
    public boolean isReadAllQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return ture if this is a read object query.
     */
    public boolean isReadObjectQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is a read query.
     */
    public boolean isReadQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this is a report query.
     */
    public boolean isReportQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Return true if this query uses an SQL query mechanism .
     */
    public boolean isSQLCallQuery() {
        // BUG#2669342 CallQueryMechanism and isCallQueryMechanism have different meaning as SDK return true but isn't.
        Call call = getDatasourceCall();
        return (call != null) && (call instanceof SQLCall);
    }

    /**
     * INTERNAL:
     * Return true if the query is a custom user defined query.
     */
    public boolean isUserDefined() {
        return isUserDefined;
    }
    
    /**
     * INTERNAL:
     * Return true if the query uses default properties.
     * This is used to determine if this query is cacheable.
     * i.e. does not use any properties that may conflict with another query
     * with the same EJBQL or selection criteria.
     */
    public boolean isDefaultPropertiesQuery() {
        return (!isUserDefined())
            && (shouldPrepare())
            && (getQueryTimeout() == DescriptorQueryManager.DefaultTimeout)
            && (getHintString() == null)
            && (shouldIgnoreBindAllParameters())
            && (shouldIgnoreCacheStatement())
            && (shouldUseWrapperPolicy());
    }

    /**
     * PUBLIC:
     * Return true if this is a write object query.
     */
    public boolean isWriteObjectQuery() {
        return false;
    }

    /**
     * PUBLIC:
     * Set for the identity map (cache) to be maintained.
     * This is the default.
     */
    public void maintainCache() {
        setShouldMaintainCache(true);
    }

    /**
     * INTERNAL:
     * This is different from 'prepareForExecution' in that this is called on the original query,
     * and the other is called on the copy of the query.
     * This query is copied for concurrency so this prepare can only setup things that
     * will apply to any future execution of this query.
     *
     * Resolve the queryTimeout using the DescriptorQueryManager if required.
     */
    protected void prepare() throws QueryException {
        //If the queryTimeout is DefaultTimeout, resolve using the DescriptorQueryManager
        if (this.getQueryTimeout() == DescriptorQueryManager.DefaultTimeout) {
            if (this.getDescriptor() == null) {
                this.setQueryTimeout(DescriptorQueryManager.NoTimeout);
            } else {
                this.setQueryTimeout(getDescriptor().getQueryManager().getQueryTimeout());
            }
        }

        getQueryMechanism().prepare();
    }
        
    /**
     * INTERNAL:
     * Copy all setting from the query.
     * This is used to morph queries from one type to the other.
     * By default this calls prepareFromQuery, but additional properties may be required
     * to be copied as prepareFromQuery only copies properties that affect the SQL.
     */
    public void copyFromQuery(DatabaseQuery query) {
        prepareFromQuery(query);
        this.cascadePolicy = query.cascadePolicy;
        this.flushOnExecute = query.flushOnExecute;
        this.arguments = query.arguments;
        this.argumentTypes = query.argumentTypes;
        this.argumentTypeNames = query.argumentTypeNames;
        this.argumentValues = query.argumentValues;
        this.queryTimeout = query.queryTimeout;
        this.redirector = query.redirector;
        this.sessionName = query.sessionName;
        this.shouldBindAllParameters = query.shouldBindAllParameters;
        this.shouldCacheStatement = query.shouldCacheStatement;
        this.shouldMaintainCache = query.shouldMaintainCache;
        this.shouldPrepare = query.shouldPrepare;
        this.shouldUseWrapperPolicy = query.shouldUseWrapperPolicy;
        this.properties = query.properties;
    }
    
    /**
     * INTERNAL:
     * Prepare the query from the prepared query.
     * This allows a dynamic query to prepare itself directly from a prepared query instance.
     * This is used in the JPQL parse cache to allow preparsed queries to be used to prepare
     * dyanmic queries.
     * This only copies over properties that are configured through JPQL.
     */
    public void prepareFromQuery(DatabaseQuery query) {
        setQueryMechanism((DatabaseQueryMechanism)query.getQueryMechanism().clone());
        getQueryMechanism().setQuery(this);
        this.descriptor = query.descriptor;
        this.hintString = query.hintString;
        this.isCustomQueryUsed = query.isCustomQueryUsed;
        //this.properties = query.properties; - Cannot set properties and CMP stores finders there.
    }

    /**
     * ADVANCED:
     * Pre-generate the call/SQL for the query.
     * This method takes a Session and an implementor of Record (DatebaseRow or XMLRecord).
     * This can be used to access the SQL for a query without executing it.
     * To access the call use, query.getCall(), or query.getSQLString() for the SQL.
     * Note the SQL will have argument markers in it (i.e. "?").
     * To translate these use query.getTranslatedSQLString(session, translationRow).
     * @see #getCall()
     * @see #getSQLString()
     * @see #getTranslatedSQLString(oracle.toplink.sessions.Session, Record)
     */
    public void prepareCall(oracle.toplink.sessions.Session session, Record translationRow) throws QueryException {
        //CR#2859559 fix to use Session and Record interfaces not impl classes.
        checkPrepare((AbstractSession)session, (AbstractRecord)translationRow, true);
    }

    /**
     * INTERNAL:
     * Set the properties needed to be cascaded into the custom query.
     */
    protected void prepareCustomQuery(DatabaseQuery customQuery) {
        // Nothing by default.
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session. In particular,
     * set the descriptor of the receiver to the ClassDescriptor for the
     * appropriate class for the receiver's object.
     */
    public void prepareForExecution() throws QueryException {}

    protected void prepareForRemoteExecution() {}

    /**
     * INTERNAL:
     * Use a TopLink redirector to redirect this query to a method.
     * Added for bug 3241138
     *
     */
    public Object redirectQuery(DatabaseQuery queryToRedirect, AbstractSession session, AbstractRecord translationRow) {
        if (queryToRedirect.getRedirector() == null) {
            return null;
        }
        DatabaseQuery queryToExecute = (DatabaseQuery)queryToRedirect.clone();
        queryToExecute.setRedirector(null);

        // since we deal with a clone when using a redirector, the descriptor will 
        // get set on the clone, but not on the original object. So before returning 
        // the results, set the descriptor from the clone onto this object (original
        // query) - GJPP, BUG# 2692956
        Object toReturn = queryToRedirect.getRedirector().invokeQuery(queryToExecute, translationRow, session);
        setDescriptor(queryToExecute.getDescriptor());
        return toReturn;
    }

    protected Object remoteExecute() {
        Transporter transporter = ((RemoteSession)getSession()).getRemoteConnection().remoteExecute((DatabaseQuery)this.clone());
        return extractRemoteResult(transporter);
    }

    /**
     * INTERNAL:
     */
    public Object remoteExecute(AbstractSession session) {
        setSession(session);
        prepareForRemoteExecution();
        return remoteExecute();
    }

    /**
     * INTERNAL:
     * Property support used by mappings.
     */
    public void removeProperty(Object property) {
        getProperties().remove(property);
    }

    /**
     * INTERNAL:
     * replace the value holders in the specified result object(s)
     */
    public IdentityHashtable replaceValueHoldersIn(Object object, RemoteSessionController controller) {
        // by default, do nothing
        return null;
    }

    /**
     * INTERNAL:
     * Translate argumentValues into a database row.
     */
    public AbstractRecord rowFromArguments(Vector argumentValues) throws QueryException {
        Vector argumentNames = getArguments();

        if (argumentNames.size() != argumentValues.size()) {
            throw QueryException.argumentSizeMismatchInQueryAndQueryDefinition(this);
        }
        Vector argumentTypes = getArgumentTypes();
        int argumentsSize = argumentNames.size();

        AbstractRecord row = new DatabaseRecord(argumentsSize);
        for (int index = 0; index < argumentsSize; index++) {
            String argumentName = (String)argumentNames.get(index);
            Object argumentValue = argumentValues.get(index);
            DatabaseField argumentField = new DatabaseField(argumentName);
            argumentField.setType((Class)argumentTypes.get(index));
            row.put(argumentField, argumentValue);
        }

        return row;
    }

    /**
     * INTERNAL:
     * Set the accessor, the query must always use the same accessor for database access.
     * This is required to support connection pooling.
     */
    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    /**
     * PUBLIC:
     * Used to define a store procedure or SQL query.
     */
    public void setDatasourceCall(Call call) {
        if (call == null) {
            return;
        }
        setQueryMechanism(call.buildNewQueryMechanism(this));
    }

    /**
     * PUBLIC:
     * Used to define a store procedure or SQL query.
     */
    public void setCall(Call call) {
        setDatasourceCall(call);
    }

    /**
     * INTERNAL:
     * Set the cascade policy.
     */
    public void setCascadePolicy(int policyConstant) {
        cascadePolicy = policyConstant;
    }

    /**
     * INTERNAL:
     * Set the descriptor for the query.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        // If the descriptor changed must unprepare as the SQL may change.
        if (this.descriptor != descriptor) {
            setIsPrepared(false);
        }
        this.descriptor = descriptor;
    }

    /**
     * PUBLIC:
     * Set the JPQL string of the query.
     * If arguments are required in the string they will be preceeded by ":" then the argument name.
     * The JPQL arguments must also be added as argument to the query.
     */
    public void setJPQLString(String jpqlString) {
        setEJBQLString(jpqlString);
    }

    /**
     * PUBLIC:
     * Set the EJBQL string of the query.
     * If arguments are required in the string they will be preceeded by "?" then the argument number.
     */
    public void setEJBQLString(String ejbqlString) {
        //Added the check for when we are building the query from the deployment XML
        if ((ejbqlString != null) && (!ejbqlString.equals(""))) {
            EJBQLCallQueryMechanism mechanism = new EJBQLCallQueryMechanism(this, new EJBQLCall(ejbqlString));
            setQueryMechanism(mechanism);
        }
    }

    /**
     * PUBLIC:
     * If executed against a RepeatableWriteUnitOfWork if this attribute is true
     * TopLink will write changes to the database before executing the query.
     */
    public void setFlushOnExecute(Boolean flushMode){
        this.flushOnExecute = flushMode;
    }
    
    /**
     * PUBLIC:
     * Used to set the database hint string on the query.
     * This should be the full hint string including the comment delimiters.
     * This method will cause a query to re-prepare if it has already been executed.
     * @param newHintString the hint string to be added into the SQL call.
     */
    public void setHintString(String newHintString) {
        hintString = newHintString;
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * If changes are made to the query that affect the derived SQL or Call
     * parameters the query needs to be prepared again.
     * <p>
     * Automatically called internally.
     */
    public void setIsPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
        this.isCustomQueryUsed = null;
    }
    
    /**
     * INTERNAL:
     * PERF: Return if the query is an execution clone.
     * This allow the clone during execution to be avoided in the cases when the query has
     * already been clone elsewhere.
     */
    public boolean isExecutionClone() {
        return isExecutionClone;
    }
    
    /**
     * INTERNAL:
     * PERF: Set if the query is an execution clone.
     * This allow the clone during execution to be avoided in the cases when the query has
     * already been clone elsewhere.
     */
    public void setIsExecutionClone(boolean isExecutionClone) {
        this.isExecutionClone = isExecutionClone;
    }
    
    /**
     * INTERNAL:
     * PERF: Return if this query will use the descriptor custom query
     * instead of executing itself.
     */
    public Boolean isCustomQueryUsed() {
        return this.isCustomQueryUsed;
    }
    
    /**
     * INTERNAL:
     * If the query mechanisn is a call query mechanism and there are no
     * arguments on the query then it must be a foreign reference custom 
     * selection query.
     */
    protected boolean isCustomSelectionQuery() {
        return getQueryMechanism().isCallQueryMechanism() && getArguments().isEmpty();
    }
    
    /**
     * INTERNAL:
     * PERF: Set if this query will use the descriptor custom query
     * instead of executing itself.
     */
    protected void setIsCustomQueryUsed(boolean isCustomQueryUsed) {
        if (isCustomQueryUsed) {
            this.isCustomQueryUsed = Boolean.TRUE;
        } else {
            this.isCustomQueryUsed = Boolean.FALSE;            
        }
    }

    /**
     * INTERNAL:
     * Set if the query is a custom user defined query.
     */
    public void setIsUserDefined(boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
    }

    /**
     * PUBLIC:
     * Set the query's name.
     * Queries can be named and added to a descriptor or the session and then referenced by name.
     */
    public void setName(String queryName) {
        name = queryName;
    }

    /**
     * INTERNAL:
     * Property support used by mappings.
     */
    public void setProperties(Hashtable properties) {
        this.properties = properties;
    }

    /**
     * INTERNAL:
     * Property support used by mappings to stache temporary stuff.
     */
    public synchronized void setProperty(Object property, Object value) {
        getProperties().put(property, value);
    }

    /**
     * Set the query mechanism for the query.
     */
    protected void setQueryMechanism(DatabaseQueryMechanism queryMechanism) {
        this.queryMechanism = queryMechanism;
        // Must un-prepare is prepare as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Set the number of seconds the driver will wait for a Statement to execute to the given number of seconds.
     * If the limit is exceeded, a DatabaseException is thrown.
     *
     * queryTimeout - the new query timeout limit in seconds; DefaultTimeout is the default, which
     * redirects to DescriptorQueryManager's queryTimeout.
     *
     * @see DescriptorQueryManager#setQueryTimeout(int)
     *
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
        this.shouldCloneCall=true;
    }

    /**
     * PUBLIC:
     * Set the query redirector.
     * A redirector can be used in a query to replace its execution with the execution of code.
     * This can be used for named or parameterized queries to allow dynamic configuration of the query base on the query arguments.
     * @see QueryRedirector
     */
    public void setRedirector(QueryRedirector redirector) {
        this.redirector = redirector;
    }

    /**
     * PUBLIC:
     * To any user of this object. Set the selection criteria of the query.
     * This method be used when dealing with expressions.
     */
    public void setSelectionCriteria(Expression expression) {
        // Do not overwrite the call if the expression is null.
        if ((expression == null) && (!getQueryMechanism().isExpressionQueryMechanism())) {
            return;
        }
        if (!getQueryMechanism().isExpressionQueryMechanism()) {
            setQueryMechanism(new ExpressionQueryMechanism(this, expression));
        } else {
            ((ExpressionQueryMechanism)getQueryMechanism()).setSelectionCriteria(expression);
        }

        // Must un-prepare is prepare as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Set the session for the query
     */
    public void setSession(AbstractSession session) {
        this.session = session;
        this.executionSession = null;
    }

    /**
     * PUBLIC:
     * Set the name of the session that the query should be executed under.
     * This can be with the session broker to override the default session.
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * PUBLIC:
     * Bind all arguments to any SQL statement.
     */
    public void setShouldBindAllParameters(boolean shouldBindAllParameters) {
        if (shouldBindAllParameters) {
            this.shouldBindAllParameters = True;
        } else {
            this.shouldBindAllParameters = False;
        }
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Sets the internal tri-state value of shouldBindAllParams
     * Used to cascade this value to alther queries
     */
    public void setShouldBindAllParameters(int bindAllParams) {
        this.shouldBindAllParameters = bindAllParams;
    }

    /**
     * PUBLIC:
     * Cache the prepared statements, this requires full parameter binding as well.
     */
    public void setShouldCacheStatement(boolean shouldCacheStatement) {
        if (shouldCacheStatement) {
            this.shouldCacheStatement = True;
        } else {
            this.shouldCacheStatement = False;
        }
        setIsPrepared(false);
    }
    
    /**
     * PUBLIC:
     * Set if the identity map (cache) should be used or not.
     * If not the cache check will be skipped and the result will not be put into the identity map.
     * By default the identity map is always maintained.
     */
    public void setShouldMaintainCache(boolean shouldMaintainCache) {
        this.shouldMaintainCache = shouldMaintainCache;
    }

    /**
     * PUBLIC:
     * Set if the query should be prepared.
     * TopLink automatically prepares queries to generate their SQL only once,
     * one each execution of the query the SQL does not need to be generated again only the arguments need to be translated.
     * This option is provide to disable this optimization as in can cause problems with certain types of queries that require dynamic SQL basd on their arguments.
     * <p>These queries include:
     * <ul>
     * <li> Expressions that make use of 'equal' where the argument value has the potential to be null, this can cause problems on databases that require IS NULL, instead of = NULL.
     * <li> Expressions that make use of 'in' and that use parameter binding, this will cause problems as the in values must be bound individually.
     * </ul>
     */
    public void setShouldPrepare(boolean shouldPrepare) {
        this.shouldPrepare = shouldPrepare;
        setIsPrepared(false);
    }

    /**
     * ADVANCED:
     * The wrapper policy can be enable on a query.
     */
    public void setShouldUseWrapperPolicy(boolean shouldUseWrapperPolicy) {
        this.shouldUseWrapperPolicy = shouldUseWrapperPolicy;
    }

    /**
     * PUBLIC:
     * To any user of this object. Set the SQL statement of the query.
     * This method should only be used when dealing with statement objects.
     */
    public void setSQLStatement(SQLStatement sqlStatement) {
        setQueryMechanism(new StatementQueryMechanism(this, sqlStatement));
    }

    /**
     * PUBLIC:
     * To any user of this object. Set the SQL string of the query.
     * This method should only be used when dealing with user defined SQL strings.
     * If arguments are required in the string they will be preceeded by "#" then the argument name.
     * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public void setSQLString(String sqlString) {
        //Added the check for when we are building the query from the deployment XML
        if ((sqlString != null) && (!sqlString.equals(""))) {
            setCall(new SQLCall(sqlString));
        }
    }

    /**
     * INTERNAL:
     * Set the row for translation
     */
    public void setTranslationRow(AbstractRecord translationRow) {
        this.translationRow = translationRow;
    }

    /**
     * PUBLIC:
     * Bind all arguments to any SQL statement.
     */
    public boolean shouldBindAllParameters() {
        return shouldBindAllParameters == True;
    }

    /**
     * PUBLIC:
     * Cache the prepared statements, this requires full parameter binding as well.
     */
    public boolean shouldCacheStatement() {
        return shouldCacheStatement == True;
    }

    /**
     * PUBLIC:
     * Flag used to determine if all parts should be cascaded
     */
    public boolean shouldCascadeAllParts() {
        return this.cascadePolicy == CascadeAllParts;
    }

    /**
     * PUBLIC:
     * Mappings should be checked to determined if the current operation should be
     * cascaded to the objects referenced.
     */
    public boolean shouldCascadeByMapping() {
        return this.cascadePolicy == CascadeByMapping;
    }

    /**
     * INTERNAL:
     * Flag used for unit of works cascade policy.
     */
    public boolean shouldCascadeOnlyDependentParts() {
        return this.cascadePolicy == CascadeDependentParts;
    }

    /**
     * PUBLIC:
     * Flag used to determine if any parts should be cascaded
     */
    public boolean shouldCascadeParts() {
        return this.cascadePolicy != NoCascading;
    }

    /**
     * PUBLIC:
     * Flag used to determine if any private parts should be cascaded
     */
    public boolean shouldCascadePrivateParts() {
        return (this.cascadePolicy == CascadePrivateParts) || (this.cascadePolicy == CascadeAllParts);
    }
    
    /**
     * INTERNAL:
     * Flag used to determine if the call needs to be cloned.
     */
    public boolean shouldCloneCall(){
        return shouldCloneCall;
    }

    /**
     * PUBLIC:
     * Local shouldBindAllParameters() should be ignored,
     * Session's shouldBindAllParameters() should be used.
     */
    public boolean shouldIgnoreBindAllParameters() {
        return shouldBindAllParameters == Undefined;
    }

    /**
     * PUBLIC:
     * Local shouldCacheStatement() should be ignored,
     * Session's shouldCacheAllStatements() should be used.
     */
    public boolean shouldIgnoreCacheStatement() {
        return shouldCacheStatement == Undefined;
    }

    /**
     * PUBLIC:
     * Return if the identity map (cache) should be used or not.
     * If not the cache check will be skipped and the result will not be put into the identity map.
     * By default the identity map is always maintained.
     */
    public boolean shouldMaintainCache() {
        return shouldMaintainCache;
    }

    /**
     * PUBLIC:
     * Return if the query should be prepared.
     * TopLink automatically prepares queries to generate their SQL only once,
     * one each execution of the query the SQL does not need to be generated again only the arguments need to be translated.
     * This option is provide to disable this optimization as in can cause problems with certain types of queries that require dynamic SQL basd on their arguments.
     * <p>These queries include:
     * <ul>
     * <li> Expressions that make use of 'equal' where the argument value has the potential to be null, this can cause problems on databases that require IS NULL, instead of = NULL.
     * <li> Expressions that make use of 'in' and that use parameter binding, this will cause problems as the in values must be bound individually.
     * </ul>
     */
    public boolean shouldPrepare() {
        return shouldPrepare;
    }

    /**
     * ADVANCED:
     * The wrapper policy can be enabled on a query.
     */
    public boolean shouldUseWrapperPolicy() {
        return shouldUseWrapperPolicy;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "()";
    }

    /**
     * INTERNAL:
     * TopLink_sessionName_domainClass.  Cached in properties
     */
     public String getDomainClassNounName(String sessionName) {    
        if (getProperty("DMSDomainClassNounName") == null) {
            StringBuffer buffer = new StringBuffer("TopLink");
            if (sessionName != null) {
                buffer.append(sessionName);
            }
            if (getReferenceClassName() != null) {
                buffer.append("_");
                buffer.append(getReferenceClassName());
            }        
            setProperty("DMSDomainClassNounName", buffer.toString());
        }
        return (String)getProperty("DMSDomainClassNounName");
     }
        
    /**
     * INTERNAL:
     * TopLink_sessionName_domainClass_queryClass_queryName (if exist).  Cached in properties
     */
    public String getQueryNounName(String sessionName) {    
        if (getProperty("DMSQueryNounName") == null) {
            StringBuffer buffer = new StringBuffer(getDomainClassNounName(sessionName));            
            buffer.append("_");
            buffer.append(getClass().getSimpleName());
            if (getName() != null) {
                buffer.append("_");
                buffer.append(getName());
            }
            setProperty("DMSQueryNounName", buffer.toString());
        }
        return (String)getProperty("DMSQueryNounName");
    }

    /**
     * INTERNAL:
     * TopLink_sessionName_domainClass_queryClass_queryName (if exist)_operationName (if exist).  Cached in properties
     */
    public String getSensorName(String operationName, String sessionName) {    
        if (operationName == null) {
            return getQueryNounName(sessionName);
        }
        
        Hashtable sensorNames = (Hashtable)getProperty("DMSSensorNames");            
        if (sensorNames == null) {
            sensorNames = new Hashtable();
            setProperty("DMSSensorNames", sensorNames);
        }
        Object sensorName = sensorNames.get(operationName);
        if (sensorName == null) {
            StringBuffer buffer = new StringBuffer(getQueryNounName(sessionName));            
            buffer.append("_");
            buffer.append(operationName);
            sensorName = buffer.toString();
            sensorNames.put(operationName, sensorName);
        }
        return (String)sensorName;
    }

    /**
     * ADVANCED:
     * Set if the descriptor requires usage of a native (unwrapped) JDBC connection.
     * This may be required for some Oracle JDBC support when a wrapping DataSource is used.
     */
    public void setIsNativeConnectionRequired(boolean isNativeConnectionRequired) {
        this.isNativeConnectionRequired = isNativeConnectionRequired;
    }

    /**
     * ADVANCED:
     * Return if the descriptor requires usage of a native (unwrapped) JDBC connection.
     * This may be required for some Oracle JDBC support when a wrapping DataSource is used.
     */
    public boolean isNativeConnectionRequired() {
        return isNativeConnectionRequired;
    }

}
