// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import oracle.toplink.queryframework.*;

/**
 * <p><b>Purpose</b>: This interface defines the link between the Session and the PerformanceProfiler tool.
 * It is provide to decouple the session from tools and to allow other profilers to register with the session.
 *
 * @author James Sutherland
 */
public interface SessionProfiler {
    //dms sensor weight constants
    public static final int NONE = 0;
    public static final int NORMAL = 5;
    public static final int HEAVY = 10;
    public static final int ALL = Integer.MAX_VALUE;

    //nouns type display name 
    public static final String TopLinkRootNoun = "/TopLink";
    public static final String SessionNounType = "TopLink Session";
    public static final String TransactionNounType = "TopLink Transaction";
    public static final String QueryNounType = "TopLink Queries";
    public static final String RcmNounType = "TopLink RCM";
    public static final String ConnectionNounType = "TopLink Connections";
    public static final String CacheNounType = "TopLink Cache";
    public static final String MiscellaneousNounType = "TopLink Miscellaneous";

    //dms sensors display name
    public static final String SessionName = "SessionName";
    public static final String LoginTime = "loginTime";
    public static final String ClientSessionCreated = "ClientSession";
    public static final String UowCreated = "UnitOfWork";
    public static final String UowCommit = "UnitOfWorkCommits";
    public static final String UowRollbacks = "UnitOfWorkRollbacks";
    public static final String OptimisticLockException = "OptimisticLocks";
    public static final String RcmStatus = "RCMStatus";
    public static final String RcmReceived = "MessagesReceived";
    public static final String RcmSent = "MessagesSent";
    public static final String RemoteChangeSet = "RemoteChangeSets";
    public static final String TlConnects = "ConnectCalls";
    public static final String TlDisconnects = "DisconnectCalls";
    public static final String CachedObjects = "CachedObjects";
    public static final String CacheHits = "CacheHits";
    public static final String CacheMisses = "CacheMisses";
    public static final String ChangeSetsProcessed = "ChangesProcessed";
    public static final String ChangeSetsNotProcessed = "ChangesNotProcessed";
    public static final String DescriptorEvent = "DescriptorEvents";
    public static final String SessionEvent = "SessionEvents";
    public static final String ConnectionInUse = "ConnectionsInUse";
    public static final String QueryPreparation = "QueryPreparation";
    public static final String SqlGeneration = "SqlGeneration";
    public static final String DatabaseExecute = "DatabaseExecute";
    public static final String SqlPrepare = "SqlPrepare";
    public static final String RowFetch = "RowFetch";
    public static final String ObjectBuilding = "ObjectBuilding";
    public static final String MergeTime = "MergeTime";
    public static final String UnitOfWorkRegister = "UnitOfWorkRegister";
    public static final String DistributedMergeDmsDisplayName = "DistributedMerge";
    public static final String Sequencing = "Sequencing";
    public static final String Caching = "Caching";
    public static final String ConnectionManagement = "ConnectionManagement";
    public static final String LoggingDMSDisPlayName = "Logging";
    public static final String JtsBeforeCompletion = "TXBeforeCompletion";
    public static final String JtsAfterCompletion = "TXAfterCompletion";
    public static final String ConnectionPing = "ConnectionHealthTest";

    //Token used by existed default performance profiler 
    public static final String Register = "register";
    public static final String Merge = "merge";
    public static final String AssignSequence = "assign sequence";
    public static final String DistributedMerge = "distributed merge";
    public static final String DeletedObject = "deleted object";
    public static final String Wrapping = "wrapping";
    public static final String Logging = "logging";
    public static final String OBJECT_BUILDING = "object building";
    public static final String SQL_GENERATION = "sql generation";
    public static final String QUERY_PREPARE = "query prepare";
    public static final String STATEMENT_EXECUTE = "sql execute";
    public static final String ROW_FETCH = "row fetch";
    public static final String SQL_PREPARE = "sql prepare";
    public static final String TRANSACTION = "transactions";
    public static final String CONNECT = "connect";
    public static final String CACHE = "cache";

    /**
     * INTERNAL:
     * End the operation timing.
     */
    public void endOperationProfile(String operationName);

    /**
     * INTERNAL:
     * End the operation timing.
     */
    public void endOperationProfile(String operationName, DatabaseQuery query, int weight);

    /**
     * INTERNAL:
     * Finish a profile operation if profiling.
     * This assumes the start operation preceeds on the stack.
     * The session must be passed to allow units of work etc. to share their parents profiler.
     *
     * @return the execution result of the query.
     */
    public Object profileExecutionOfQuery(DatabaseQuery query, Record row, oracle.toplink.internal.sessions.AbstractSession session);

    /**
     * INTERNAL:
     * Set the sesssion.
     */
    public void setSession(Session session);

    /**
     * INTERNAL:
     * Start the operation timing.
     */
    public void startOperationProfile(String operationName);

    /**
     * INTERNAL:
     * Start the operation timing.
     */
    public void startOperationProfile(String operationName, DatabaseQuery query, int weight);

    /**
     * INTERNAL:
     * Update the value of the State sensor.(DMS)
     */
    public void update(String operationName, Object value);

    /**
     * INTERNAL:
     * Increase DMS Event sensor occurrence.(DMS)
     */
    public void occurred(String operationName);

    /**
     * INTERNAL:
     * Set DMS sensor weight(DMS)
     */
    public void setProfileWeight(int weight);

    /**
     * INTERNAL:
     * Return DMS sensor weight(DMS)
     */
    public int getProfileWeight();

    /**
     * INTERNAL:
     * Initialize TopLink noun tree(DMS)
     */
    public void initialize();
}