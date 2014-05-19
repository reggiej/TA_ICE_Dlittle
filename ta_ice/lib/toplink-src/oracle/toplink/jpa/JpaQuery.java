// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.jpa;

import java.util.Collection;
import oracle.toplink.queryframework.DatabaseQuery;

/**
 * PUBLIC:
 * TopLInk specific EJB 3.0 query interface.  Provides the functionality defined in
 * javax.persistence.Query and adds access to the underlying database query for TopLink specific
 * functionality.
 */
public interface JpaQuery extends javax.persistence.Query {

    /**
     * PUBLIC:
     * Return the cached database query for this EJBQueryImpl.  If the query is
     * a named query and it has not yet been looked up, the query will be looked up
     * and stored as the cached query.
     */
    public DatabaseQuery getDatabaseQuery();

    /**
     * PUBLIC:
     * return the EntityManager for this query
     */
    public JpaEntityManager getEntityManager();

    /**
     * PUBLIC:
     * Non-standard method to return results of a ReadQuery that has a containerPoliry
     * that returns objects as a collection rather than a List
     * @return Collection of results
     */
    public Collection getResultCollection();

    /**
     * PUBLIC:
     * Replace the cached query with the given query.
     */
    public void setDatabaseQuery(DatabaseQuery query);

}
