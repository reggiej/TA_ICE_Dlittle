// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import javax.ejb.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.expressions.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.tools.sessionconfiguration.*;
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.internal.security.PrivilegedGetDeclaredField;
import oracle.toplink.internal.security.PrivilegedGetValueFromField;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;

/**
 * <p>
 * <b>Purpose</b>: Implement the EJB entity bean BMP/CMP APIs.</p>
 *
 * <p><b>Description</b>: This class maps the EJB entity bean APIs to TopLink APIs.
 * It can be used from a BMP base class or from a CMP persister in compatible EJB servers.</p>
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Create
 * <li> Store
 * <li> Remove
 * <li> Load
 * <li> Finders
 * </ul></p>
 *
 * @author James Sutherland
 * @since TOPLink/Java 3.0
 */
public class EJBDataStore {

    /** The bean class and session name are deployment properties that are used to lookup the descriptor and the session. **/
    protected Class beanClass;
    protected String sessionName;
    protected XMLSessionConfigLoader xmlSessionConfigLoader;
    protected AbstractSession session;
    protected ClassDescriptor descriptor;

    /**
     * Create a new data store.
     * By default use the default session.
     */
    public EJBDataStore() {
        this.sessionName = "default";
    }

    /**
     * PUBLIC:
     * Insert the bean into the database.
     * This registers it with the active unit of work for insertion on commit.
     * If using sequencing (not sybase natvie) the sequence number will be assigned.
     */
    public Object create(Object bean) throws CreateException {
        try {
            UnitOfWorkImpl unitOfWork = getActiveUnitOfWork();
            if (unitOfWork == null) {
                throw ValidationException.ejbMustBeInTransaction(bean);
            }
            unitOfWork.log(SessionLog.FINEST, SessionLog.EJB, "EJB_create", bean);
            unitOfWork.assignSequenceNumber(bean);
            if (unitOfWork.checkExistence(bean) != null) {
                throw new DuplicateKeyException();
            }
            unitOfWork.registerNewContainerBean(bean);
        } catch (Exception exception) {
            throw new CreateException(ExceptionLocalization.buildMessage("create_insertion_failed", (Object[])null) + "\n" + oracle.toplink.internal.helper.Helper.printStackTraceToString(exception));
        }
        return getWrapperPolicy().buildPrimaryKeyFromBean(bean, getSession());
    }

    /**
     * PUBLIC:
     * Insert the bean into the database.
     */
    public Object create(EntityBean bean) throws CreateException {
        return create((Object)bean);
    }

    /**
     * INTERNAL:
     * Extract a Vector of the primary keys,
     */
    public Vector extractPrimaryKeyVector(Vector objects) {
        Vector primaryKeyObjects = new Vector(objects.size());
        for (Enumeration resultsEnum = objects.elements(); resultsEnum.hasMoreElements();) {
            primaryKeyObjects.addElement(getWrapperPolicy().buildPrimaryKeyFromBean(resultsEnum.nextElement(), getSession()));
        }

        return primaryKeyObjects;
    }

