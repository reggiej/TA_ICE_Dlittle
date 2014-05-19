// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.exceptions.*;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;

/**
 * <p><b>Purpose</b>:
 * Concrete class used for executing non selecting SQL strings.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Execute a non selecting raw SQL string.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DataModifyQuery extends ModifyQuery {
    public DataModifyQuery() {
        super();
    }

	/** 
	  * Warning: Allowing an unverified SQL string to be passed into this 
	  * method makes your application vulnerable to SQL injection attacks. 
	  */
    public DataModifyQuery(String sqlString) {
        this();

        setSQLString(sqlString);
    }

    public DataModifyQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Perform the work to execute the SQL call.
     * Return the row count of the number of rows effected by the SQL call.
     */
    public Object executeDatabaseQuery() throws DatabaseException {

        /* Fix to allow executing non-selecting SQL in a UnitOfWork. - RB */
        if (getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();
            /* bug:4211104 for DataModifyQueries executed during an event, while transaction was started by the uow*/
            if ( !unitOfWork.getCommitManager().isActive() && !unitOfWork.isInTransaction()) {
                unitOfWork.beginEarlyTransaction();
            }
            unitOfWork.setWasNonObjectLevelModifyQueryExecuted(true);
        }
        return getQueryMechanism().executeNoSelect();
    }

    /**
     * PUBLIC:
     * Return if this is a data modify query.
     */
    public boolean isDataModifyQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        super.prepare();

        getQueryMechanism().prepareExecuteNoSelect();
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session. In particular,
     * set the descriptor of the receiver to the ClassDescriptor for the
     * appropriate class for the receiver's object.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        setModifyRow(getTranslationRow());
    }
}