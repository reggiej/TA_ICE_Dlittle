// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.jpa;

import java.util.Vector;

import oracle.toplink.sessions.*;
import oracle.toplink.expressions.Expression;
import oracle.toplink.threetier.ServerSession;

/**
 * <p>
 * <b>Purpose</b>: Defines the Interface for TopLink extensions to the EntityManager
 * <p>
 * @see javax.persistence.EntityManager
 * @author Gordon Yorke
 */
public interface JpaEntityManager extends javax.persistence.EntityManager {

	/**
	 * This method returns the current session to the requestor.  The current session
	 * will be a the active UnitOfWork within a transaction and will be a 'scrap'
	 * UnitOfWork outside of a transaction.  The caller is conserned about the results
	 * then the getSession() or getUnitOfWork() API should be called.
	 */
    public Session getActiveSession();
    
    /**
     * Return the underlying server session
     */
    public ServerSession getServerSession();
    
    /**
     * This method will return the transactional UnitOfWork during the transaction and null
     * outside of the transaction.
     */
    public UnitOfWork getUnitOfWork();
    
    /**
     * This method will return a Session outside of a transaction and null within a transaction.
     */
    public Session getSession();
    
    /**
     * This method is used to create a query using a Toplink Expression and the return type.
     */
    public javax.persistence.Query createQuery(Expression expression, Class resultType);
    
    /**
     * This method will create a query object that wraps a TopLink Named Query.
     */
    public javax.persistence.Query createDescriptorNamedQuery(String queryName, Class descriptorClass);
    
    /**
     * This method will create a query object that wraps a TopLink Named Query.
     */
    public javax.persistence.Query createDescriptorNamedQuery(String queryName, Class descriptorClass, Vector argumentTypes);

}
