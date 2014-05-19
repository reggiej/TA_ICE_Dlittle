// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;
import java.util.Iterator;

/**
 * INTERNAL:
 * Entity object that stores all deployment descriptor
 * info pertaining to a particular entity bean.
 */
public class Entity extends EnterpriseObject {
    protected String home;// Optional
    protected String remote;// Optional
    protected String localHome;// Optional
    protected String local;// Optional

    //String ejbClassName; // defined in super
    protected String persistenceType;// Required
    protected String primaryKeyClass;// Required
    protected boolean reentrant;// Required
    protected String cmpVersion;// Optional
    protected String abstractSchemaName;// Optional
    protected Vector cmpFields;// 0 or more CmpField objects
    protected String primaryKeyField;// Optional
    protected Vector securityRoleReferences;// 0 or more SecurityRoleReference objects
    protected Vector queries;// 0 or more Query objects

    public void addCmpField(String cmpFieldName) {
        if (cmpFields == null) {
            cmpFields = new Vector();
        }
        cmpFields.add(new CmpField(cmpFieldName));
    }

    public void addQuery(Query query) {
        if (queries == null) {
            queries = new Vector();
        }
        queries.add(query);
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public CmpField getCmpFieldNamed(String name) {
        if (cmpFields == null) {
            return null;
        }

        Iterator cmpFieldIt = cmpFields.iterator();
        while (cmpFieldIt.hasNext()) {
            CmpField cmpField = (CmpField)cmpFieldIt.next();
            if (name.equals(cmpField.getName())) {
                return cmpField;
            }
        }
        return null;
    }

    public Vector getCmpFields() {
        return cmpFields;
    }

    public String getCmpVersion() {
        return cmpVersion;
    }

    /**
    * Left for legacy reasons, it is superceded with getRemoteHome()
    */
    public String getHome() {
        return home;
    }

    public String getLocalHome() {
        return localHome;
    }

    public String getLocal() {
        return local;
    }

    public String getPersistenceType() {
        return persistenceType;
    }

    public String getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    public String getPrimaryKeyField() {
        return primaryKeyField;
    }

    public Vector getQueries() {
        return queries;
    }

    public boolean getReentrant() {
        return reentrant;
    }

    public String getRemote() {
        return remote;
    }

    /**
    * Preferred way of getting Remote home.
    */
    public String getRemoteHome() {
        return getHome();// left for legacy reasons.
    }

    /**
    * Just a helper method, preferred way of setting Remote home.
    */
    public void setRemoteHome(String remoteHome) {
        setHome(remoteHome);
    }

    public Vector getSecurityRoleReferences() {
        return securityRoleReferences;
    }

    public boolean isEntity() {
        return true;
    }

    /**
     * @param abstractName the abstract schema name String
     */
    public void setAbstractSchemaName(String abstractName) {
        abstractSchemaName = abstractName;
    }

    /**
     *
     * @param cmpFlds the collection of CmpField
     */
    public void setCmpFields(Vector cmpFlds) {
        cmpFields = cmpFlds;
    }

    /**
     * @param cmpVer the cmp version String
     */
    public void setCmpVersion(String cmpVer) {
        if ((cmpVer == null) || cmpVer.equalsIgnoreCase(EjbJarConstants.CMP_VERSION_2)) {
            cmpVersion = EjbJarConstants.CMP_VERSION_2;
        } else if (cmpVer.equalsIgnoreCase(EjbJarConstants.CMP_VERSION_1)) {
            cmpVersion = EjbJarConstants.CMP_VERSION_1;
        } else {
            cmpVersion = null;
        }
    }

    /**
     * Left for legacy reasons, it is superceeded with setRemoteHome()
     * @param h the home String
     */
    public void setHome(String h) {
        home = h;
    }

    public void setLocal(String newLocal) {
        local = newLocal;
    }

    public void setLocalHome(String newLocalHome) {
        localHome = newLocalHome;
    }

    /**
     * @param pType the persistence type String
     */
    public void setPersistenceType(String pType) {
        if (pType.equalsIgnoreCase(EjbJarConstants.BEAN_MANAGED)) {
            persistenceType = EjbJarConstants.BEAN_MANAGED;
        } else if (pType.equalsIgnoreCase(EjbJarConstants.CONTAINER_MANAGED)) {
            persistenceType = EjbJarConstants.CONTAINER_MANAGED;
        } else {
            persistenceType = null;
        }
    }

    /**
     * @param primKeyCls the primary key class String
     */
    public void setPrimaryKeyClass(String primKeyCls) {
        primaryKeyClass = primKeyCls;
    }

    /**
     * @param primKeyField the primary key field String
     */
    public void setPrimaryKeyField(String primKeyField) {
        primaryKeyField = primKeyField;
    }

    /**
     *
     * @param q the collection of Query
     */
    public void setQueries(Vector q) {
        queries = q;
    }

    /**
     * @param flag true if reentrant, false if not
     */
    public void setReentrant(boolean flag) {
        reentrant = flag;
    }

    /**
     * @param rem the remote String
     */
    public void setRemote(String rem) {
        remote = rem;
    }

    /**
     *
     * @param securityRoleRefs the collection of SecurityRoleReference
     */
    public void setSecurityRoleReferences(Vector securityRoleRefs) {
        securityRoleReferences = securityRoleRefs;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        home = optionalStringFromElement(e, HOME);
        remote = optionalStringFromElement(e, REMOTE);
        localHome = optionalStringFromElement(e, LOCAL_HOME);
        local = optionalStringFromElement(e, LOCAL);
        setPersistenceType(stringFromElement(e, PERSISTENCE_TYPE));
        primaryKeyClass = stringFromElement(e, PRIM_KEY_CLASS);
        reentrant = TRUE_VALUE.equalsIgnoreCase(stringFromElement(e, REENTRANT));
        setCmpVersion(optionalStringFromElement(e, CMP_VERSION));
        abstractSchemaName = optionalStringFromElement(e, ABSTRACT_SCHEMA_NAME);
        cmpFields = optionalObjectsFromElement(e, CMP_FIELD, new CmpField());
        primaryKeyField = optionalStringFromElement(e, PRIMKEY_FIELD);
        securityRoleReferences = optionalObjectsFromElement(e, SECURITY_ROLE_REF, new SecurityRoleReference());
        queries = optionalObjectsFromElement(e, QUERY, new Query());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(ENTITY);
        inheritedFields(doc, e);
        optionallyAddText(doc, e, HOME, home);
        optionallyAddText(doc, e, REMOTE, remote);
        optionallyAddText(doc, e, LOCAL_HOME, localHome);
        optionallyAddText(doc, e, LOCAL, local);
        addText(doc, e, EJB_CLASS, ejbClass);
        addText(doc, e, PERSISTENCE_TYPE, persistenceType);
        addText(doc, e, PRIM_KEY_CLASS, primaryKeyClass);

        // use True/False for dtd
        String reentrantValue = reentrant ? TRUE_VALUE : FALSE_VALUE;

        String schemaLocation = doc.getDocumentElement().getAttribute(XSI_SCHEMALOCATION);
        if (!(schemaLocation == null || schemaLocation.equals(""))) {
            // use true/false for schema
            reentrantValue = String.valueOf(reentrant);
        }
        e.appendChild(createElement(doc, REENTRANT, reentrantValue));

        optionallyAddText(doc, e, CMP_VERSION, cmpVersion);
        optionallyAddText(doc, e, ABSTRACT_SCHEMA_NAME, abstractSchemaName);
        optionallyAddCollection(doc, e, cmpFields);
        optionallyAddText(doc, e, PRIMKEY_FIELD, primaryKeyField);
        optionallyAddCollection(doc, e, envEntries);
        optionallyAddCollection(doc, e, ejbReferences);
        optionallyAddCollection(doc, e, ejbLocalReferences);
        optionallyAddCollection(doc, e, securityRoleReferences);

        if (securityIdentity != null) {
            e.appendChild(securityIdentity.toElement(doc));
        }

        optionallyAddCollection(doc, e, resourceReferences);
        optionallyAddCollection(doc, e, resourceEnvReferences);
        optionallyAddCollection(doc, e, queries);
        return e;
    }

    public boolean isUnknownPkClassDefined() {
        return primaryKeyClass.equals("java.lang.Object");
    }
}
