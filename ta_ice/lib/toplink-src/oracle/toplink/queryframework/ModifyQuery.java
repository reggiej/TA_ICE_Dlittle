// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all modify queries.
 * Currently contains no behavoir.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class ModifyQuery extends DatabaseQuery {
    protected AbstractRecord modifyRow;
    
    // needed to allow the user to force SQL to database when batch writing is used. bug:4104613
    protected boolean forceBatchStatementExecution = false;

    /**
     * INTERNAL:
     * Return the modify row
     */
    public AbstractRecord getModifyRow() {
        return modifyRow;
    }

    /**
     * PUBLIC:
     * Return if this is a modify query.
     */
    public boolean isModifyQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Set the modify row
     */
    public void setModifyRow(AbstractRecord row) {
        modifyRow = row;
    }
    
    /**
     * PUBLIC:
     * Allow setting this query to be the last statement added to a batch statement 
     * and ensure it is flushed on execution.  Setting to true will cause the batch
     * statement to be sent to the database.  Default setting of false causes the batch 
     * statement execution to be delayed to allow additional statements to
     * be added.  Setting to true reduces the efficiency of batch writting.  
     * 
     *  This has no effect if batch writing is not enabled.   
     */
     
    public void setForceBatchStatementExecution(boolean value) {
        this.forceBatchStatementExecution = value;
    }
    
    /**
     * PUBLIC:
     * Returns if this query has been set to flush on execution.
     * @see #setForceBatchStatementExecution(boolean)
     */
     
    public boolean forceBatchStatementExecution() {
        return forceBatchStatementExecution;
    }
}