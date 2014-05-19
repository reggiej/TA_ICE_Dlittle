// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 * Session object that stores all deployment descriptor
 * info pertaining to a particular session bean.
 */
public class Session extends EnterpriseObject {
    protected String home;// Optional
    protected String remote;// Optional
    protected String localHome;// Optional
    protected String local;// Optional
    String sessionType;// Required
    String transactionType;// Required
    Vector securityRoleReferences;// 0 or more SecurityRoleReference objects

    /**
     * @return String the home String
     */
    public String getHome() {
        return home;
    }

    /**
     * @return String the remote String
     */
    public String getRemote() {
        return remote;
    }

    /**
     * @return Vector the collection of SecurityRoleReference, or null if not set
     */
    public Vector getSecurityRoleReferences() {
        return securityRoleReferences;
    }

    /**
     * @return String the session type String
     */
    public String getSessionType() {
        return sessionType;
    }

    /**
     * @return String the transaction type String
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * Return true if this is a session object, false if not.
     */
    public boolean isSession() {
        return true;
    }

    /**
     * @param h the home String
     */
    public void setHome(String h) {
        home = h;
    }

    /**
     * @param rem the remote String
     */
    public void setRemote(String rem) {
        remote = rem;
    }

    /**
     *
     * @param refs the collection of SecurityRoleReference
     */
    public void setSecurityRoleReferences(Vector refs) {
        securityRoleReferences = refs;
    }

    /**
     * @param sessType the session type String
     */
    public void setSessionType(String sessType) {
        sessionType = sessType;
    }

    /**
     * @param transType the transaction type String
     */
    public void setTransactionType(String transType) {
        transactionType = transType;
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
        sessionType = stringFromElement(e, SESSION_TYPE);
        transactionType = stringFromElement(e, TRANSACTION_TYPE);
        securityRoleReferences = optionalObjectsFromElement(e, SECURITY_ROLE_REF, new SecurityRoleReference());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(SESSION);
        inheritedFields(doc, e);

        optionallyAddText(doc, e, HOME, home);
        optionallyAddText(doc, e, REMOTE, remote);
        optionallyAddText(doc, e, LOCAL_HOME, localHome);
        optionallyAddText(doc, e, LOCAL, local);
        addText(doc, e, EJB_CLASS, ejbClass);
        addText(doc, e, SESSION_TYPE, sessionType);
        addText(doc, e, TRANSACTION_TYPE, transactionType);
        optionallyAddCollection(doc, e, envEntries);
        optionallyAddCollection(doc, e, ejbReferences);
        optionallyAddCollection(doc, e, ejbLocalReferences);
        optionallyAddCollection(doc, e, securityRoleReferences);
        if (securityIdentity != null) {
            e.appendChild(securityIdentity.toElement(doc));
        }
        optionallyAddCollection(doc, e, resourceReferences);
        optionallyAddCollection(doc, e, resourceEnvReferences);
        return e;
    }
}