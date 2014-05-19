// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.*;
import oracle.toplink.remote.*;

/**
 * <p><b>Purpose</b>:
 * Used to support cursored streams in a read query.
 * <p>
 * <p><b>Responsibilities</b>:
 * Execute the cursored read and build the stream.
 *
 * @author James Sutherland
 * @since TOPLink/Java 1.2
 */
public class CursoredStreamPolicy extends CursorPolicy {
    protected int initialReadSize;
    protected ValueReadQuery sizeQuery;

    /**
     * default constructor
     */
    public CursoredStreamPolicy() {
        super();
    }

    /**
     * set the initial read size to match the page size
     */
    public CursoredStreamPolicy(ReadQuery query, int pageSize) {
        super(query, pageSize);
        setInitialReadSize(pageSize);
    }

    public CursoredStreamPolicy(ReadQuery query, int initialReadSize, int pageSize) {
        this(query, pageSize);
        setInitialReadSize(initialReadSize);
    }

    public CursoredStreamPolicy(ReadQuery query, int initialReadSize, int pageSize, ValueReadQuery sizeQuery) {
        this(query, initialReadSize, pageSize);
        setSizeQuery(sizeQuery);
    }

    /**
     * INTERNAL:
     * Execute the cursored select and build the stream.
     */
    public Object execute() {
        DatabaseCall call = getQuery().getQueryMechanism().cursorSelectAllRows();

        // Create cursored stream		
        CursoredStream stream = new CursoredStream(call, this);

        return stream;
    }

    /**
     * Specifies the number of elements to be read initially into a cursored stream.
     */
    public int getInitialReadSize() {
        return initialReadSize;
    }

    /**
     * Return the query used to read the size.
     * This is required for SQL read queries.
     */
    public ValueReadQuery getSizeQuery() {
        return sizeQuery;
    }

    /**
     * INTERNAL:
     * Return if a custom size query is defined.
     */
    public boolean hasSizeQuery() {
        return sizeQuery != null;
    }

    public boolean isCursoredStreamPolicy() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare and validate.
     */
    public void prepare(DatabaseQuery query, AbstractSession session) throws QueryException {
        super.prepare(query, session);
    }

    /**
     * INTERNAL:
     * Execute the cursored select and build the stream.
     */
    public Object remoteExecute() {
        return ((DistributedSession)getQuery().getSession()).cursorSelectObjects(this);
    }

    /**
     * Specifies the number of elements to be read initially into a cursored stream
     */
    public void setInitialReadSize(int initialReadSize) {
        this.initialReadSize = initialReadSize;
    }

    /**
     * Set the query used to read the size.
     * This is required for SQL read queries.
     */
    public void setSizeQuery(ValueReadQuery sizeQuery) {
        this.sizeQuery = sizeQuery;
    }
}