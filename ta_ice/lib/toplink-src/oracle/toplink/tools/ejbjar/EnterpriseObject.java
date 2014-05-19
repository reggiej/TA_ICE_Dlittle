// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 * Superclass for session, entity and messagedriven beans.
 * Abstract object that contains common state for all
 * three types of enterprise bean objects: entity, session
 * and message-driven beans.
 */
public abstract class EnterpriseObject extends Description {
    String displayName;// Optional
    String smallIcon;// Optional
    String largeIcon;// Optional
    String ejbName;// Required
    String ejbClass;// Required
    String local;// Optional
    String remote;// Optional
    Vector envEntries;// 0 or more EnvironmentEntry objects for env-entry*
    Vector ejbReferences;// 0 or more EjbReference objects for ejb-ref*
    Vector ejbLocalReferences;// 0 or more EjbReference objects for ejb-local-ref*
    SecurityIdentity securityIdentity;// Optional for security-identity?
    Vector resourceReferences;// 0 or more ResourceReference objects for resource-ref*
    Vector resourceEnvReferences;// 0 or more ResourceEnvReference objects for resource-env-ref*

    /**
     * @return String the display name String, or null if not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return String the ejb class name String
     */
    public String getEjbClass() {
        return ejbClass;
    }

    /**
     * @return String the ejb name String
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * @return Vector the collection of EjbReference, or null if not set
     */
    public Vector getEjbReferences() {
        return ejbReferences;
    }

    /**
     * @return Vector the collection of EnvironmentEntry, or null if not set
     */
    public Vector getEnvEntries() {
        return envEntries;
    }

    /**
     * @return String the largeIcon String, or null if not set
     */
    public String getLargeIcon() {
        return largeIcon;
    }

    /**
     *
     * @return Vector the collection of ResourceEnvReference, or null if not set
     */
    public Vector getResourceEnvReferences() {
        return resourceEnvReferences;
    }

    /**
     *
     * @return Vector the collection of ResourceReference, or null if not set
     */
    public Vector getResourceReferences() {
        return resourceReferences;
    }

    /**
     * @return SecurityIdentity the security identity, or null if not set
     */
    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    /**
     * @return String the smallIcon String, or null if not set
     */
    public String getSmallIcon() {
        return smallIcon;
    }

    /**
     * Return true if this is an entity object, false if not.
     */
    public boolean isEntity() {
        return false;
    }

    /**
     * Return true if this is a message-driven object, false if not.
     */
    public boolean isMessageDriven() {
        return false;
    }

    /**
     * Return true if this is a session object, false if not.
     */
    public boolean isSession() {
        return false;
    }
    
    public String getLocal() {
        return local;
    }

    public String getRemote() {
        return remote;
    }

    /**
     * @param s the display name String
     */
    public void setDisplayName(String s) {
        displayName = s;
    }

    /**
     * @param ejbCls the ejb class name String
     */
    public void setEjbClass(String ejbCls) {
        ejbClass = ejbCls;
    }

    /**
     * @param ejbNm the ejb name String
     */
    public void setEjbName(String ejbNm) {
        ejbName = ejbNm;
    }

    /**
     * @param ejbRefs the collection of EjbReference
     */
    public void setEjbReferences(Vector ejbRefs) {
        ejbReferences = ejbRefs;
    }

    /**
     * @return Vector the collection of EjbLocalReference, or null if not set
     */
    public Vector getEjbLocalReferences() {
        return ejbLocalReferences;
    }

    /**
     * @param ejbLocalRefs the collection of EjbReference
     */
    public void setEjbLocalReferences(Vector ejbLocalRefs) {
        ejbLocalReferences = ejbLocalRefs;
    }

    /**
     *
     * @param entries the collection of EnvironmentEntry
     */
    public void setEnvEntries(Vector entries) {
        envEntries = entries;
    }

    /**
     * @param s the largeIcon String
     */
    public void setLargeIcon(String s) {
        largeIcon = s;
    }

    /**
     *
     * @param resourceRefs the collection of ResourceReference
     */
    public void setResourceReferences(Vector resourceRefs) {
        resourceReferences = resourceRefs;
    }

    /**
      * @param securityIdent the SecurityIdentity object
      */
    public void setSecurityIdentity(SecurityIdentity securityIdent) {
        securityIdentity = securityIdent;
    }

    /**
      * @param s the smallIcon String
      */
    public void setSmallIcon(String s) {
        smallIcon = s;
    }

    /**
     * Insert elements for the fields that subclasses inherit
     * from this object.
     * Note: This means a class needs to know if it has subclasses.
     * @param doc a Document instance used to create elements
     * @param e the parent Element in which to insert the field elements
     */
    public void inheritedFields(Document doc, Element e) {
        super.inheritedFields(doc, e);
        optionallyAddText(doc, e, DISPLAY_NAME, displayName);
        optionallyAddText(doc, e, SMALL_ICON, smallIcon);
        optionallyAddText(doc, e, LARGE_ICON, largeIcon);
        addText(doc, e, EJB_NAME, ejbName);

        // addText(doc, e, EJB_CLASS, ejbClass);  
        //optionallyAddCollection(doc, e, envEntries); 
        //optionallyAddCollection(doc, e, ejbReferences); 
        //if(securityIdentity!=null) e.appendChild(securityIdentity.toElement(doc)); 
        //optionallyAddCollection(doc, e, resourceReferences); 
        //optionallyAddCollection(doc, e, resourceEnvReferences); 
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        displayName = optionalStringFromElement(e, DISPLAY_NAME);
        smallIcon = optionalStringFromElement(e, SMALL_ICON);
        largeIcon = optionalStringFromElement(e, LARGE_ICON);
        setEjbName(stringFromElement(e, EJB_NAME));
        setEjbClass(stringFromElement(e, EJB_CLASS));
        envEntries = optionalObjectsFromElement(e, ENV_ENTRY, new EnvironmentEntry());
        ejbReferences = optionalObjectsFromElement(e, EJB_REF, new EjbReference());
        ejbLocalReferences = optionalObjectsFromElement(e, EJB_LOCAL_REF, new EjbLocalReference());
        securityIdentity = (SecurityIdentity)optionalObjectFromElement(e, SECURITY_IDENTITY, new SecurityIdentity());
        resourceReferences = optionalObjectsFromElement(e, RESOURCE_REF, new ResourceReference());
        resourceEnvReferences = optionalObjectsFromElement(e, RESOURCE_ENV_REF, new ResourceEnvReference());
        local = optionalStringFromElement(e, LOCAL);
        remote = optionalStringFromElement(e, REMOTE);
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public abstract Element toElement(Document doc);
}