    /**
     * PUBLIC:
     * Read all the objects for the class and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll() throws FinderException {
        return findAll(new ReadAllQuery(getBeanClass()));
    }

    /**
     * PUBLIC:
     * Read all the objects for the class and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll() throws FinderException {
        return ejb20FindAll(new ReadAllQuery(getBeanClass()));
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the expression and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(Expression expression) throws FinderException {
        return findAll(new ReadAllQuery(getBeanClass(), expression));
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the expression and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll(Expression expression) throws FinderException {
        return ejb20FindAll(new ReadAllQuery(getBeanClass(), expression));
    }

    /**
     * Read all the objects for the class given the call and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(Call call) throws FinderException {
        return findAll(new ReadAllQuery(getBeanClass(), call));
    }

    /**
     * Read all the objects for the class given the call and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll(Call call) throws FinderException {
        return ejb20FindAll(new ReadAllQuery(getBeanClass(), call));
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the query and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(ReadAllQuery query) throws FinderException {
        return extractPrimaryKeyVector(findAllObjects(query)).elements();
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the query and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0spec.
     */
    public Collection ejb20FindAll(ReadAllQuery query) throws FinderException {
        return extractPrimaryKeyVector(findAllObjects(query));
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAllByNamedQuery(String queryName, Vector arguments) throws FinderException {
        return extractPrimaryKeyVector(findAllObjectsByNamedQuery(queryName, arguments)).elements();
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAllByNamedQuery(String queryName, Vector arguments) throws FinderException {
        return extractPrimaryKeyVector(findAllObjectsByNamedQuery(queryName, arguments));
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the query and load them into the cache.
     * Return a vector of the objects.
     */
    public Vector findAllObjects(ReadAllQuery query) throws FinderException {
        AbstractSession session = getActiveSession();
        session.log(SessionLog.FINEST, SessionLog.EJB, "EJB_find_all", query);

        query.setReferenceClass(getBeanClass());
        query.setDescriptor(getDescriptor());
        query.setShouldUseWrapperPolicy(false);

        Vector result = null;

        try {
            //oracle.toplink.internal.helper.Helper.toDo("support other collection types, this does not make sense unless container also supports/allows them");
            result = (Vector)session.executeQuery(query);
        } catch (Exception exception) {
            throw new FinderException(ExceptionLocalization.buildMessage("finder_query_failed", (Object[])null) + "\n" + oracle.toplink.internal.helper.Helper.printStackTraceToString(exception));
        }

        return result;
    }

    /**
     * PUBLIC:
     * Read all the objects for the class given the query and load them into the cache.
     * Return a vector of the objects.
     */
    public Vector findAllObjectsByNamedQuery(String queryName, Vector arguments) throws FinderException {
        AbstractSession session = getActiveSession();
        session.log(SessionLog.FINEST, SessionLog.EJB, "EJB_find_all_by_name", queryName);

        Vector result = null;

        try {
            //oracle.toplink.internal.helper.Helper.toDo("support other collection types, this does not make sense unless container also supports/allows them");
            result = (Vector)session.executeQuery(queryName, getBeanClass(), arguments);
        } catch (Exception exception) {
            throw new FinderException(ExceptionLocalization.buildMessage("finder_query_failed", (Object[])null) + "\n" + oracle.toplink.internal.helper.Helper.printStackTraceToString(exception));
        }

        return result;
    }

    /**
     * PUBLIC:
     * Find the object by primary key object and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findByPrimaryKey(Object primaryKeyObject) throws FinderException {
        // This check that the object exists.
        Object bean = getWrapperPolicy().buildBeanFromPrimaryKey(primaryKeyObject, getSession());
        findObject(new ReadObjectQuery(bean));

        return primaryKeyObject;
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * If object not found return null, otherwise return the object.
     */
    public Object findObject(ReadObjectQuery query) throws FinderException {
        AbstractSession session = getActiveSession();
        session.log(SessionLog.FINEST, SessionLog.EJB, "EJB_find_one", query);

        query.setReferenceClass(getBeanClass());
        query.setDescriptor(getDescriptor());
        query.setShouldUseWrapperPolicy(false);

        Object result = null;

        try {
            result = session.executeQuery(query);
        } catch (Exception exception) {
            throw new FinderException(ExceptionLocalization.buildMessage("finder_query_failed", (Object[])null) + "\n" + oracle.toplink.internal.helper.Helper.printStackTraceToString(exception));
        }
        if (result == null) {
            throw new ObjectNotFoundException();
        }
        return result;
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * If object not found return null, otherwise return the object.
     */
    public Object findObjectByNamedQuery(String queryName, Vector arguments) throws FinderException {
        AbstractSession session = getActiveSession();
        session.log(SessionLog.FINEST, SessionLog.EJB, "EJB_find_one_by_name", queryName);

        Object result = null;

        try {
            result = session.executeQuery(queryName, getBeanClass(), arguments);
        } catch (Exception exception) {
            throw new FinderException(ExceptionLocalization.buildMessage("finder_query_failed", (Object[])null) + "\n" + oracle.toplink.internal.helper.Helper.printStackTraceToString(exception));
        }
        if (result == null) {
            throw new ObjectNotFoundException();
        }
        return result;
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(Expression expression) throws FinderException {
        return findOne(new ReadObjectQuery(getBeanClass(), expression));
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(Call call) throws FinderException {
        return findOne(new ReadObjectQuery(getBeanClass(), call));
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(ReadObjectQuery query) throws FinderException {
        // This check that the object exists.
        Object result = findObject(query);

        return getWrapperPolicy().buildPrimaryKeyFromBean(result, getSession());
    }

    /**
     * PUBLIC:
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOneByNamedQuery(String queryName, Vector arguments) throws FinderException {
        // This check that the object exists.
        Object result = findObjectByNamedQuery(queryName, arguments);

        return getWrapperPolicy().buildPrimaryKeyFromBean(result, getSession());
    }

    /**
     * PUBLIC:
     * Return the session or unit of work if in a transaction.
     */
    public AbstractSession getActiveSession() {
        return (AbstractSession)getSession().getActiveSession();
    }

    /**
     * PUBLIC:
     * Return the unit of work in the transaction context.
     */
    public UnitOfWorkImpl getActiveUnitOfWork() {
        UnitOfWorkImpl activeUnitOfWork = (UnitOfWorkImpl)getSession().getActiveUnitOfWork();
        if (activeUnitOfWork == null) {
            return null;
        }

        // To allow the merge of new objects they must be cached by primary key.
        activeUnitOfWork.setShouldNewObjectsBeCached(true);
        return activeUnitOfWork;
    }

    /**
     * PUBLIC:
     * Return the bean class.
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /**
     * INTERNAL:
     * Return the descriptor, lookup through bean class.
     */
    public oracle.toplink.descriptors.ClassDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = getSession().getDescriptor(getBeanClass());
        }
        return descriptor;
    }

    /**
     * ADVANCED:
     * Return the session.  The ClassLoader for this session will be extracted from the bean Class.
     */
    public AbstractSession getSession() {
        if (session == null) {
            //bug#4504537 - session logging out in BMP when it shouldn't - changed shouldCheckClassLoader to be false
            ClassLoader classLoader = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    classLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedGetClassLoaderForClass(getBeanClass()));
                }catch (PrivilegedActionException ex){
                    throw (RuntimeException)ex.getCause();
                }
            }else{
                classLoader = PrivilegedAccessHelper.getClassLoaderForClass(getBeanClass());
            }
            session = (AbstractSession)oracle.toplink.tools.sessionmanagement.SessionManager.getManager().getSession(xmlSessionConfigLoader, getSessionName(), classLoader, true, false, false);
        }
        return session;
    }

    /**
     * PUBLIC:
     * Return the session name.
     * This session name is used to lookup the session.
     * By default it is default.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * PUBLIC:
     * Return the XMLSessionConfigLoader for this datastore.
     */
    public XMLSessionConfigLoader getXMLSessionConfigLoader() {
        return this.xmlSessionConfigLoader;
    }

    /**
     * INTERNAL:
     * Return the descriptor, lookup through bean class.
     */
    public EJBWrapperPolicy getWrapperPolicy() {
        return (EJBWrapperPolicy)getDescriptor().getWrapperPolicy();
    }

    /**
     * PUBLIC:
     * Load from the database/cache into the object.
     * It is assumed the bean has its' key set.
     * Runtime exceptions are thrown as load does not define any exception.
     */
    public void load(Object bean) {
        AbstractSession session = getActiveSession();
        session.log(SessionLog.FINEST, SessionLog.EJB, "EJB_load");
        Vector primaryKeys = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(bean, session);
        Object cachedBean = session.getIdentityMapAccessorInstance().getFromIdentityMap(primaryKeys, getDescriptor().getJavaClass(), getDescriptor());
        if (cachedBean == null) {
            ReadObjectQuery query = new ReadObjectQuery();
            query.setSelectionObject(bean);
            query.setShouldUseWrapperPolicy(false);
            cachedBean = session.executeQuery(query);
        }
        if (cachedBean == null) {
            //Must create Validation exception because it is a runtime exception and does not require a throws clause
            Object[] args = { bean };
            throw ValidationException.ejbContainerExceptionRaised(new ObjectNotFoundException(ExceptionLocalization.buildMessage("bean_not_found_on_database", args)));
        }

        Object clonedBean = cachedBean;

        // If in unit of work must clone the bean's dependent parts.
        if (session.isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)session;

            // Use a parralel unit of work to manage the container's clones.
            clonedBean = unitOfWork.getContainerUnitOfWork().registerExistingObject(cachedBean);

            // Link the container bean to the clone.
            unitOfWork.getContainerBeans().put(bean, cachedBean);
        }

        getDescriptor().getObjectBuilder().copyInto(clonedBean, bean);
    }

    /**
     * PUBLIC:
     * Delete the bean from the database.
     */
    public void remove(Object bean) throws RemoveException {
        try {
            UnitOfWorkImpl unitOfWork = getActiveUnitOfWork();
            if (unitOfWork == null) {
                throw ValidationException.ejbMustBeInTransaction(bean);
            }
            unitOfWork.log(SessionLog.FINEST, SessionLog.EJB, "EJB_remove", bean);

            //we must get the bean from the local session so that we are deleting the correct version
            // Not the container version
            Vector primaryKeys = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(bean, session);
            Object cachedBean = session.getIdentityMapAccessorInstance().getFromIdentityMap(primaryKeys, getDescriptor().getJavaClass(), getDescriptor());
            if (cachedBean == null) {
                ReadObjectQuery query = new ReadObjectQuery();
                query.setSelectionObject(bean);
                query.setShouldUseWrapperPolicy(false);
                cachedBean = unitOfWork.executeQuery(query);
            }
            if (cachedBean == null) {
                //Must create Validation exception because it is a runtime exception and does not require a throws clause
                Object[] args = { bean };
                throw ValidationException.ejbContainerExceptionRaised(new ObjectNotFoundException(ExceptionLocalization.buildMessage("bean_not_found_on_database", args)));

            }
            unitOfWork.deleteObject(cachedBean);
        } catch (Exception exception) {
            throw new RemoveException(ExceptionLocalization.buildMessage("remove_deletion_failed", (Object[])null) + "\n" + exception);
        }
    }

    /**
     * PUBLIC:
     * Delete the bean from the database.
     */
    public void remove(EntityBean bean) throws RemoveException {
        remove((Object)bean);
    }

    /**
     * REQUIRED:
     * Set the bean class for the data store.
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    protected void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * PUBLIC:
     * Set the session.
     * By default it is looked up through the session name or the default session is used.
     */
    public void setSession(oracle.toplink.sessions.Session session) {
        this.session = (AbstractSession)session;
    }

    /**
     * PUBLIC:
     * Set the session name.
     * This session name is used to lookup the session.
     * By default it is default.
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * PUBLIC:
     * Set the XMLSessionConfigLoader that will be used to load the session configuration information.
     * By default it is it is the core loader.
     */
    public void setXMLSessionConfigLoader(XMLSessionConfigLoader xmlSessionConfigloader) {
        this.xmlSessionConfigLoader = xmlSessionConfigloader;
    }

    /**
     * PUBLIC:
     * Update the bean into the database.
     * Note, runtime exceptions are thrown as store defines no exception.
     * This uses the unit of work merge to update the TopLink version of the object.
     */
    public void store(Object bean) throws DatabaseException, OptimisticLockException {
        // Unfortunately this is called after beforeCompletion in JTS for some reason... so..
        // we cannot do anything here.
        // Merge clone is enough as independent parts are other beans and stored seperatly.
        UnitOfWorkImpl unitOfWork = getActiveUnitOfWork();
        unitOfWork.log(SessionLog.FINEST, SessionLog.EJB, "EJB_store", bean);
        //unitOfWork.mergeClone(bean);
    }

    /**
     * PUBLIC:
     * Update the bean into the database.
     * Note, runtime exceptions are thrown as store defines no exception.
     * Store must always be an update, so update is called, not write object.
     */
    public void store(EntityBean bean) throws DatabaseException, OptimisticLockException {
        store((Object)bean);
    }
}
