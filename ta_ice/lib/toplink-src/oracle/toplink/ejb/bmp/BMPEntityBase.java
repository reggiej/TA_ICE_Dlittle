// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb.bmp;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Collection;
import javax.ejb.*;
import javax.ejb.CreateException;
import oracle.toplink.ejb.*;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.queryframework.ReadObjectQuery;
import oracle.toplink.expressions.Expression;
import oracle.toplink.queryframework.Call;
import oracle.toplink.tools.sessionconfiguration.*;

/**
 * <p>
 * <b>Purpose</b>: To provide bean developers a prebuilt accessor into TopLink functionality for beans using BMP.</p>
 *
 * <p><b>Description</b>: This class provides implementations of ejbStore(), ejbLoad() and others
 * with hooks into TopLink functionality.  This allows bean developers to simply extend this
 * class and with a TopLink project provide persistence functionality to your bean</p>
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Provides required functionality for Bean Managed Persistence
 * </ul></p>
 *
 * @author Gordon J Yorke
 * @since TOPLink/Java 3.0
 */
public class BMPEntityBase {
    protected EJBDataStore datastore;
    protected String sessionName;
    protected EntityContext entityContext;
    protected XMLSessionConfigLoader xmlSessionConfigLoader;

    /**
     * BMPEntityBase constructor.
     */
    public BMPEntityBase() {
        super();
        this.xmlSessionConfigLoader = new XMLSessionConfigLoader();
        this.sessionName = "default";
    }

    /**
     * BMPEntityBase constructor.
     */
    public BMPEntityBase(String sessionName) {
        super();
        this.xmlSessionConfigLoader = new XMLSessionConfigLoader();
        this.sessionName = sessionName;
    }

    /**
     * BMPEntityBase constructor.
     */
    public BMPEntityBase(String sessionName, XMLSessionConfigLoader xmlSessionConfigloader) {
        super();
        this.xmlSessionConfigLoader = xmlSessionConfigloader;
        this.sessionName = sessionName;
    }

    /**
     * This is a "no-op" for TopLink BMP.  The developer may wish to perform certain operations
     * here.  This method is usually called after a bean has been taken out of the pool, by
     * the container to be re-used.
     */
    public void ejbActivate() {
    }

    /**
     * Load from the database/cache into the object.
     * It is assumed the bean has it's key set.
     * Finder exceptions are thrown as load does not define any exception.
     */
    public void ejbLoad() {
        ((EJBWrapperPolicy)getDataStore().getDescriptor().getWrapperPolicy()).dumpPrimaryKeyIntoBean(getEntityContext().getPrimaryKey(), this, getDataStore().getActiveSession());
        getDataStore().load(this);
    }

    /**
     * Delete the bean from the database.
     */
    public void ejbRemove() throws RemoveException {
        getDataStore().remove(this);
    }

    /**
     * This is a no-op, because the ejbStore is called after beforeCompletion which is the callback
     * to TopLink to commit the transaction
     */
    public void ejbStore() {
        getDataStore().store(this);

    }

    /**
     * Read all the objects for the class and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll() throws FinderException {
        return getDataStore().findAll();
    }

    /**
     * Read all the objects for the class and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll() throws FinderException {
        return getDataStore().ejb20FindAll();
    }

    /**
     * Read all the objects for the class given the expression and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(Expression expression) throws FinderException {
        return getDataStore().findAll(expression);
    }

    /**
     * Read all the objects for the class given the expression and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll(Expression expression) throws FinderException {
        return getDataStore().ejb20FindAll(expression);
    }

    /**
     * Read all the objects for the class given the call and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(Call call) throws FinderException {
        return getDataStore().findAll(call);
    }

    /**
     * Read all the objects for the class given the call and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll(Call call) throws FinderException {
        return getDataStore().ejb20FindAll(call);
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAll(ReadAllQuery query) throws FinderException {
        return getDataStore().findAll(query);
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAll(ReadAllQuery query) throws FinderException {
        return getDataStore().ejb20FindAll(query);
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return an enumeration on the primary key objects as per the BMP spec.
     */
    public Enumeration findAllByNamedQuery(String queryName, Vector arguments) throws FinderException {
        return getDataStore().findAllByNamedQuery(queryName, arguments);
    }

    /**
     * Read all the objects for the class given the query and load them into the cache.
     * Return a Collection on the primary key objects as per the BMP 2.0 spec.
     */
    public Collection ejb20FindAllByNamedQuery(String queryName, Vector arguments) throws FinderException {
        return getDataStore().ejb20FindAllByNamedQuery(queryName, arguments);
    }

    /**
     * Find the object by primary key object and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findByPrimaryKey(Object primaryKeyObject) throws FinderException {
        return getDataStore().findByPrimaryKey(primaryKeyObject);
    }

    /**
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(Expression expression) throws FinderException {
        return getDataStore().findOne(expression);
    }

    /**
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(Call call) throws FinderException {
        return getDataStore().findOne(call);
    }

    /**
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOne(ReadObjectQuery query) throws FinderException {
        return getDataStore().findOne(query);
    }

    /**
     * Find the object by the query and load it into the cache.
     * Throw object not found if null, otherwise return the primary key.
     */
    public Object findOneByNamedQuery(String queryName, Vector arguments) throws FinderException {
        return getDataStore().findOneByNamedQuery(queryName, arguments);
    }

    /**
     * PUBIC:
     * Returns the dataStore object for this bean
     *
     * @return oracle.toplink.ejb.EJBDataStore
     */
    public EJBDataStore getDataStore() {
        if (this.datastore == null) {
            this.datastore = BMPDatastoreManager.getManager().getDatastore(this.xmlSessionConfigLoader, getSessionName(), this.getClass());
        }
        return this.datastore;
    }

    /**
     * PUBIC:
     * Returns the EntityContext associated with this bean
     *
     */
    public EntityContext getEntityContext() {
        return this.entityContext;
    }

    /**
     * Returns the Session name that represents the session that this bean should be using
     *
     */
    public java.lang.String getSessionName() {
        return sessionName;
    }

    /**
     * This method is called by the container when a new bean instance is created.  When this method is
     * Called the context has yet to be associated with any entity object identity
     */
    public void setEntityContext(EntityContext ctx) {
        this.entityContext = ctx;
    }

    /**
     * PUBLIC:
     * This method calls the internal TopLink inplementation to create a persistent object.
     *
     * @return java.lang.Object The primary key object from the newly created bean
     */
    public Object tlCreateImpl() throws CreateException {
        return getDataStore().create(this);
    }

    /**
     * This method is called by the container when a bean instance is being removed from the pool
     */
    public void unsetEntityContext() {
        this.entityContext = null;
    }
}
