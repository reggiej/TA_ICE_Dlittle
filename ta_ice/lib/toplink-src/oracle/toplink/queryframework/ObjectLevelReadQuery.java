// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.*;
import oracle.toplink.expressions.*;
import oracle.toplink.history.*;
import oracle.toplink.internal.databaseaccess.DatabaseCall;
import oracle.toplink.internal.expressions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.history.*;
import oracle.toplink.internal.queryframework.*;
import oracle.toplink.mappings.*;
import oracle.toplink.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.querykeys.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.logging.SessionLog;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all read queries using objects.
 *
 * <p><b>Description</b>:
 * Contains common behavior for all read queries using objects.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class ObjectLevelReadQuery extends ObjectBuildingQuery {

    /** Provide a default builder so that it's easier to be consistent */
    protected ExpressionBuilder defaultBuilder;

    /** Allow for the cache usage to be specified to enable in-memory querying. */
    protected int cacheUsage;

    // Note: UseDescriptorSetting will result in CheckCacheByPrimaryKey for most cases
    // it simply allows the ClassDescriptor's disable cache hits to be used
    public static final int UseDescriptorSetting = -1;
    public static final int DoNotCheckCache = 0;
    public static final int CheckCacheByExactPrimaryKey = 1;
    public static final int CheckCacheByPrimaryKey = 2;
    public static final int CheckCacheThenDatabase = 3;
    public static final int CheckCacheOnly = 4;
    public static final int ConformResultsInUnitOfWork = 5;

    /** Allow for additional fields to be selected, used for m-m batch reading. */
    protected Vector additionalFields;

    /** Allow for a complex result to be return including the rows and objects, used for m-m batch reading. */
    protected boolean shouldIncludeData;

    /** Allow a prePrepare stage to build the expression for EJBQL and QBE and resolve joining. */
    protected boolean isPrePrepared;

    /** Indicates if distinct should be used or not. */
    protected short distinctState;
    public static final short UNCOMPUTED_DISTINCT = 0;
    public static final short USE_DISTINCT = 1;
    public static final short DONT_USE_DISTINCT = 2;

    /**
     * Used to determine behaviour of indirection in in-memory querying and conforming.
     */
    protected int inMemoryQueryIndirectionPolicy;

    /**
     * Used to set the read time on objects that use this query.
     * Should be set to the time the query returned from the database.
     */
    protected long executionTime = 0;

    /** Allow for a query level fetch group to be set. */
    protected FetchGroup fetchGroup;

    /** The pre-defined fetch group name. */
    protected String fetchGroupName;

    /** Flag to turn on/off the use of the default fetch group. */
    protected boolean shouldUseDefaultFetchGroup = true;
    
    /**
     * Stores the non fetchjoin attributes, these are joins that will be
     * represented in the where clause but not in the select.
     */
    protected Vector nonFetchJoinAttributeExpressions;

    /**  Stores the partial attributes that have been added to this query */
    protected Vector partialAttributeExpressions;
    
    /** Stores the helper object for dealing with joined attributes */
    protected JoinedAttributeManager joinedAttributeManager;
    
    /** PERF: Caches locking policy isReferenceClassLocked setting. */
    protected Boolean isReferenceClassLocked;
    
    /** PERF: Allow queries to build directly from the database result-set. */
    protected boolean isResultSetOptimizedQuery = false;
    
    /** PERF: Allow queries to be defined as read-only in unit of work execution. */
    protected boolean isReadOnly = false;
    
    /** Define if an outer join should be used to read subclasses. */
    protected Boolean shouldOuterJoinSubclasses;
    
    /** Allow concrete subclasses calls to be prepared and cached for inheritance queries. */
    protected Map<Class, DatabaseCall> concreteSubclassCalls;
    
    /**
     * INTERNAL:
     * Initialize the state of the query
     */
    public ObjectLevelReadQuery() {
        this.shouldRefreshIdentityMapResult = false;
        this.distinctState = UNCOMPUTED_DISTINCT;
        this.cacheUsage = UseDescriptorSetting;
        this.shouldIncludeData = false;
        this.inMemoryQueryIndirectionPolicy = InMemoryQueryIndirectionPolicy.SHOULD_THROW_INDIRECTION_EXCEPTION;
    }
    
    /**
     * INTERNAL:
     * Return if the query is equal to the other.
     * This is used to allow dynamic expression query SQL to be cached.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if ((object == null) || (!getClass().equals(object.getClass())))  {
            return false;
        }
        // Only check expression queries for now.
        if ((!isExpressionQuery()) || (!isDefaultPropertiesQuery())) {
            return super.equals(object);
        }
        ObjectLevelReadQuery query = (ObjectLevelReadQuery) object;
        if (!getExpressionBuilder().equals(query.getExpressionBuilder())) {
            return false;
        }
        if (hasJoining()) {
            if (!query.hasJoining()) {
                return false;
            }
            List joinedAttributes = getJoinedAttributeManager().getJoinedAttributeExpressions();
            List otherJoinedAttributes = query.getJoinedAttributeManager().getJoinedAttributeExpressions();
            int size = joinedAttributes.size();
            if (size != otherJoinedAttributes.size()) {
                return false;
            }
            for (int index = 0; index < size; index++) {
                if (!joinedAttributes.get(index).equals(otherJoinedAttributes.get(index))) {
                    return false;
                }
            }
        } else if (query.hasJoining()) {
            return false;
        }
        return ((getReferenceClass() == query.getReferenceClass()) || ((getReferenceClass() != null) && getReferenceClass().equals(query.getReferenceClass())))
            && ((getSelectionCriteria() == query.getSelectionCriteria()) || ((getSelectionCriteria() != null) && getSelectionCriteria().equals(query.getSelectionCriteria())));
    }
        
    /**
     * INTERNAL:
     * Compute a consistent hash-code for the expression.
     * This is used to allow dynamic expression's SQL to be cached.
     */
    public int hashCode() {
        if (!isExpressionQuery()) {
            return super.hashCode();
        }
        int hashCode = 32;
        if (getReferenceClass() != null) {
            hashCode = hashCode + getReferenceClass().hashCode();
        }
        if (getSelectionCriteria() != null) {
            hashCode = hashCode + getSelectionCriteria().hashCode();
        }
        return hashCode;
    }
    
    /**
     * PUBLIC:
     * Return if the query is read-only.
     * This allows queries executed against a UnitOfWork to be read-only.
     * This means the query will be executed against the Session,
     * and the resulting objects will not be tracked for changes.
     * The resulting objects are from the Session shared cache,
     * and must not be modified.
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }
        
    /**
     * PUBLIC:
     * Set the query to be read-only.
     * This allows queries executed against a UnitOfWork to be read-only.
     * This means the query will be executed against the Session,
     * and the resulting objects will not be tracked for changes.
     * The resulting objects are from the Session shared cache,
     * and must not be modified.
     */
    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
    
    /**
     * INTERNAL:
     * Return if this query originally used the default lock mode.
     */
    protected boolean wasDefaultLockMode() {
        return wasDefaultLockMode;
    }
    
    /**
     * INTERNAL:
     * Set if this query originally used the default lock mode.
     */
    protected void setWasDefaultLockMode(boolean wasDefaultLockMode) {
        this.wasDefaultLockMode = wasDefaultLockMode;
    }
    
    /**
     * INTERNAL:
     * Clone the query
     */
    public Object clone() {
        ObjectLevelReadQuery cloneQuery = (ObjectLevelReadQuery)super.clone();

        // Must also clone the joined expressions as always joined attribute will be added
        // don't use setters as this will trigger unprepare.
        if (cloneQuery.hasJoining()) {
            cloneQuery.joinedAttributeManager = (JoinedAttributeManager)cloneQuery.getJoinedAttributeManager().clone();
            cloneQuery.joinedAttributeManager.setBaseQuery(cloneQuery);
        }
        if (hasNonFetchJoinedAttributeExpressions()){
            cloneQuery.setNonFetchJoinAttributeExpressions((Vector)this.nonFetchJoinAttributeExpressions.clone());
        }
        return cloneQuery;
    }

    /**
     * INTERNAL:
     * Clone the query, including its selection criteria.
     * <p>
     * Normally selection criteria are not cloned here as they are cloned
     * later on during prepare.
     */
    public Object deepClone() {
        ObjectLevelReadQuery clone = (ObjectLevelReadQuery)clone();
        if (getSelectionCriteria() != null) {
            clone.setSelectionCriteria((Expression)getSelectionCriteria().clone());
        } else if (defaultBuilder != null) {
            clone.defaultBuilder = (ExpressionBuilder)defaultBuilder.clone();
        }
        return clone;
    }

    /**
     * PUBLIC:
     * Set the query to lock, this will also turn refreshCache on.
     */
    public void acquireLocks() {
        setLockMode(LOCK);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Set the query to lock without waiting (blocking), this will also turn refreshCache on.
     */
    public void acquireLocksWithoutWaiting() {
        setLockMode(LOCK_NOWAIT);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Additional fields can be added to a query.  This is used in m-m bacth reading to bring back the key from the join table.
     */
    public void addAdditionalField(DatabaseField field) {
        getAdditionalFields().add(field);
        // Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Additional fields can be added to a query.  This is used in m-m bacth reading to bring back the key from the join table.
     */
    public void addAdditionalField(Expression fieldExpression) {
        getAdditionalFields().add(fieldExpression);
        // Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Specify the one-to-one mapped attribute to be join fetched in this query.
     * The query will join the object(s) being read with the one-to-one attribute,
     * this allows all of the data required for the object(s) to be read in a single query instead of (n) queries.
     * This should be used when the application knows that it requires the part for all of the objects being read.
     *
     * <p>Note: This cannot be used for objects where it is possible not to have a part,
     * as these objects will be ommited from the result set,
     * unless an outer join is used through passing and expression using "getAllowingNull".
     * To join fetch collection relationships use the addJoinedAttribute(Expression) using "anyOf" ot "anyOfAllowingNone".
     *
     * <p>Example: query.addJoinedAttribute("address")
     *
     * @see #addJoinedAttribute(Expression)
     * @see ReadAllQuery#addBatchReadAttribute(Expression)
     */
    public void addJoinedAttribute(String attributeName) {
        addJoinedAttribute(getExpressionBuilder().get(attributeName));
    }

    /**
     * PUBLIC:
     * Specify the attribute to be join fetched in this query.
     * The query will join the object(s) being read with the specified attribute,
     * this allows all of the data required for the object(s) to be read in a single query instead of (n) queries.
     * This should be used when the application knows that it requires the part for all of the objects being read.
     *
     * <p>Note: This cannot be used for objects where it is possible not to have a part,
     * as these objects will be ommited from the result set,
     * unless an outer join is used through passing and expression using "getAllowingNull".
     *
     * <p>Example: 
     * The following will fetch along with Employee(s) "Jones" all projects they participate in
     * along with teamLeaders and their addresses, teamMembers and their phones.
     * 
     * query.setSelectionCriteria(query.getExpressionBuilder().get("lastName").equal("Jones"));
     * Expression projects = query.getExpressionBuilder().anyOf("projects");
     * query.addJoinedAttribute(projects);
     * Expression teamLeader = projects.get("teamLeader");
     * query.addJoinedAttribute(teamLeader);
     * Expression teamLeaderAddress = teamLeader.getAllowingNull("address");
     * query.addJoinedAttribute(teamLeaderAddress);
     * Expression teamMembers = projects.anyOf("teamMembers");
     * query.addJoinedAttribute(teamMembers);
     * Expression teamMembersPhones = teamMembers.anyOfAllowingNone("phoneNumbers");
     * query.addJoinedAttribute(teamMembersPhones);
     * 
     * Note that:
     * the order is essential: an expression should be added before any expression derived from it;
     * the object is built once - it won't be rebuilt if it to be read again as a joined attribute:
     * in the example the query won't get phones for "Jones" - 
     * even though they are among teamMembers (for whom phones are read).
     *
     */
    public void addJoinedAttribute(Expression attributeExpression) {
        getJoinedAttributeManager().addJoinedAttributeExpression(attributeExpression);
        // Bug2804042 Must un-prepare if prepared as the SQL may change.
        // Joined attributes are now calculated in prePrepare.
        setIsPrePrepared(false);
    }

    /**
     * PUBLIC:
     * Specify the one-to-one mapped attribute to be optimized in this query.
     * The query will join the object(s) being read with the one-to-one 
     * attribute. The difference between this and a joined attribute is that
     * it allows data to be retrieved based on a join, but will not populate
     * the joined attribute. It also allows all of the data required for the 
     * object(s) to be read in a single query instead of (n) queries. This 
     * should be used when the application knows that it requires the part for 
     * all of the objects being read. This can be used only for one-to-one 
     * mappings where the target is not the same class as the source, either 
     * directly or through inheritance.  Also two joins cannot be done to the 
     * same class.
     *
     * <p>Note: This cannot be used for objects where it is possible not to have 
     * a part, as these objects will be ommited from the result set, unless an 
     * outer join is used through passing and expression using "getAllowingNull".
     *
     * <p>Example: query.addNonFetchJoinedAttribute("address")
     *
     * @see #addNonFetchJoinedAttribute(Expression)
     */
    public void addNonFetchJoinedAttribute(String attributeName) {
        addNonFetchJoinedAttribute(getExpressionBuilder().get(attributeName));
    }

    /**
     * PUBLIC:
     * Specify the one-to-one mapped attribute to be optimized in this query.
     * The query will join the object(s) being read with the one-to-one 
     * attribute. The difference between this and a joined attribute is that
     * it allows data to be retrieved based on a join, but will not populate
     * the joined attribute. It also allows all of the data required for the 
     * object(s) to be read in a single query instead of (n) queries. This 
     * should be used when the application knows that it requires the part for 
     * all of the objects being read. This can be used only for one-to-one 
     * mappings where the target is not the same class as the source, either 
     * directly or through inheritance.  Also two joins cannot be done to the 
     * same class.
     *
     * <p>Note: This cannot be used for objects where it is possible not to have 
     * a part, as these objects will be ommited from the result set, unless an 
     * outer join is used through passing and expression using "getAllowingNull".
     *
     * <p>Example: query.addNonFetchJoinedAttribute(query.getExpressionBuilder().get("teamLeader").get("address"))
     *
     * @see #addNonFetchJoinedAttribute(Expression)
     */
    public void addNonFetchJoinedAttribute(Expression attributeExpression) {
        getNonFetchJoinAttributeExpressions().add(attributeExpression);
        
        // Bug 2804042 Must un-prepare if prepared as the SQL may change.
        // Joined attributes are now calculated in prePrepare.
        setIsPrePrepared(false);
    }

    /**
     * PUBLIC:
     * Specify that only a subset of the class' attributes be selected in this query.
     * <p>
     * This allows for the query to be optimized through selecting less data.
     * <p>
     * Partial objects will be returned from the query, where the unspecified attributes will be left <code>null</code>.
     * The primary key will always be selected to allow re-querying of the whole object.
     * <p>Note: Because the object is not fully initialized it cannot be cached, and cannot be edited.
     * <p>Note: You cannot have 2 partial attributes of the same type.  You also cannot
     * add a partial attribute which is of the same type as the class being queried.
     * <p><b>Example</b>: query.addPartialAttribute("firstName")
     * @see #addPartialAttribute(Expression)
     */
    public void addPartialAttribute(String attributeName) {
        addPartialAttribute(getExpressionBuilder().get(attributeName));
    }

    /**
     * INTERNAL:
     * Iterate through a list of joined expressions and add the fields they represent to a list
     * of fields.
     */
    protected void addSelectionFieldsForJoinedExpressions(List fields, List joinedExpressions) {
        for (int index = 0; index < joinedExpressions.size(); index++) {
            ObjectExpression objectExpression = (ObjectExpression)joinedExpressions.get(index);

            // Expression may not have been initialized.
            ExpressionBuilder builder = objectExpression.getBuilder();
            builder.setSession(getSession().getRootSession(null));
            builder.setQueryClass(getReferenceClass());
            ClassDescriptor descriptor = objectExpression.getMapping().getReferenceDescriptor();
            fields.addAll(descriptor.getAllFields());
        }
    }

    /**
     * ADVANCED: Sets the query to execute as of the past time.
     * Both the query execution and result will conform to the database as it
     * existed in the past.
     * <p>
     * Equivalent to query.getSelectionCriteria().asOf(pastTime) called
     * immediately before query execution.
     * <p>An as of clause at the query level will override any clauses set at the
     * expression level.  Useful in cases where the selection criteria is not known in
     * advance, such as for query by example or primary key (selection object), or
     * where you do not need to cache the result (report query).
     * <p>Ideally an as of clause at the session level is superior as query
     * results can then be cached.  You must set
     * {@link oracle.toplink.queryframework.ObjectLevelReadQuery#setShouldMaintainCache setShouldMaintainCache(false)}
     * <p>To query all joined/batched attributes as of the same time set
     * this.{@link oracle.toplink.queryframework.ObjectLevelReadQuery#cascadeAllParts cascadeAllParts()}.
     * @throws QueryException (at execution time) unless
     * <code>setShouldMaintainCache(false)</code> is set.  If some more recent
     * data were in the cache, this would be returned instead, and both the
     * cache and query result would become inconsistent.
     * @since OracleAS TopLink 10<i>g</i> (10.0.3)
     * @see #hasAsOfClause
     * @see oracle.toplink.sessions.Session#acquireHistoricalSession(oracle.toplink.history.AsOfClause)
     * @see oracle.toplink.expressions.Expression#asOf(oracle.toplink.history.AsOfClause)
     */
    public void setAsOfClause(AsOfClause pastTime) {
        getExpressionBuilder().asOf(new UniversalAsOfClause(pastTime));
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Specify that only a subset of the class' attributes be selected in this query.
     * <p>This allows for the query to be optimized through selecting less data.
     * <p>Partial objects will be returned from the query, where the unspecified attributes will be left <code>null</code>.
     * The primary key will always be selected to allow re-querying of the whole object.
     * <p>Note: Because the object is not fully initialized it cannot be cached, and cannot be edited.
     * <p>Note: You cannot have 2 partial attributes of the same type.  You also cannot
     * add a partial attribute which is of the same type as the class being queried.
     * <p><b>Example</b>: query.addPartialAttribute(query.getExpressionBuilder().get("address").get("city"))
     */
    public void addPartialAttribute(Expression attributeExpression) {
        getPartialAttributeExpressions().addElement(attributeExpression);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Used to build the object, and register it if in the context of a unit of work. 
     */
    public Object buildObject(AbstractRecord row) {
        return getDescriptor().getObjectBuilder().buildObject(this, row);
    }

    /**
     * PUBLIC:
     * The cache will checked completely, if the object is not found null will be returned or an error if the query is too complex.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void checkCacheOnly() {
        setCacheUsage(CheckCacheOnly);
    }

    /**
     * INTERNAL:
     * Ensure that the descriptor has been set.
     */
    public void checkDescriptor(AbstractSession session) throws QueryException {
        if (getDescriptor() == null) {
            if (getReferenceClass() == null) {
                throw QueryException.referenceClassMissing(this);
            }
            ClassDescriptor referenceDescriptor = session.getDescriptor(getReferenceClass());
            if (referenceDescriptor == null) {
                throw QueryException.descriptorIsMissing(getReferenceClass(), this);
            }
            setDescriptor(referenceDescriptor);
        }
    }

    /**
     * INTERNAL:
     * Contains the body of the check early return call, implemented by subclasses.
     */
    protected abstract Object checkEarlyReturnImpl(AbstractSession session, AbstractRecord translationRow);

    /**
     * INTERNAL:
     * Check to see if this query already knows the return vale without preforming any further work.
     */
    public Object checkEarlyReturn(AbstractSession session, AbstractRecord translationRow) {
        // For bug 3136413/2610803 building the selection criteria from an EJBQL string or
        // an example object is done just in time.
        // Also calls checkDescriptor here.
        //buildSelectionCriteria(session);
        checkPrePrepare(session);

        if (!session.isUnitOfWork()) {
            return checkEarlyReturnImpl(session, translationRow);
        }
        UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)session;

        // The cache check must happen on the UnitOfWork in these cases either
        // to access transient state or for pessimistic locking, as only the 
        // UOW knows which objects it has locked.
        // Bug 6978106 - Added unitOfWork.wasTransactionBegunPrematurely() call to allow UnitOfWork cache checking 
        // for transactions begun early; This is necessary where indirection is not configured on relationships.
        Object result = checkEarlyReturnImpl(unitOfWork, translationRow);
        if (result != null) {
            return result;
        }

        // don't bother trying to get a cache hit on the parent session
        // as if not in UnitOfWork it is not yet pessimistically locked
        // on the database for sure.
        // Note for ReadObjectQueries we totally ignore shouldCheckCacheOnly.
        if (isReadObjectQuery() && isLockQuery()) {
            return null;
        }

        // follow the execution path in looking for the object.
        AbstractSession parentSession = unitOfWork.getParentIdentityMapSession(this);

        // assert parentSession != unitOfWork;
        result = checkEarlyReturn(parentSession, translationRow);

        if (result != null) {
            // Optimization: If find deleted object by exact primary key
            // treat this as cache hit but return null.  Bug 2782991.
            if (result == InvalidObject.instance) {
                return result;
            }
            return registerResultInUnitOfWork(result, unitOfWork, translationRow, false);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Check to see if this query needs to be prepare and prepare it.
     * The prepare is done on the original query to ensure that the work is not repeated.
     */
    public void checkPrepare(AbstractSession session, AbstractRecord translationRow, boolean force) {
        // CR#3823735 For custom queries the prePrepare may not have been called yet.
        checkPrePrepare(session);
        super.checkPrepare(session, translationRow, force);
    }

    /**
     * INTERNAL:
     * ObjectLevelReadQueries now have an explicit pre-prepare stage, which
     * is for checking for pessimistic locking, and computing any joined
     * attributes declared on the descriptor.
     */
    protected void checkPrePrepare(AbstractSession session) {
        if (getRedirector() != null) {
            return;
        }
        // Must always check descriptor as transient for remote.
        checkDescriptor(session);
        
        // This query is first prepared for global common state, this must be synced.
        if (!isPrePrepared()) {// Avoid the monitor is already prePrepare, must check again for concurrency.      
            synchronized (this) {
                if (!isPrePrepared()) {
                    AbstractSession alreadySetSession = getSession();
                    setSession(session);// Session is required for some init stuff.
                    prePrepare();
                    setSession(alreadySetSession);
                    setIsPrePrepared(true);// MUST not set prepare until done as other thread may hit before finishing the prePrepare.
                }
            }
        }
    }

    /**
     * INTERNAL:
     * The reference class has been changed, need to reset the
     * descriptor. Null out the current descriptor and call
     * checkDescriptor
     * Added Feb 27, 2001 JED for EJBQL feature
     */
    public void changeDescriptor(AbstractSession theSession) {
        setDescriptor(null);
        checkDescriptor(theSession);
    }

    /**
     * INTERNAL:
     * Conforms and registers an individual result.  This instance could be one
     * of the elements returned from a read all query, the result of a Read Object
     * query, or an element read from a cursor.
     * <p>
     * A result needs to be registered before it can be conformed, so
     * registerIndividualResult is called here.
     * <p>
     * Conforming on a result from the database is lenient.  Since the object
     * matched the query on the database we assume it matches here unless we can
     * determine for sure that it was changed in this UnitOfWork not to conform.
     * @param result may be an original, or a raw database row
     * @param arguments the parameters this query was executed with
     * @param selectionCriteriaClone the expression to conform to.  If was a
     * selection object or key, null (which all conform to) is used
     * @param alreadyReturned a hashtable of objects already found by scanning
     * the UnitOfWork cache for conforming instances.  Prevents duplicates.
     * @param buildDirectlyFromRows whether result is an original or a raw database
     * row
     * @return a clone, or null if result does not conform.
     */
    protected Object conformIndividualResult(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord arguments, Expression selectionCriteriaClone, IdentityHashtable alreadyReturned, boolean buildDirectlyFromRows) {
        // First register the object.  Since object is presently unwrapped the
        // exact objects stored in the cache will be returned.
        // The object is known to exist.
        Object clone = null;
        if (buildDirectlyFromRows) {
            clone = buildObject((AbstractRecord)result);
        } else {
            clone = registerIndividualResult(result, unitOfWork, null);
        }

        if (getDescriptor().hasWrapperPolicy() && getDescriptor().getWrapperPolicy().isWrapped(clone)) {
            // The only time the clone could be wrapped is if we are not registering
            // results in the unitOfWork and we are ready to return a final
            // (unregistered) result now.  Any further processing may accidently
            // cause it to get registered.
            return clone;
        }
        //bug 4459976 in order to maintain backward compatibility on ordering
        // lets use the result as a guild for the final result not the hashtable
        // of found objects.
        if (unitOfWork.isObjectDeleted(clone) ) {
            return null;
        }
        if (!isExpressionQuery() || (selectionCriteriaClone == null)) {
            if (alreadyReturned != null) {
                alreadyReturned.remove(clone);
            }
            return clone;
        }
        try {
            // pass in the policy to assume that the object conforms if indirection is not triggered.  This
            // is valid because the query returned the object and we should trust the query that the object
            // matches the selection criteria, and because the indirection is not triggered then the customer
            //has not changed the value.
            // bug 2637555
            // unless for bug 3568141 use the painstaking shouldTriggerIndirection if set
            int policy = getInMemoryQueryIndirectionPolicyState();
            if (policy != InMemoryQueryIndirectionPolicy.SHOULD_TRIGGER_INDIRECTION) {
                policy = InMemoryQueryIndirectionPolicy.SHOULD_IGNORE_EXCEPTION_RETURN_CONFORMED;
            }
            if (selectionCriteriaClone.doesConform(clone, unitOfWork, arguments, policy)) {
                 if (alreadyReturned != null) {
                    alreadyReturned.remove(clone);
                }
               return clone;
            }
        } catch (QueryException exception) {
            // bug 3570561: mask all-pervasive valueholder exceptions while conforming
            if ((unitOfWork.getShouldThrowConformExceptions() == UnitOfWorkImpl.THROW_ALL_CONFORM_EXCEPTIONS) && (exception.getErrorCode() != QueryException.MUST_INSTANTIATE_VALUEHOLDERS)) {
                throw exception;
            }
            if (alreadyReturned != null) {
                alreadyReturned.remove(clone);
            }
            return clone;
        }
        return null;
    }

    /**
     * PUBLIC:
     * The cache will checked completely, if the object is not found the database will be queried,
     * and the database result will be verified with what is in the cache and/or unit of work including new objects.
     * This can lead to poor performance so it is recomended that only the database be queried in most cases.
     * Queries can be configured to use the cache at several levels.
     * Other caching option are available.
     * @see #setCacheUsage(int)
     */
    public void conformResultsInUnitOfWork() {
        setCacheUsage(ConformResultsInUnitOfWork);
    }

    /**
     * PUBLIC:
     * Set the query not to lock.
     */
    public void dontAcquireLocks() {
        setLockMode(NO_LOCK);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * This can be used to explicitly disable the cache hit.
     * The cache hit may not be desired in some cases, such as
     * stored procedures that accept the primary key but do not query on it.
     */
    public void dontCheckCache() {
        setCacheUsage(DoNotCheckCache);
    }

    /**
     * PUBLIC:
     * When unset means perform read normally and dont do refresh.
     */
    public void dontRefreshIdentityMapResult() {
        setShouldRefreshIdentityMapResult(false);
    }

    /**
     * PUBLIC:
     * When unset means perform read normally and dont do refresh.
     */
    public void dontRefreshRemoteIdentityMapResult() {
        setShouldRefreshRemoteIdentityMapResult(false);
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void dontUseDistinct() {
        setDistinctState(DONT_USE_DISTINCT);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * There is a very special case where a query may be a bean-level
     * pessimistic locking query.
     * <p>
     * If that is so, only queries executed inside of a UnitOfWork should
     * have a locking clause.  In the extremely rare case that we execute
     * a locking query outside of a UnitOfWork, must disable locking so that
     * we do not get a fetch out of sequence error.
     */
    public DatabaseQuery prepareOutsideUnitOfWork(AbstractSession session) {
        // Implementation is complicated because: if locking refresh will be
        // auto set to true preventing cache hit.
        // Must prepare this query from scratch if outside uow but locking
        // Must not reprepare this query as a NO_LOCK, but create a clone first
        // Must not cloneAndUnPrepare unless really have to
        if (isLockQuery(session) && getLockingClause().isForUpdateOfClause()) {
            ObjectLevelReadQuery clone = (ObjectLevelReadQuery)clone();
            clone.setIsExecutionClone(true);
            clone.dontAcquireLocks();
            clone.setIsPrepared(false);
            clone.checkPrePrepare(session);
            return clone;
        }
        return this;
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
        //Bug#2839852  Refreshing is not possible if the query uses checkCacheOnly.
        if (shouldRefreshIdentityMapResult() && shouldCheckCacheOnly()) {
            throw QueryException.refreshNotPossibleWithCheckCacheOnly(this);
        }
        return super.execute(session, translationRow);
    }

    /**
     * INTERNAL:
     * Executes the prepared query on the datastore.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        if (getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();

            // Note if a nested unit of work this will recursively start a
            // transaction early on the parent also.
            if (isLockQuery()) {
                if ((!unitOfWork.getCommitManager().isActive()) && (!unitOfWork.wasTransactionBegunPrematurely())) {
                    unitOfWork.beginTransaction();
                    unitOfWork.setWasTransactionBegunPrematurely(true);
                }
            }
            if (unitOfWork.isNestedUnitOfWork()) {
                UnitOfWorkImpl nestedUnitOfWork = (UnitOfWorkImpl)getSession();
                setSession(nestedUnitOfWork.getParent());
                Object result = executeDatabaseQuery();
                setSession(nestedUnitOfWork);
                return registerResultInUnitOfWork(result, nestedUnitOfWork, getTranslationRow(), false);
            }
        }
        // PERF: If the query has been set to optimize building its result
        // directly from the database result-set then follow an optimized path.
        if (isResultSetOptimizedQuery()) {
            return executeObjectLevelReadQueryFromResultSet();
        }
        
        session.validateQuery(this);// this will update the query with any settings

        if (getQueryId() == 0) {
            setQueryId(getSession().getNextQueryId());
        }

        return executeObjectLevelReadQuery();
    }

    /**
     * Executes the prepared query on the datastore.
     */
    protected abstract Object executeObjectLevelReadQuery() throws DatabaseException;
    
    /**
     * Executes the prepared query on the datastore.
     */
    protected abstract Object executeObjectLevelReadQueryFromResultSet() throws DatabaseException;

    /**
     * INTERNAL:
     * Execute the query in the unit of work.
     * This allows any pre-execute checks to be done for unit of work queries.
     */
    public Object executeInUnitOfWork(UnitOfWorkImpl unitOfWork, AbstractRecord translationRow) throws DatabaseException, OptimisticLockException {
        if (!shouldMaintainCache() || isReadOnly()) {
            return unitOfWork.getParent().executeQuery(this, translationRow);
        }
        // code removed that through exception post write changes.  This code removed
        // as reads no longer load into shared cache once UOW is in transaction.
        
        Object result = execute(unitOfWork, translationRow);

        // Optimization: If find deleted object on uow by exact primary key
        // treat this as cache hit but return null.  Bug 2782991.
        if (result == InvalidObject.instance) {
            return null;
        }
        return result;
    }

    /**
     * INTERNAL:
     * Additional fields can be added to a query.  This is used in m-m bacth reading to bring back the key from the join table.
     */
    public Vector getAdditionalFields() {
        if (this.additionalFields == null) {
            this.additionalFields = NonSynchronizedVector.newInstance();
        }
        return this.additionalFields;
    }

    /**
     * ADVANCED:
     * Answers the past time this query is as of.
     * @return An immutable object representation of the past time.
     * <code>null</code> if no clause set, <code>AsOfClause.NO_CLAUSE</code> if
     * clause explicitly set to <code>null</code>.
     * @see oracle.toplink.history.AsOfClause
     * @see #setAsOfClause(oracle.toplink.history.AsOfClause)
     * @see #hasAsOfClause
     */
    public AsOfClause getAsOfClause() {
        return (defaultBuilder != null) ? defaultBuilder.getAsOfClause() : null;
    }

    /**
     * PUBLIC:
     * Return the cache usage.
     * By default only primary key read object queries will first check the cache before accessing the database.
     * Any query can be configure to query against the cache completely, by key or ignore the cache check.
     * <p>Valid values are:
     * <ul>
     * <li> DoNotCheckCache
     * <li> CheckCacheByExactPrimaryKey
     * <li> CheckCacheByPrimaryKey
     * <li> CheckCacheThenDatabase
     * <li> CheckCacheOnly
     * <li> ConformResultsInUnitOfWork
     * <li> UseDescriptorSetting
     * Note: UseDescriptorSetting functions like CheckCacheByPrimaryKey, except checks the appropriate descriptor's
     * shouldDisableCacheHits setting when querying on the cache.
     * </ul>
     */
    public int getCacheUsage() {
        return cacheUsage;
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public short getDistinctState() {
        return distinctState;
    }

    /**
     * PUBLIC:
     * This method returns the current example object.  The "example" object is an actual domain object, provided
     * by the client, from which an expression is generated.
     * This expression is used for a query of all objects from the same class, that match the attribute values of
     * the "example" object.
     */
    public Object getExampleObject() {
        if (getQueryMechanism().isQueryByExampleMechanism()) {
            return ((QueryByExampleMechanism)getQueryMechanism()).getExampleObject();
        } else {
            return null;
        }
    }

    /**
     * REQUIRED:
     * Get the expression builder which should be used for this query.
     * This expression builder should be used to build all expressions used by this query.
     */
    public ExpressionBuilder getExpressionBuilder() {
        if (defaultBuilder == null) {
            initializeDefaultBuilder();
        }

        return defaultBuilder;
    }

    /**
     * INTERNAL
     * Sets the default expression builder for this query.
     */
    public void setExpressionBuilder(ExpressionBuilder builder) {
        this.defaultBuilder = builder;
    }

    /**
     * PUBLIC:
     * Returns the InMemoryQueryIndirectionPolicy for this query
     */
    public int getInMemoryQueryIndirectionPolicyState() {
        return this.inMemoryQueryIndirectionPolicy;
    }
    
    /**
     * PUBLIC:
     * Returns the InMemoryQueryIndirectionPolicy for this query
     */
    public InMemoryQueryIndirectionPolicy getInMemoryQueryIndirectionPolicy() {
        return new InMemoryQueryIndirectionPolicy(inMemoryQueryIndirectionPolicy, this);
    }

    /**
     * INTERNAL:
     * Return join manager responsible for managing all aspects of joining for the query.
     * Queries without joining should not have a joinedAttributeManager.
     */
    public JoinedAttributeManager getJoinedAttributeManager() {
        if (this.joinedAttributeManager == null) {
            this.joinedAttributeManager = new JoinedAttributeManager(getDescriptor(), getExpressionBuilder(), this);
        }
        return this.joinedAttributeManager;
    }
    
    /**
     * INTERNAL:
     * Set join manager responsible for managing all aspects of joining for the query.
     */
    public void setJoinedAttributeManager(JoinedAttributeManager joinedAttributeManager) {
        this.joinedAttributeManager = joinedAttributeManager;
    }
    
    /**
     * INTERNAL:
     * Return if any attributes are joined.
     * To avoid the initialization of the JoinedAttributeManager this should be first checked before accessing.
     */
    public boolean hasJoining() {
        return this.joinedAttributeManager != null;
    }

    /**
     * INTERNAL:
     * Convience method for project mapping.
     */
    public List getJoinedAttributeExpressions() {
        return getJoinedAttributeManager().getJoinedAttributeExpressions();
    }
 
    /**
     * INTERNAL:
     * Convience method for project mapping.
     */
    public void setJoinedAttributeExpressions(List expressions) {
        if (((expressions != null) && !expressions.isEmpty()) || hasJoining()) {
            getJoinedAttributeManager().setJoinedAttributeExpressions_(expressions);
        }
    }
            
    /**
     * PUBLIC:
     * Return if duplicate rows should be filter when using 1-m joining.
     */
    public boolean shouldFilterDuplicates() {
        if (hasJoining()) {
            return getJoinedAttributeManager().shouldFilterDuplicates();
        }
        return true;
    }
        
    /**
     * PUBLIC:
     * Set if duplicate rows should be filter when using 1-m joining.
     */
    public void setShouldFilterDuplicates(boolean shouldFilterDuplicates) {
        getJoinedAttributeManager().setShouldFilterDuplicates(shouldFilterDuplicates);
    }
    
   /**
     * INTERNAL:
     * Lookup the mapping for this item by traversing its expression recursively.
     * If an aggregate of foreign mapping is found it is traversed.
     */
    public DatabaseMapping getLeafMappingFor(Expression expression, ClassDescriptor rootDescriptor) throws QueryException {
        // Check for database field expressions or place holder
        if ((expression == null) || (expression.isFieldExpression())) {
            return null;
        }

        if (!(expression.isQueryKeyExpression())) {
            return null;
        }

        QueryKeyExpression qkExpression = (QueryKeyExpression)expression;
        Expression baseExpression = qkExpression.getBaseExpression();

        ClassDescriptor descriptor = getLeafDescriptorFor(baseExpression, rootDescriptor);
        return descriptor.getObjectBuilder().getMappingForAttributeName(qkExpression.getName());
    }

    /**
     * INTERNAL:
     * Lookup the descriptor for this item by traversing its expression recursively.
     * @param expression
     * @param rootDescriptor
     * @return
     * @throws oracle.toplink.exceptions.QueryException
     */
    public ClassDescriptor getLeafDescriptorFor(Expression expression, ClassDescriptor rootDescriptor) throws QueryException {
        // The base case
        if (expression.isExpressionBuilder()) {
            // The following special case is where there is a parallel builder
            // which has a different reference class as the primary builder.
            Class queryClass = ((ExpressionBuilder)expression).getQueryClass();
            if ((queryClass != null) && (queryClass != getReferenceClass())) {
                return getSession().getDescriptor(queryClass);
            }
            return rootDescriptor;
        }
        Expression baseExpression = ((QueryKeyExpression)expression).getBaseExpression();
        ClassDescriptor baseDescriptor = getLeafDescriptorFor(baseExpression, rootDescriptor);
        ClassDescriptor descriptor = null;
        String attributeName = expression.getName();

        DatabaseMapping mapping = baseDescriptor.getObjectBuilder().getMappingForAttributeName(attributeName);

        if (mapping == null) {
            QueryKey queryKey = baseDescriptor.getQueryKeyNamed(attributeName);
            if (queryKey != null) {
                if (queryKey.isForeignReferenceQueryKey()) {
                    descriptor = getSession().getDescriptor(((ForeignReferenceQueryKey)queryKey).getReferenceClass());
                } else// if (queryKey.isDirectQueryKey())
                 {
                    descriptor = queryKey.getDescriptor();
                }
            }
            if (descriptor == null) {
                throw QueryException.invalidExpressionForQueryItem(expression, this);
            }
        } else if (mapping.isAggregateObjectMapping()) {
            descriptor = ((AggregateObjectMapping)mapping).getReferenceDescriptor();
        } else if (mapping.isForeignReferenceMapping()) {
            descriptor = ((ForeignReferenceMapping)mapping).getReferenceDescriptor();
        }
        return descriptor;
    }

    /**
     * PUBLIC:
     * Return the current locking mode.
     */
    public short getLockMode() {
        if (lockingClause == null) {
            return DEFAULT_LOCK_MODE;
        } else {
            return lockingClause.getLockMode();
        }
    }

    /**
     * INTERNAL:
     * It is not exactly as simple as a query being either locking or not.
     * Any combination of the reference class object and joined attributes
     * may be locked.
     */
    public ForUpdateClause getLockingClause() {
        return lockingClause;
    }

    /**
     * INTERNAL:
     * Return the attributes that must be joined, but not fetched, that is,
     * do not trigger the value holder.
     */
    public Vector getNonFetchJoinAttributeExpressions() {
        if (this.nonFetchJoinAttributeExpressions == null){
            this.nonFetchJoinAttributeExpressions = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        }
        return nonFetchJoinAttributeExpressions;
    }

    /**
     * INTERNAL:
     * Return the partial attributes to select.
     */
    public Vector getPartialAttributeExpressions() {
        if (this.partialAttributeExpressions == null) {
            this.partialAttributeExpressions = NonSynchronizedVector.newInstance();
        }
        return this.partialAttributeExpressions;
    }


    /**
     * INTERNAL:
     * Return the time this query actually went to the database
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * PUBLIC:
     * When using Query By Example, an instance of QueryByExamplePolicy is used to customize the query.
     * The policy is useful when special operations are to be used for comparisons (notEqual, lessThan,
     * greaterThan, like etc.), when a certain value is to be ignored, or when dealing with nulls.
     */
    public QueryByExamplePolicy getQueryByExamplePolicy() {
        if (getQueryMechanism().isQueryByExampleMechanism()) {
            return ((QueryByExampleMechanism)getQueryMechanism()).getQueryByExamplePolicy();
        } else {
            return null;
        }
    }

    /**
     * PUBLIC:
     * Return the reference class of the query.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Return the reference class of the query.
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * Used by TopLink to persist the expression into XML.  Will convert to
     * oracle.toplink.tools.workbench.expressions.* types.
     * Please note that this feature should be revisted for future releases.
     */
    public oracle.toplink.tools.workbench.expressions.ExpressionRepresentation getSelectionCriteriaForPersistence() {
        Expression expression = this.getSelectionCriteria();
        oracle.toplink.tools.workbench.expressions.ExpressionRepresentation result = null;
        if (expression != null) {
            // CR#... this was calling compound, but need to call either compound or basic.
            result = oracle.toplink.tools.workbench.expressions.ExpressionRepresentation.convertFromRuntime(expression);
        }
        return result;
    }

    /**
     * PUBLIC:
     * Answers if the domain objects are to be read as of a past time.
     * @see #getAsOfClause
     */
    public boolean hasAsOfClause() {
        return ((defaultBuilder != null) && defaultBuilder.hasAsOfClause());
    }

    /**
     * INTERNAL:
     * Return the attributes that must be joined.
     */
    public boolean hasNonFetchJoinedAttributeExpressions() {
        return this.nonFetchJoinAttributeExpressions != null && !this.nonFetchJoinAttributeExpressions.isEmpty();
    }

    /**
     * INTERNAL:
     * Return if partial attributes.
     */
    public boolean hasPartialAttributeExpressions() {
        return (partialAttributeExpressions != null) && (!partialAttributeExpressions.isEmpty());
    }
    
    /**
     * INTERNAL:
     * Return if additional field.
     */
    public boolean hasAdditionalFields() {
        return (additionalFields != null) && (!additionalFields.isEmpty());
    }
    
    /**
     * INTERNAL:
     * Return the fields required in the select clause, for patial attribute reading.
     */
    public Vector getPartialAttributeSelectionFields(boolean isCustomSQL) {
        Vector localFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(getPartialAttributeExpressions().size());
        Vector foreignFields = null;
        
        //Add primary key and indicator fields.
        localFields.addAll(getDescriptor().getPrimaryKeyFields());
        if (getDescriptor().hasInheritance() && (getDescriptor().getInheritancePolicy().getClassIndicatorField() != null)) {
            localFields.addElement(getDescriptor().getInheritancePolicy().getClassIndicatorField());
        }

        //Add attribute fields
        for(Iterator it = getPartialAttributeExpressions().iterator();it.hasNext();){
            Expression expression = (Expression)it.next();
            if (expression.isQueryKeyExpression()) {
                ((QueryKeyExpression)expression).getBuilder().setSession(session.getRootSession(null));
                ((QueryKeyExpression)expression).getBuilder().setQueryClass(getDescriptor().getJavaClass());
                DatabaseMapping mapping = ((QueryKeyExpression)expression).getMapping();
                if (!((QueryKeyExpression)expression).getBaseExpression().isExpressionBuilder()) {
                    if(foreignFields==null){
                        foreignFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
                    }
                    if(!isCustomSQL){
                        foreignFields.add(expression);
                    }else{
                        foreignFields.addAll(expression.getFields());
                    }
                }else{
                    if (mapping == null) {
                        throw QueryException.specifiedPartialAttributeDoesNotExist(this, expression.getName(), descriptor.getJavaClass().getName());
                    }
                    if(mapping.isForeignReferenceMapping() ){
                        if(foreignFields==null){
                            foreignFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
                        }
                        if(!isCustomSQL){
                            foreignFields.add(expression);
                        }else{
                            foreignFields.addAll(expression.getFields());
                        }
                    }else{
                        localFields.addAll(expression.getFields());
                    }
                }
            } else {
                throw QueryException.expressionDoesNotSupportPartialAttributeReading(expression);
            }
        }
        //Build fields in same order as the fields of the descriptor to ensure field and join indexes match.
        Vector selectionFields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance();
        for (Iterator iterator = getDescriptor().getFields().iterator(); iterator.hasNext();) {
            DatabaseField field = (DatabaseField)iterator.next();
            if (localFields.contains(field)) {
                selectionFields.add(field);
            } else {
                selectionFields.add(null);
            }
        }
        //Combine fields list for source descriptor and target descriptor.
        if(foreignFields!=null){
            selectionFields.addAll(foreignFields);
        }
        return selectionFields;
    }
    
    /**
     * INTERNAL:
     * Return the fields required in the select clause, for fetch group reading.
     */
    public Vector getFetchGroupSelectionFields(boolean isCustomSQL) {
        Set fetchedFields = new HashSet(getFetchGroup().getAttributes().size() + 2);
        // Add required fields.
        fetchedFields.addAll(getDescriptor().getPrimaryKeyFields());
        if (getDescriptor().hasInheritance() && (getDescriptor().getInheritancePolicy().getClassIndicatorField() != null)) {
            fetchedFields.add(getDescriptor().getInheritancePolicy().getClassIndicatorField());
        }
        if (shouldMaintainCache() && getDescriptor().usesOptimisticLocking()) {
            DatabaseField lockField = getDescriptor().getOptimisticLockingPolicy().getWriteLockField();
            if (lockField != null) {
                fetchedFields.add(lockField);
            }
        }
        // Add specified fields.
        for (Iterator iterator = getFetchGroup().getAttributes().iterator(); iterator.hasNext();) {
            String attribute = (String)iterator.next();
            DatabaseMapping mapping = getDescriptor().getObjectBuilder().getMappingForAttributeName(attribute);
            if (mapping == null) {
                throw QueryException.fetchGroupAttributeNotMapped(attribute);
            }
            fetchedFields.addAll(mapping.getFields());
        }
        
        // Build field list in the same order as descriptor's fields to ensure field and join indexes match.
        // Put null placeholders in place of non-selected fields.        
        Vector fields = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(getFetchGroup().getAttributes().size() + 2);
        for (Iterator iterator = getDescriptor().getFields().iterator(); iterator.hasNext();) {
            DatabaseField field = (DatabaseField)iterator.next();
            if (fetchedFields.contains(field)) {
                fields.add(field);
            } else {
                fields.add(null);
            }
        }
        
        // Add joined fields.
        if(hasJoining()){
            if(isCustomSQL){
                addSelectionFieldsForJoinedExpressions(fields, getJoinedAttributeManager().getJoinedAttributeExpressions());
                addSelectionFieldsForJoinedExpressions(fields, getJoinedAttributeManager().getJoinedMappingExpressions());
            }else{
                Helper.addAllToVector(fields, getJoinedAttributeManager().getJoinedAttributeExpressions());
                Helper.addAllToVector(fields, getJoinedAttributeManager().getJoinedMappingExpressions());
            }
        }
        
        if (hasAdditionalFields()) {
            // Add additional fields, use for batch reading m-m.
            Helper.addAllToVector(fields, getAdditionalFields());
        }
        return fields;
    }

    /**
     * INTERNAL:
     * Return the fields selected by the query.
     * This includes the partial or joined fields.
     * This is only used for custom SQL executions.
     */
    public Vector getSelectionFields() {
        if (hasPartialAttributeExpressions()) {
            return getPartialAttributeSelectionFields(true);
        } else if (hasFetchGroup()) {
            return getFetchGroupSelectionFields(true);
        } else if (hasJoining()) {
            JoinedAttributeManager joinManager = getJoinedAttributeManager();
            Vector fields = NonSynchronizedVector.newInstance(getDescriptor().getAllFields().size() + joinManager.getJoinedAttributeExpressions().size() + joinManager.getJoinedMappingExpressions().size());
            Helper.addAllToVector(fields, getDescriptor().getAllFields());
            addSelectionFieldsForJoinedExpressions(fields, joinManager.getJoinedAttributeExpressions());
            addSelectionFieldsForJoinedExpressions(fields, joinManager.getJoinedMappingExpressions());
            return fields;
        }
        return getDescriptor().getAllFields();
    }
 
    /**
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder, otherwise
     * generate one and cache it. This helps avoid unnecessary rebuilds.
     */
    protected void initializeDefaultBuilder() {
        DatabaseQueryMechanism mech = getQueryMechanism();
        if (mech.isExpressionQueryMechanism() && ((ExpressionQueryMechanism)mech).getExpressionBuilder() != null) {
            this.defaultBuilder = ((ExpressionQueryMechanism)mech).getExpressionBuilder();
            return;
        }
        this.defaultBuilder = new ExpressionBuilder();
    }

    /**
     * INTERNAL:
     * return true if this query has computed its distinct value already
     */
    public boolean isDistinctComputed() {
        return getDistinctState() != UNCOMPUTED_DISTINCT;
    }

    /**
     * PUBLIC:
     * Answers if the query lock mode is known to be LOCK or LOCK_NOWAIT.
     *
     * In the case of DEFAULT_LOCK_MODE and the query reference class being a CMP entity bean,
     * at execution time LOCK, LOCK_NOWAIT, or NO_LOCK will be decided.
     * <p>
     * If a single joined attribute was configured for pessimistic locking then
     * this will return true (after first execution) as the SQL contained a
     * FOR UPDATE OF clause.
     */
    public boolean isLockQuery() {
        return getLockMode() > NO_LOCK;
    }

    /**
     * ADVANCED:
     * Answers if this query will issue any pessimistic locks.
     * <p>
     * If the lock mode is not known (DEFAULT_LOCK_MODE / descriptor specified
     * fine-grained locking) the lock mode will be determined now, to be either
     * LOCK, LOCK_NOWAIT, or NO_LOCK.
     * @see #isLockQuery()
     */
    public boolean isLockQuery(oracle.toplink.sessions.Session session) {
        checkPrePrepare((AbstractSession)session);
        return isLockQuery();
    }

    /**
     * PUBLIC:
     * Return if this is an object level read query.
     */
    public boolean isObjectLevelReadQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Return if partial attribute.
     */
    public boolean isPartialAttribute(String attributeName) {
        if (!hasPartialAttributeExpressions()) {
            return false;
        }
        Vector partialAttributeExpressions = getPartialAttributeExpressions();
        int size = partialAttributeExpressions.size();
        for (int index = 0; index < size; index++) {
            QueryKeyExpression expression = (QueryKeyExpression)partialAttributeExpressions.get(index);
            while (!expression.getBaseExpression().isExpressionBuilder()) {
                expression = (QueryKeyExpression)expression.getBaseExpression();
            }
            if (expression.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * PUBLIC:
     * Queries prepare common stated in themselves.
     */
    protected boolean isPrePrepared() {
        return isPrePrepared;
    }

    /**
     * INTERNAL:
     * Answers if we are executing through a UnitOfWork and registering results.
     * This is only ever false if using the conforming without registering
     * feature.
     */
    protected boolean isRegisteringResults() {
        return ((shouldRegisterResultsInUnitOfWork() && getDescriptor().shouldRegisterResultsInUnitOfWork()) || isLockQuery());
    }

    /**
     * INTERNAL:
     * If changes are made to the query that affect the derived SQL or Call
     * parameters the query needs to be prepared again.
     * <p>
     * Automatically called internally.
     * <p>
     * The early phase of preparation is to check if this is a pessimistic
     * locking query.
     */
    protected void setIsPrePrepared(boolean isPrePrepared) {
        // Only unprepare if was prepared to begin with, prevent unpreparing during prepare.
        if (this.isPrePrepared && !isPrePrepared) {
            setIsPrepared(false);
            if (hasJoining()) {
                getJoinedAttributeManager().reset();
            }
        }
        this.isPrePrepared = isPrePrepared;
    }
    
    /**
     * INTERNAL:
     * Clear cached flags when un-preparing.
     */
    public void setIsPrepared(boolean isPrepared) {
        super.setIsPrepared(isPrepared);
        this.isReferenceClassLocked = null;
        this.concreteSubclassCalls = null;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() throws QueryException {
        super.prepare();
        prepareQuery();
        if (hasJoining()) {
            getJoinedAttributeManager().computeJoiningMappingQueries(session);
        }
    }
    
    /**
     * INTERNAL:
     * Check if the query is cached and prepare from it.
     * Return true if the query was cached.
     */
    protected boolean prepareFromCachedQuery() {
        // PERF: Check if the equivalent expression query has already been prepared.
        // Only allow queries with default properties to be cached.
        boolean isCacheable = isExpressionQuery() && (!getQueryMechanism().isEJBQLCallQueryMechanism()) && isDefaultPropertiesQuery() && (!getSession().isHistoricalSession());
        DatabaseQuery cachedQuery = null;
        if (isCacheable) {
            cachedQuery = getDescriptor().getQueryManager().getCachedExpressionQuery(this);
        } else {
            return false;
        }
        if ((cachedQuery != null) && cachedQuery.isPrepared()) {
            prepareFromQuery(cachedQuery);
            setIsPrepared(true);
            return true;
        }
        getDescriptor().getQueryManager().putCachedExpressionQuery(this);
        return false;
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
        if (query.isObjectLevelReadQuery()) {
            ObjectLevelReadQuery readQuery = (ObjectLevelReadQuery)query;
            this.cacheUsage = readQuery.cacheUsage;
            this.isReadOnly = readQuery.isReadOnly;
            this.isResultSetOptimizedQuery = readQuery.isResultSetOptimizedQuery;
            this.shouldIncludeData = readQuery.shouldIncludeData;
            this.inMemoryQueryIndirectionPolicy = readQuery.inMemoryQueryIndirectionPolicy;
        }
    }
    
    /**
     * INTERNAL:
     * Prepare the query from the prepared query.
     * This allows a dynamic query to prepare itself directly from a prepared query instance.
     * This is used in the EJBQL parse cache to allow preparsed queries to be used to prepare
     * dyanmic queries.
     * This only copies over properties that are configured through EJBQL.
     */
    public void prepareFromQuery(DatabaseQuery query) {
        super.prepareFromQuery(query);
        if (query.isObjectLevelReadQuery()) {
            ObjectLevelReadQuery objectQuery = (ObjectLevelReadQuery)query;
            this.referenceClass = objectQuery.referenceClass;
            this.distinctState = objectQuery.distinctState;
            if (objectQuery.hasJoining()) {
                JoinedAttributeManager thisManager = getJoinedAttributeManager();
                JoinedAttributeManager queryManager = objectQuery.getJoinedAttributeManager();
                thisManager.setJoinedAttributeExpressions_(queryManager.getJoinedAttributeExpressions());
                thisManager.setJoinedMappingExpressions_(queryManager.getJoinedMappingExpressions());
                thisManager.setJoinedMappingIndexes_((HashMap)queryManager.getJoinedMappingIndexes_());
                thisManager.setJoinedMappingQueries_((HashMap)queryManager.getJoinedMappingQueries_());
            }
            this.nonFetchJoinAttributeExpressions = objectQuery.nonFetchJoinAttributeExpressions;
            this.defaultBuilder = objectQuery.defaultBuilder;
            this.fetchGroup = objectQuery.fetchGroup;
            this.fetchGroupName = objectQuery.fetchGroupName;
            this.isReferenceClassLocked = objectQuery.isReferenceClassLocked;
            this.shouldOuterJoinSubclasses = objectQuery.shouldOuterJoinSubclasses;
            this.shouldUseDefaultFetchGroup = objectQuery.shouldUseDefaultFetchGroup;
            this.concreteSubclassCalls = objectQuery.concreteSubclassCalls;
            this.additionalFields = objectQuery.additionalFields;
            this.partialAttributeExpressions = objectQuery.partialAttributeExpressions;
        }
    }
    
    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prePrepare() throws QueryException {
        // For bug 3136413/2610803 building the selection criteria from an EJBQL string or
        // an example object is done just in time.
        buildSelectionCriteria(session);
        checkDescriptor(session);

        // Add mapping joined attributes.
        if (getQueryMechanism().isExpressionQueryMechanism() && getDescriptor().getObjectBuilder().hasJoinedAttributes()) {
            getJoinedAttributeManager().processJoinedMappings();
        }

        // modify query for locking only if locking has not been configured
        if (isDefaultLock()) {
            setWasDefaultLockMode(true);
            ForUpdateOfClause lockingClause = null;
            if (hasJoining()) {
                lockingClause = getJoinedAttributeManager().setupLockingClauseForJoinedExpressions(lockingClause, getSession());
            }
            if (descriptor.hasPessimisticLockingPolicy()) {
                lockingClause = new ForUpdateOfClause();
                lockingClause.setLockMode(descriptor.getCMPPolicy().getPessimisticLockingPolicy().getLockingMode());
                lockingClause.addLockedExpression(getExpressionBuilder());
            }
            if (lockingClause == null) {
                this.lockingClause = ForUpdateClause.newInstance(NO_LOCK);
            } else {
                this.lockingClause = lockingClause;
                // SPECJ: Locking not compatible with distinct for batch reading.
                dontUseDistinct();
            }
        } else if ((getLockMode() == NO_LOCK) && (!descriptor.hasPessimisticLockingPolicy())) {
            setWasDefaultLockMode(true);            
        }
        if(hasJoining() && hasPartialAttributeExpressions()) {
            session.log(SessionLog.WARNING, SessionLog.QUERY, "query_has_both_join_attributes_and_partial_attributes", new Object[]{this, this.getName()});
        }
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepareQuery() throws QueryException {
        if ((!shouldMaintainCache()) && shouldRefreshIdentityMapResult() && (!descriptor.isAggregateCollectionDescriptor())) {
            throw QueryException.refreshNotPossibleWithoutCache(this);
        }
        if (shouldMaintainCache() && hasPartialAttributeExpressions()) {
            throw QueryException.cannotCachePartialObjects(this);
        }

        if (descriptor.isAggregateDescriptor()) {
            // Not allowed
            throw QueryException.aggregateObjectCannotBeDeletedOrWritten(descriptor, this);
        }

        if (hasAsOfClause() && (getSession().getAsOfClause() == null)) {
            if (shouldMaintainCache()) {
                throw QueryException.historicalQueriesMustPreserveGlobalCache();
            } else if (!getSession().getPlatform().isOracle() && !getSession().getProject().hasGenericHistorySupport()) {
                throw QueryException.historicalQueriesOnlySupportedOnOracle();
            }
        }

        // If fetch group manager is not set in the descriptor and the user attempts to use fetch group in the query dynamiclly, throw exception here.
        if ((!getDescriptor().hasFetchGroupManager()) && ((getFetchGroup() != null) || (getFetchGroupName() != null))) {
            throw QueryException.fetchGroupValidOnlyIfFetchGroupManagerInDescriptor(getDescriptor().getJavaClassName(), getName());
        }

        // Prepare fetch group if applied.
        if (getDescriptor().hasFetchGroupManager()) {
            getDescriptor().getFetchGroupManager().prepareQueryWithFetchGroup(this);
        }

        // Validate and prepare join expressions.			
        if (hasJoining()) {
            getJoinedAttributeManager().prepareJoinExpressions(getSession());
        }  

        // Validate and prepare partial attribute expressions.
        if (hasPartialAttributeExpressions()) {
            for (int index = 0; index < getPartialAttributeExpressions().size(); index++) {
                Expression expression = (Expression)getPartialAttributeExpressions().get(index);

                // Search if any of the expression traverse a 1-m.
                while (expression.isQueryKeyExpression() && (!expression.isExpressionBuilder())) {
                    if (((QueryKeyExpression)expression).shouldQueryToManyRelationship()) {
                    	getJoinedAttributeManager().setIsToManyJoinQuery(true);
                    }
                    expression = ((QueryKeyExpression)expression).getBaseExpression();
                }
            }
        }
        
        // Ensure the subclass call cache is initialized if a multiple table inheritance descriptor.
        // This must be initialized in the query before it is cloned, and never cloned.
        if ((!shouldOuterJoinSubclasses()) && getDescriptor().hasInheritance() && getDescriptor().getInheritancePolicy().requiresMultipleTableSubclassRead()) {
            getConcreteSubclassCalls();
        }
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepareForRemoteExecution() throws QueryException {
        super.prepareForRemoteExecution();
        checkPrePrepare(getSession());
        prepareQuery();
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object(s) resulting from the query.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void refreshIdentityMapResult() {
        setShouldRefreshIdentityMapResult(true);
    }

    /**
     * PUBLIC:
     * Refresh the attributes of the object(s) resulting from the query.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void refreshRemoteIdentityMapResult() {
        setShouldRefreshRemoteIdentityMapResult(true);
    }
    
    /**
     * INTERNAL:
     * All objects queried via a UnitOfWork get registered here.  If the query
     * went to the database.
     * <p>
     * Involves registering the query result individually and in totality, and
     * hence refreshing / conforming is done here.
     *
     * @param result may be collection (read all) or an object (read one),
     * or even a cursor.  If in transaction the shared cache will
     * be bypassed, meaning the result may not be originals from the parent
     * but raw database rows.
     * @param unitOfWork the unitOfWork the result is being registered in.
     * @param arguments the original arguments/parameters passed to the query
     * execution.  Used by conforming
     * @param buildDirectlyFromRows If in transaction must construct
     * a registered result from raw database rows.
     *
     * @return the final (conformed, refreshed, wrapped) UnitOfWork query result
     */
    public abstract Object registerResultInUnitOfWork(Object result, UnitOfWorkImpl unitOfWork, AbstractRecord arguments, boolean buildDirectlyFromRows);

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void resetDistinct() {
        setDistinctState(UNCOMPUTED_DISTINCT);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Additional fields can be added to a query.  This is used in m-m bacth reading to bring back the key from the join table.
     */
    public void setAdditionalFields(Vector additionalFields) {
        this.additionalFields = additionalFields;
    }

    /**
     * PUBLIC:
     * Set the cache usage.
     * By default only primary key read object queries will first check the cache before accessing the database.
     * Any query can be configure to query against the cache completely, by key or ignore the cache check.
     * <p>Valid values are:
     * <ul>
     * <li> DoNotCheckCache - The query does not check the cache but accesses the database, the cache will still be maintain.
     * <li> CheckCacheByExactPrimaryKey - If the query is exactly and only on the object's primary key the cache will be checked.
     * <li> CheckCacheByPrimaryKey - If the query contains the primary key and possible other values the cache will be checked.
     * <li> CheckCacheThenDatabase - The whole cache will be checked to see if there is any object matching the query, if not the database will be accessed.
     * <li> CheckCacheOnly - The whole cache will be checked to see if there is any object matching the query, if not null or an empty collection is returned.
     * <li> ConformResultsAgainstUnitOfWork - The results will be checked againtst the changes within the unit of work and object no longer matching or deleted will be remove, matching new objects will also be added.
     * <li> shouldCheckDescriptorForCacheUsage - This setting functions like CheckCacheByPrimaryKey, except checks the appropriate descriptor's
     * shouldDisableCacheHits setting when querying on the cache.
      * </ul>
     */
    public void setCacheUsage(int cacheUsage) {
        this.cacheUsage = cacheUsage;
    }

    /**
     * INTERNAL:
     * Set the descriptor for the query.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        super.setDescriptor(descriptor);
        if (this.joinedAttributeManager != null){
            this.joinedAttributeManager.setDescriptor(descriptor);
        }
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void setDistinctState(short distinctState) {
        this.distinctState = distinctState;
    }

    /**
     * INTERNAL:
     * Set the the time this query went to the database.
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * PUBLIC:
     * Set the example object of the query to be the newExampleObject.
     * The example object is used for Query By Example.
     * When doing a Query By Example, an instance of the desired object is created, and the fields are filled with
     * the values that are required in the result set.  From these values the corresponding expression is built
     * by TopLink, and the query is executed, returning the set of results.
     * <p>If a query already has a selection criteria this criteria and the generated
     * query by example criteria will be conjuncted.
     * <p>Once a query is executed you must make an explicit call to setExampleObject
     * if the example object is changed, so the query will know to prepare itself again.
     * <p>There is a caution to setting both a selection criteria and an example object:
     * Only in this case if you set the example object again after execution you must then also reset the selection criteria.
     * (This is because after execution the original criteria and Query By Example criteria were fused together,
     * and the former cannot be easily recovered from the now invalid result).
     */
    public void setExampleObject(Object newExampleObject) {
        if (!getQueryMechanism().isQueryByExampleMechanism()) {
            setQueryMechanism(new QueryByExampleMechanism(this, getSelectionCriteria()));
            ((QueryByExampleMechanism)getQueryMechanism()).setExampleObject(newExampleObject);
            setIsPrepared(false);
        } else {
            ((QueryByExampleMechanism)getQueryMechanism()).setExampleObject(newExampleObject);

            // Potentially force the query to be prepared again.  If shouldPrepare() is false
            // must use a slightly inferior check.
            if (isPrepared() || (!shouldPrepare() && ((QueryByExampleMechanism)getQueryMechanism()).isParsed())) {
                ((QueryByExampleMechanism)getQueryMechanism()).setIsParsed(false);
                setSelectionCriteria(null);
            }
        }
        if (newExampleObject != null) {
            setReferenceClass(newExampleObject.getClass());
        }
    }

    /**
     * PUBLIC:
     * Set the InMemoryQueryIndirectionPolicy for this query.
     */
    public void setInMemoryQueryIndirectionPolicy(InMemoryQueryIndirectionPolicy inMemoryQueryIndirectionPolicy) {
        // Bug2862302 Backwards compatibility.  This makes sure 9.0.3 and any older version project xml don't break.
        if (inMemoryQueryIndirectionPolicy != null) {
            this.inMemoryQueryIndirectionPolicy = inMemoryQueryIndirectionPolicy.getPolicy();
            inMemoryQueryIndirectionPolicy.setQuery(this);
        }
    }
    
    /**
     * PUBLIC:
     * Set the InMemoryQueryIndirectionPolicy for this query.
     */
    public void setInMemoryQueryIndirectionPolicyState(int inMemoryQueryIndirectionPolicy) {
        this.inMemoryQueryIndirectionPolicy = inMemoryQueryIndirectionPolicy;
    }

    /**
     * PUBLIC:
     * Sets whether this is a pessimistically locking query.
     * <ul>
     * <li>ObjectBuildingQuery.LOCK: SELECT .... FOR UPDATE issued.
     * <li>ObjectBuildingQuery.LOCK_NOWAIT: SELECT .... FOR UPDATE NO WAIT issued.
     * <li>ObjectBuildingQuery.NO_LOCK: no pessimistic locking.
     * <li>ObjectBuildingQuery.DEFAULT_LOCK_MODE (default) and you have a CMP descriptor:
     * fine grained locking will occur.
     * </ul>
     * <p>Fine Grained Locking: On execution the reference class
     * and those of all joined attributes will be checked.  If any of these have a
     * PessimisticLockingPolicy set on their descriptor, they will be locked in a
     * SELECT ... FOR UPDATE OF ... {NO WAIT}.  Issues fewer locks
     * and avoids setting the lock mode on each query.
     * <p>Example:<code>readAllQuery.setSelectionCriteria(employee.get("address").equal("Ottawa"));</code>
     * <ul><li>LOCK: all employees in Ottawa and all referenced Ottawa addresses will be locked.
     * <li>DEFAULT_LOCK_MODE: if address is a joined attribute, and only address has a pessimistic
     * locking policy, only referenced Ottawa addresses will be locked.
     * </ul>
     * @see oracle.toplink.descriptors.PessimisticLockingPolicy
     */
    public void setLockMode(short lockMode) {
        if ((lockMode == LOCK) || (lockMode == LOCK_NOWAIT)) {
            lockingClause = ForUpdateClause.newInstance(lockMode);
            setShouldRefreshIdentityMapResult(true);
        } else if (lockMode == NO_LOCK) {
            lockingClause = ForUpdateClause.newInstance(lockMode);
        } else {
            lockingClause = null;
            setIsPrePrepared(false);
        }
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Return the attributes that must be joined, but not fetched, that is,
     * do not trigger the value holder.
     */
    protected void setNonFetchJoinAttributeExpressions(Vector nonFetchJoinExpressions) {
        this.nonFetchJoinAttributeExpressions = nonFetchJoinExpressions;
    }

    /**
     * INTERNAL:
     * The locking clause contains a list of expressions representing which
     * objects are to be locked by the query.
     * <p>
     * Use for even finer grained control over what is and is not locked by
     * a particular query.
     */
    public void setLockingClause(ForUpdateClause clause) {
        if (clause.isForUpdateOfClause()) {
            this.lockingClause = clause;
            setIsPrePrepared(false);
        } else {
            setLockMode(clause.getLockMode());
        }
    }

    /**
     * INTERNAL:
     * Set the partial attributes to select.
     */
    public void setPartialAttributeExpressions(Vector partialAttributeExpressions) {
        this.partialAttributeExpressions = partialAttributeExpressions;
    }

    public void setEJBQLString(String ejbqlString) {
        super.setEJBQLString(ejbqlString);
        setIsPrePrepared(false);
    }

    /**
     * PUBLIC:
     * The QueryByExamplePolicy, is a useful to customize the query when Query By Example is used.
     * The policy will control what attributes should, or should not be included in the query.
     * When dealing with nulls, using specail operations (notEqual, lessThan, like, etc.)
     * for comparison, or chosing to include certain attributes at all times, it is useful to modify
     * the policy accordingly.
     * <p>Once a query is executed you must make an explicit call to setQueryByExamplePolicy
     * when changing the policy, so the query will know to prepare itself again.
     * <p>There is a caution to setting both a selection criteria and an example object:
     * If you set the policy after execution you must also reset the selection criteria.
     * (This is because after execution the original criteria and Query By Example criteria are fused together,
     * and the former cannot be easily recovered).
     */
    public void setQueryByExamplePolicy(QueryByExamplePolicy queryByExamplePolicy) {
        if (!getQueryMechanism().isQueryByExampleMechanism()) {
            setQueryMechanism(new QueryByExampleMechanism(this, getSelectionCriteria()));
            ((QueryByExampleMechanism)getQueryMechanism()).setQueryByExamplePolicy(queryByExamplePolicy);
            setIsPrepared(false);
        } else {
            ((QueryByExampleMechanism)getQueryMechanism()).setQueryByExamplePolicy(queryByExamplePolicy);
            // Must allow the query to be prepared again.
            if (isPrepared() || (!shouldPrepare() && ((QueryByExampleMechanism)getQueryMechanism()).isParsed())) {
                ((QueryByExampleMechanism)getQueryMechanism()).setIsParsed(false);
                setSelectionCriteria(null);
                // setIsPrepared(false) triggered by previous.
            }
        }
        setIsPrePrepared(false);
    }

    /**
     * REQUIRED:
     * Set the reference class for the query.
     */
    public void setReferenceClass(Class aClass) {
        if (referenceClass != aClass) {
            setIsPrepared(false);
        }
        referenceClass = aClass;
    }

    /**
     * INTERNAL:
     * Set the reference class for the query.
     */
    public void setReferenceClassName(String aClass) {
        if (referenceClassName != aClass) {
            setIsPrepared(false);
        }
        referenceClassName = aClass;
    }

    /**
     * INTERNAL:
     * Used by TopLink to set the expression from XML.  Will convert from
     * oracle.toplink.tools.workbench.expressions.* types to Runtime Expressions.
     * Please note that this feature should be revisted for future releases.
     */
    public void setSelectionCriteriaFromPersistence(oracle.toplink.tools.workbench.expressions.ExpressionRepresentation representation) {
        Expression expression = null;
        if (representation != null) {
            expression = representation.convertToRuntime(getExpressionBuilder());
            // only set the expression if it exists, otherwise we may be overwriting SQL strings
            setSelectionCriteria(expression);
        }
    }

    public void setSelectionCriteria(Expression expression) {
        super.setSelectionCriteria(expression);
        if ((expression != null) && (defaultBuilder != null) && (defaultBuilder.getQueryClass() == null)){
            // For flashback: Must make sure expression and defaultBuilder always in sync.
            ExpressionBuilder newBuilder = expression.getBuilder();
            if (newBuilder != defaultBuilder) {
                if (hasAsOfClause() && getAsOfClause().isUniversal()) {
                    newBuilder.asOf(defaultBuilder.getAsOfClause());
                }
                defaultBuilder = newBuilder;
            }
        }
    }

    /**
     * INTERNAL:
     * Set if the rows for the result of the query should also be returned using a complex query result.
     * @see ComplexQueryResult
     */
    public void setShouldIncludeData(boolean shouldIncludeData) {
        this.shouldIncludeData = shouldIncludeData;
    }

    /**
     * PUBLIC:
     * Set if the attributes of the object(s) resulting from the query should be refreshed.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void setShouldRefreshIdentityMapResult(boolean shouldRefreshIdentityMapResult) {
        this.shouldRefreshIdentityMapResult = shouldRefreshIdentityMapResult;
        if (shouldRefreshIdentityMapResult) {
            setShouldRefreshRemoteIdentityMapResult(true);
        }
    }

    /**
     * PUBLIC:
     * Set if the attributes of the object(s) resulting from the query should be refreshed.
     * If cascading is used the private parts of the objects will also be refreshed.
     */
    public void setShouldRefreshRemoteIdentityMapResult(boolean shouldRefreshIdentityMapResult) {
        this.shouldRefreshRemoteIdentityMapResult = shouldRefreshIdentityMapResult;
    }

    /**
     * INTERNAL:
     * Set to false to have queries conform to a UnitOfWork without registering
     * any additional objects not already in that UnitOfWork.
     * @see #shouldRegisterResultsInUnitOfWork
     * @bug 2612601
     */
    public void setShouldRegisterResultsInUnitOfWork(boolean shouldRegisterResultsInUnitOfWork) {
        this.shouldRegisterResultsInUnitOfWork = shouldRegisterResultsInUnitOfWork;
    }

    /**
     * PUBLIC:
     * Return if cache should be checked.
     */
    public boolean shouldCheckCacheOnly() {
        return getCacheUsage() == CheckCacheOnly;
    }

    /**
     * PUBLIC:
     * Return whether the descriptor's disableCacheHits setting should be checked prior
     * to querying the cache.
     */
    public boolean shouldCheckDescriptorForCacheUsage() {
        return getCacheUsage() == UseDescriptorSetting;
    }

    /**
     * PUBLIC:
     * Should the results will be checked against the changes within the unit of work and object no longer matching or deleted will be remove, matching new objects will also be added..
     */
    public boolean shouldConformResultsInUnitOfWork() {
        return getCacheUsage() == ConformResultsInUnitOfWork;
    }

    /**
     * INTERNAL:
     * return true if this query should use a distinct
     */
    public boolean shouldDistinctBeUsed() {
        return getDistinctState() == USE_DISTINCT;
    }

    /**
     * INTERNAL:
     * Return if the rows for the result of the query should also be returned using a complex query result.
     * @see ComplexQueryResult
     */
    public boolean shouldIncludeData() {
        return shouldIncludeData;
    }
        
    /**
     * PUBLIC:
     * Return if an outer join should be used to read subclasses.
     * By default a seperate query is done for each subclass when querying for
     * a root or branch inheritance class that has subclasses that span multiple tables.
     */
    public boolean shouldOuterJoinSubclasses() {
        if (shouldOuterJoinSubclasses == null) {
            return false;
        }
        return shouldOuterJoinSubclasses.booleanValue();
    }
            
    /**
     * PUBLIC:
     * Set if an outer join should be used to read subclasses.
     * By default a seperate query is done for each subclass when querying for
     * a root or branch inheritance class that has subclasses that span multiple tables.
     */
    public void setShouldOuterJoinSubclasses(boolean shouldOuterJoinSubclasses) {
        this.shouldOuterJoinSubclasses = Boolean.valueOf(shouldOuterJoinSubclasses);
    }
    
    /**
     * INTERNAL:
     * Allows one to do conforming in a UnitOfWork without registering.
     * Queries executed on a UnitOfWork will only return working copies for objects
     * that have already been registered.
     * <p>Extreme care should be taken in using this feature, for a user will
     * get back a mix of registered and original (unregistered) objects.
     * <p>Best used with a WrapperPolicy where invoking on an object will trigger
     * its registration (CMP).  Without a WrapperPolicy {@link oracle.toplink.sessions.UnitOfWork#registerExistingObject registerExistingObject}
     * should be called on any object that you intend to change.
     * @return true by default.
     * @see #setShouldRegisterResultsInUnitOfWork(boolean)
     * @see oracle.toplink.descriptors.ClassDescriptor#shouldRegisterResultsInUnitOfWork()
     * @bug 2612601
     */
    public boolean shouldRegisterResultsInUnitOfWork() {
        return shouldRegisterResultsInUnitOfWork;
    }

    /**
     * INTERNAL:
     * Return if this is a full object query, not partial nor fetch group.
     */
    public boolean shouldReadAllMappings() {
        return (!hasPartialAttributeExpressions()) && (!hasFetchGroup());
    }
    
    /**
     * INTERNAL:
     * Check if the mapping is part of the partial attributes.
     */
    public boolean shouldReadMapping(DatabaseMapping mapping) {
        if ((!hasPartialAttributeExpressions()) && (!hasFetchGroup())) {
            return true;
        }

        String attrName = mapping.getAttributeName();

        //bug 3659145
        if (hasFetchGroup()) {
            return isFetchGroupAttribute(attrName);
        }

        return isPartialAttribute(attrName);
    }

    /**
     * PUBLIC:
     * Set to a boolean. When set means refresh the instance
     * variables of referenceObject from the database.
     */
    public boolean shouldRefreshIdentityMapResult() {
        return shouldRefreshIdentityMapResult;
    }

    /**
     * PUBLIC:
     * Set to a boolean. When set means refresh the instance
     * variables of referenceObject from the database.
     */
    public boolean shouldRefreshRemoteIdentityMapResult() {
        return shouldRefreshRemoteIdentityMapResult;
    }

    public String toString() {
        if (getReferenceClass() == null) {
            return super.toString();
        }
        return Helper.getShortClassName(getClass()) + "(" + getReferenceClass().getName() + ")";
    }

    /**
     * ADVANCED:
     * Used for CMP only.  This allows users to indicate whether cmp finders executed
     * at the beginning of a transaction should always be run against a UnitOfWork.
     * Defaults to true.
     * <p>
     * If set to false, then UnitOfWork allocation will be deferred until a business
     * method (including creates/removes) or finder with shouldProcessResultsInUnitOfWork == true
     * is invoked.  Any finder executed before such a time, will do so against the
     * underlying ServerSession.  Forcing finder execution to always go through a
     * UnitOfWork means the results will be cloned and cached in the UnitOfWork up
     * front.  This is desired when the results will be accessed in the same transaction.
     * <p>
     * Note that finders executed with an unspecified transaction context will never
     * be executed against a UnitOfWork, even if this setting is true.  This case may happen
     * with the NotSupported, Never, and Supports attributes.
     */
    public void setShouldProcessResultsInUnitOfWork(boolean processResultsInUnitOfWork) {
        this.shouldProcessResultsInUnitOfWork = processResultsInUnitOfWork;
    }

    /**
     * ADVANCED:
     * Used for CMP only.  Indicates whether cmp finders executed at the beginning
     * of a transaction should always be run against a UnitOfWork.
     * Defaults to true.
     * <p>
     * If set to false, then UnitOfWork allocation will be deferred until a business
     * method (including creates/removes) or finder with shouldProcessResultsInUnitOfWork == true
     * is invoked.  Any finder executed before such a time, will do so against the
     * underlying ServerSession.  Forcing finder execution to always go through a
     * UnitOfWork means the results will be cloned and cached in the UnitOfWork up
     * front.  This is desired when the results will be accessed in the same transaction.
     * <p>
     * Note that finders executed with an unspecified transaction context will never
     * be executed against a UnitOfWork, even if this setting is true.  This case may happen
     * with the NotSupported, Never, and Supports attributes.
     */
    public boolean shouldProcessResultsInUnitOfWork() {
        return this.shouldProcessResultsInUnitOfWork;
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void useDistinct() {
        setDistinctState(USE_DISTINCT);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
    * INTERNAL:
    * Helper method that checks if clone has been locked with uow.
    */
    public boolean isClonePessimisticLocked(Object clone, UnitOfWorkImpl uow) {
        return getDescriptor().hasPessimisticLockingPolicy() && uow.isPessimisticLocked(clone);
    }

    /**
     * INTERNAL:
     * Cache the locking policy isReferenceClassLocked check.
     */
    protected boolean isReferenceClassLocked() {
        if (isReferenceClassLocked == null) {
            isReferenceClassLocked = Boolean.valueOf(isLockQuery() && lockingClause.isReferenceClassLocked());
        }
        return isReferenceClassLocked.booleanValue();
    }
    
    /**
     * INTERNAL:
     * Helper method that records clone with uow if query is pessimistic locking.
     */
    public void recordCloneForPessimisticLocking(Object clone, UnitOfWorkImpl uow) {
        if (isReferenceClassLocked()) {
            uow.addPessimisticLockedClone(clone);
        }
    }

    /**
     * ADVANCED:
     * Return if the query should be optimized to build directly from the result set.
     * This optimization follows an optimized path and can only be used for,
     * singleton primary key, direct mapped, simple type, no inheritance, uow isolated objects.
     */
    public boolean isResultSetOptimizedQuery() {
        return isResultSetOptimizedQuery;
    }
    
    /**
     * ADVANCED:
     * Set if the query should be optimized to build directly from the result set.
     * This optimization follows an optimized path and can only be used for,
     * singleton primary key, direct mapped, simple type, no inheritance, uow isolated objects.
     */
    public void setIsResultSetOptimizedQuery(boolean isResultSetOptimizedQuery) {
        this.isResultSetOptimizedQuery = isResultSetOptimizedQuery;
    }
    
    /**
     * INTERNAL: Helper method to determine the default mode. If true and quey has a pessimistic locking policy,
     * locking will be configured according to the pessimistic locking policy.
     */
    public boolean isDefaultLock() {
        return (lockingClause == null) || wasDefaultLockMode();
    }
    
    /**
     * INTERNAL:
     * Return true if the query uses default properties.
     * This is used to determine if this query is cacheable.
     * i.e. does not use any properties that may conflict with another query
     * with the same JPQL or selection criteria.
     */
    public boolean isDefaultPropertiesQuery() {
        return super.isDefaultPropertiesQuery()
            && (isDefaultLock())
            && (!isDistinctComputed())
            && (!hasAdditionalFields())
            && (!hasPartialAttributeExpressions())
            && (!hasNonFetchJoinedAttributeExpressions())
            && (!hasFetchGroup())
            && (getFetchGroupName() == null)
            && (shouldUseDefaultFetchGroup());
    }

    /**
     * Return if a fetch group is set in the query.
     */
    public boolean hasFetchGroup() {
        return fetchGroup != null;
    }
    
    /**
     * Return the fetch group set in the query.
     * If a fetch group is not explicitly set in the query, default fetch group optionally defined in the decsiptor
     * would be used, unless the user explicitly calls query.setShouldUseDefaultFetchGroup(false).
     */
    public FetchGroup getFetchGroup() {
        return fetchGroup;
    }

    /**
     * INTERNAL:
     * Initialize fetch group
     */
    public void initializeFetchGroup() {
        if (fetchGroup != null) {
            //fetch group already set.
            return;
        }

        //not explicitly set dynamically fetch group
        if (fetchGroupName != null) {//set pre-defined named group
            fetchGroup = getDescriptor().getFetchGroupManager().getFetchGroup(fetchGroupName);
            if (fetchGroup == null) {
                //named fetch group is not defined in the descriptor
                throw QueryException.fetchGroupNotDefinedInDescriptor(fetchGroupName);
            }
        } else {//not set fecth group at all
            //use the default fetch group if not explicitly turned off
            if (shouldUseDefaultFetchGroup()) {
                fetchGroup = getDescriptor().getDefaultFetchGroup();
            }
        }
    }

    /**
     * Set a dynamic (use case) fetch group to the query.
     */
    public void setFetchGroup(FetchGroup newFetchGroup) {
        fetchGroup = newFetchGroup;
    }

    /**
     * Set a descriptor-level pre-defined named fetch group  to the query.
     */
    public void setFetchGroupName(String groupName) {
        //nullify the fecth group refernce as one query can only has one fetch group.
        fetchGroup = null;
        fetchGroupName = groupName;
    }

    /**
     * Return the fetch group name set in the query.
     */
    public String getFetchGroupName() {
        return fetchGroupName;
    }

    /**
     * Return false if the query does not use the default fetch group defined in the descriptor level.
     */
    public boolean shouldUseDefaultFetchGroup() {
        return shouldUseDefaultFetchGroup;
    }

    /**
     * Set false if the user does not want to use the default fetch group defined in the descriptor level.
     */
    public void setShouldUseDefaultFetchGroup(boolean shouldUseDefaultFetchGroup) {
        this.shouldUseDefaultFetchGroup = shouldUseDefaultFetchGroup;
    }

    /**
     * INTERNAL:
     * Return if fetch group attribute.
     */
    public boolean isFetchGroupAttribute(String attributeName) {
        if (getFetchGroup() == null) {
            //every attribute is fetched already
            return true;
        }
        return getFetchGroup().getAttributes().contains(attributeName);
    }
    
    /**
     * INTERNAL:
     * Return the cache of concrete subclass calls.
     * This allow concrete subclasses calls to be prepared and cached for inheritance queries.
     */
    public Map<Class, DatabaseCall> getConcreteSubclassCalls() {
        if (concreteSubclassCalls == null) {
            concreteSubclassCalls = new ConcurrentHashMap(8);
        }
        return concreteSubclassCalls;
    }
}
