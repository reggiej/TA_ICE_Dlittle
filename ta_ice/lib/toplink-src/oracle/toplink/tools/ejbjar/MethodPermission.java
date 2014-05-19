// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 */
public class MethodPermission extends Description {
    Vector roleNames;// either unchecked or 1 or more roleNames String objects

    //OR
    String unchecked = FALSE_VALUE;// either unchecked or 1 or more roleNames String objects
    Vector methods;// 1 or more Method objects

    /**
     * @return Vector the collection of Method objects
     */
    public Vector getMethods() {
        return methods;
    }

    /**
     * @return Vector the collection of role name String objects
     */
    public Vector getRoleNames() {
        return roleNames;
    }

    /**
     * @return unchecked element
     */
    public String hasUnchecked() {
        return unchecked;
    }

    /**
     * @param flag unchecked element
     */
    public void setUnchecked(String flag) {
        unchecked = flag;
    }

    /**
     * @param v the collection of Method objects
     */
    public void setMethods(Vector v) {
        methods = v;
    }

    /**
     * @param v the collection of role name String objects
     */
    public void setRoleNames(Vector v) {
        roleNames = v;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        // either unchecked or 1 or more roleNames String objects
        roleNames = optionalStringsFromElement(e, ROLE_NAME);
        if (null == roleNames) {
            // check whether there is <unchecked> element
            String checkedElement = optionalStringFromElement(e, UNCHECKED);

            // make sure the above returns non null if the element checked is empty.
            if (checkedElement != null) {
                unchecked = TRUE_VALUE;
            }
        }
        methods = objectsFromElement(e, METHOD, new Method());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(METHOD_PERMISSION);
        inheritedFields(doc, e);
        if (null != roleNames) {
            addTextCollection(doc, e, ROLE_NAME, roleNames);
        } else {
            // unchecked is an EMPTY element.
            addText(doc, e, UNCHECKED, "");// NOTE ""
        }
        addCollection(doc, e, methods);
        return e;
    }
}