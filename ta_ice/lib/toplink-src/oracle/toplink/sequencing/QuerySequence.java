// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sequencing;

import java.util.Vector;
import java.math.BigDecimal;
import oracle.toplink.sessions.Record;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: An generic query sequence mechanism.
 * <p>
 * <b>Description</b>
 * This sequence allows the sequence operations to be customized through user defined queries.
 * A select and update query can be set which can use custom SQL or stored procedures to define the sequencing mechanism.
 * If a single stored procedure is used that does the update and select only the select query needs to be set.
 */
public class QuerySequence extends StandardSequence {
    protected ValueReadQuery selectQuery;
    protected DataModifyQuery updateQuery;
    protected boolean shouldAcquireValueAfterInsert;
    protected boolean shouldUseTransaction;
    protected boolean shouldSkipUpdate;
    protected boolean shouldSelectBeforeUpdate;
    protected boolean wasSelectQueryCreated;
    protected boolean wasUpdateQueryCreated;

    public QuerySequence() {
        super();
    }
    
    /**
     * Create a new sequence with the name.
     */
    public QuerySequence(String name) {
        super(name);
    }
    
    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public QuerySequence(String name, int size) {
        super(name, size);
    }

    public QuerySequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }
    
    public QuerySequence(boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super();
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, int size, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }
    
    public QuerySequence(String name, int size, int initialValue, 
            boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size, initialValue);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }    

    public boolean equals(Object obj) {
        if (obj instanceof QuerySequence && super.equals(obj)) {
            QuerySequence other = (QuerySequence)obj;
            return (getSelectQuery() == other.getSelectQuery()) && (getUpdateQuery() == other.getUpdateQuery()) && (shouldAcquireValueAfterInsert() == other.shouldAcquireValueAfterInsert()) && (shouldUseTransaction() == other.shouldUseTransaction()) && (shouldSkipUpdate() == other.shouldSkipUpdate()) && (shouldSelectBeforeUpdate() == other.shouldSelectBeforeUpdate());

        } else {
            return false;
        }
    }

    /**
    * PUBLIC:
    */
    public boolean shouldAcquireValueAfterInsert() {
        return shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    public void setShouldAcquireValueAfterInsert(boolean shouldAcquireValueAfterInsert) {
        this.shouldAcquireValueAfterInsert = shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldUseTransaction() {
        return shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setShouldUseTransaction(boolean shouldUseTransaction) {
        this.shouldUseTransaction = shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setSelectQuery(ValueReadQuery query) {
        selectQuery = query;
    }

    /**
    * PUBLIC:
    */
    public ValueReadQuery getSelectQuery() {
        return selectQuery;
    }

    /**
    * PUBLIC:
    */
    public void setUpdateQuery(DataModifyQuery query) {
        updateQuery = query;
    }

    /**
    * PUBLIC:
    */
    public DataModifyQuery getUpdateQuery() {
        return updateQuery;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSkipUpdate(boolean shouldSkipUpdate) {
        this.shouldSkipUpdate = shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSkipUpdate() {
        return shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSelectBeforeUpdate(boolean shouldSelectBeforeUpdate) {
        this.shouldSelectBeforeUpdate = shouldSelectBeforeUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSelectBeforeUpdate() {
        return shouldSelectBeforeUpdate;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery(String seqName, Integer size) {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery(String seqName, Number sizeOrNewValue) {
        return null;
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        super.onConnect();
        if (getSelectQuery() == null) {
            setSelectQuery(buildSelectQuery());
            wasSelectQueryCreated = getSelectQuery() != null;
        }
        if ((getUpdateQuery() == null) && !shouldSkipUpdate()) {
            setUpdateQuery(buildUpdateQuery());
            wasUpdateQueryCreated = getUpdateQuery() != null;
        }
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        if (wasSelectQueryCreated) {
            setSelectQuery(null);
            wasSelectQueryCreated = false;
        }
        if (wasUpdateQueryCreated) {
            setUpdateQuery(null);
            wasUpdateQueryCreated = false;
        }
        super.onDisconnect();
    }

    /**
    * INTERNAL:
    */
    protected Number updateAndSelectSequence(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
        Integer sizeInteger = new Integer(size);
        if (shouldSkipUpdate()) {
            return (Number)select(accessor, writeSession, seqName, sizeInteger);
        } else {
            if (shouldSelectBeforeUpdate()) {
                Object result = select(accessor, writeSession, seqName, sizeInteger);
                BigDecimal currentValue;
                if (result instanceof Number) {
                    currentValue = new BigDecimal(((Number)result).longValue());
                } else if (result instanceof String) {
                    currentValue = new BigDecimal((String)result);
                } else if (result instanceof Record) {
                    Object val = ((Record)result).get("text()");
                    currentValue = new BigDecimal((String)val);
                } else {
                    // DatabaseException.errorPreallocatingSequenceNumbers() is thrown by the superclass
                    return null;
                }

                // Increment value
                BigDecimal newValue = currentValue.add(new BigDecimal(size));

                update(accessor, writeSession, seqName, newValue);
                return newValue;
            } else {
                update(accessor, writeSession, seqName, sizeInteger);
                return (Number)select(accessor, writeSession, seqName, sizeInteger);
            }
        }
    }

    /**
    * INTERNAL:
    */
    protected Object select(Accessor accessor, AbstractSession writeSession, String seqName, Integer size) {
        ValueReadQuery query = getSelectQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (ValueReadQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildSelectQuery(seqName, size);
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector args = createArguments(query, seqName, size);
        if (args != null) {
            return writeSession.executeQuery(query, args);
        } else {
            return writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected void update(Accessor accessor, AbstractSession writeSession, String seqName, Number sizeOrNewValue) {
        DataModifyQuery query = getUpdateQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (DataModifyQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildUpdateQuery(seqName, sizeOrNewValue);
            if (query == null) {
                return;
            }
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector args = createArguments(query, seqName, sizeOrNewValue);
        if (args != null) {
            writeSession.executeQuery(query, args);
        } else {
            writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected Vector createArguments(DatabaseQuery query, String seqName, Number sizeOrNewValue) {
        int nArgs = query.getArguments().size();
        if (nArgs > 0) {
            Vector args = new Vector(nArgs);
            args.addElement(seqName);
            if (nArgs > 1) {
                args.addElement(sizeOrNewValue);
            }
            return args;
        } else {
            return null;
        }
    }
}
