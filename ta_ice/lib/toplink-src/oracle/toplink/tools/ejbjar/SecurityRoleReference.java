// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 */
public class SecurityRoleReference extends NameAndDescription {
    String roleLink;// Optional

    /**
     *@return role-name
     */
    public String getRoleName() {
        return getName();
    }

    /**
     *@param role-name
     */
    protected void setRoleName(String roleName) {
        setName(roleName);
    }

    /**
     * @return String the role link String, or null if not specified
     */
    public String getRoleLink() {
        return roleLink;
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return ROLE_NAME;
    }

    /**
     * @param roleLnk the role link String
     */
    public void setRoleLink(String roleLnk) {
        roleLink = roleLnk;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        roleLink = optionalStringFromElement(e, ROLE_LINK);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(SECURITY_ROLE_REF);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        optionallyAddText(doc, e, ROLE_LINK, roleLink);
        return e;
    }
}