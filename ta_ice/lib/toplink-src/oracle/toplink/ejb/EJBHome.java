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
 * <b>Purpose</b>: Provide a useful home class for TopLink perssited bean's homes to inherit from.</p>
 *
 * <p><b>Description</b>: This class declares useful finders such as find by query and expression.</p>
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public interface EJBHome extends javax.ejb.EJBHome {

    /**
     * PUBLIC:
     * Read all the objects for the class.
     */
    public Enumeration findAll() throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the expression.
     */
    public Enumeration findAll(Expression expression) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the call.
     */
    public Enumeration findAll(Call call) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the query.
     */
    public Enumeration findAll(ReadAllQuery query) throws RemoteException, FinderException;

    /**
     * PUBLIC:
     * Read all the objects for the class given the named query.
     */
    public Enumeration findAllByNamedQuery(String queryName, Vector arguments) throws RemoteException, FinderException;
}