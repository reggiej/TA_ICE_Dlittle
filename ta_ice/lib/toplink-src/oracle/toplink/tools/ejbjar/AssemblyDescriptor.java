// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 */
public class AssemblyDescriptor extends DomObject {
    Vector securityRoles;// 0 or more SecurityRole objects
    Vector methodPermissions;// 0 or more MethodPermission objects
    Vector containerTransactions;// 0 or more ContainerTransaction objects
    ExcludeList excludeList;// Optional

    /**
     * @return ExcludeList
     */
    public ExcludeList getExcludeList() {
        return excludeList;
    }

    /**
     * @param excludeList
     */
    public void getExcludeList(ExcludeList excludeList) {
        this.excludeList = excludeList;
    }

    /**
     * @return Vector the collection of ContainerTransaction, or null if not set
     */
    public Vector getContainerTransactions() {
        return containerTransactions;
    }

    /**
     * @return Vector the collection of MethodPermission, or null if not set
     */
    public Vector getMethodPermissions() {
        return methodPermissions;
    }

    /**
     * @return Vector the collection of SecurityRole, or null if not set
     */
    public Vector getSecurityRoles() {
        return securityRoles;
    }

    /**
     * @param containerTxn the collection of ContainerTransaction
     */
    public void setContainerTransactions(Vector containerTxn) {
        containerTransactions = containerTxn;
    }

    /**
     * @param permissions the collection of MethodPermission
     */
    public void setMethodPermissions(Vector permissions) {
        methodPermissions = permissions;
    }

    /**
     * @param roles the collection of SecurityRole
     */
    public void setSecurityRoles(Vector roles) {
        securityRoles = roles;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        // super.loadFromElement(e); super class has an abstract method
        securityRoles = optionalObjectsFromElement(e, SECURITY_ROLE, new SecurityRole());
        methodPermissions = optionalObjectsFromElement(e, METHOD_PERMISSION, new MethodPermission());
        containerTransactions = optionalObjectsFromElement(e, CONTAINER_TRANSACTION, new ContainerTransaction());
        excludeList = (ExcludeList)optionalObjectFromElement(e, EXCLUDE_LIST, new ExcludeList());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(ASSEMBLY_DESCRIPTOR);
        optionallyAddCollection(doc, e, securityRoles);
        optionallyAddCollection(doc, e, methodPermissions);
        optionallyAddCollection(doc, e, containerTransactions);
        if (excludeList != null) {
            e.appendChild(excludeList.toElement(doc));
        }
        return e;
    }
}