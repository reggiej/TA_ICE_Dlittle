// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb;

import java.util.*;
import java.rmi.*;
import javax.ejb.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.expressions.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.*;

/**
 * <p>
 * <b>Purpose</b>: Provide a useful home class for TopLink persisted bean's homes to inherit from in EJB 2.0.</p>
 *
 * <p><b>Description</b>: This class declares useful finders such as find by query and expression.</p>
 *
 */
public interface EJB20Home extends javax.ejb.EJBHome {

    /**
     * PUBLIC:
     * Read all the objects for the class.
     */
    public Collection findAll() throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the expression.
     */
    public Collection findAll(Expression expression) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the call.
     */
    public Collection findAll(Call call) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the query.
     */
    public Collection findAll(ReadAllQuery query) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the named query.
     */
    public Collection findAllByNamedQuery(String queryName, Vector arguments) throws RemoteException, FinderException;
}