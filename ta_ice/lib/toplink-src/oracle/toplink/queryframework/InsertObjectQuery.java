// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.descriptors.DescriptorQueryManager;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>:
 * Used for inserting new objects into the database.
 *
 * <p><b>Description</b>:
 * This class does not have much behavior.
 * It inherits most of it's behavior from WriteObjectQuery
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class InsertObjectQuery extends WriteObjectQuery {

    /**
     * PUBLIC:
     * Default constructor.
     */
    public InsertObjectQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Create an insert query with the object being inserted.
     */
    public InsertObjectQuery(Object objectToInsert) {
        this();
        setObject(objectToInsert);
    }

    /**
     * PUBLIC:
     * Create an insert query with the custom call.
     */
    public InsertObjectQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Perform an insert.
     */
    public void executeCommit() throws DatabaseException {
        // Check for redirection.
        if (getRedirector() != null) {
            redirectQuery(this, session, translationRow);
            return;
        }
        
        // Check whether the object is already being committed,
        // if it is and it is new, then a shallow insert must be done.
        if (getSession().getCommitManager().isCommitInPreModify(getObject())) {
            // A shallow insert must be performed.
            dontCascadeParts();
            getQueryMechanism().insertObjectForWrite();
            getSession().getCommitManager().markShallowCommit(object);
        } else {
            getQueryMechanism().insertObjectForWrite();
        }
    }
    
    /**
     * INTERNAL:
     * Perform an insert.
     */
    public void executeCommitWithChangeSet() throws DatabaseException {
        // The same commit is used for changeset or not for inserts.
        executeCommit();
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        super.prepare();

        getQueryMechanism().prepareInsertObject();
    }
        
    /**
     * INTERNAL:
     * Check to see if a custom query should be used for this query.
     * This is done before the query is copied and prepared/executed.
     * null means there is none.
     */
    protected DatabaseQuery checkForCustomQuery(AbstractSession session, AbstractRecord translationRow) {
        checkDescriptor(session);

        // check if user defined a custom query
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();
        if ((!isCallQuery())// this is not a hand-coded (custom SQL, SDK etc.) call
                 && (!isUserDefined())// and this is not a user-defined query (in the query manager)
                 && queryManager.hasInsertQuery()) {// and there is a user-defined query (in the query manager)
            return queryManager.getInsertQuery();
        }

        return null;
    }
    
    /**
     * INTERNAL:
     * Set the properties needed to be cascaded into the custom query.
     */
    protected void prepareCustomQuery(DatabaseQuery customQuery) {
        InsertObjectQuery customInsertQuery = (InsertObjectQuery)customQuery;        
        customInsertQuery.setObject(getObject());
        customInsertQuery.setObjectChangeSet(getObjectChangeSet());
        customInsertQuery.setCascadePolicy(getCascadePolicy());
        customInsertQuery.setShouldMaintainCache(shouldMaintainCache());
        customInsertQuery.setModifyRow(null);
    }
    
    /**
     * PUBLIC:
     * Return if this is an insert object query.
     */
    public boolean isInsertObjectQuery() {
        return true;
    }
}