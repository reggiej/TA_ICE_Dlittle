// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class RunAs extends NameAndDescription {

    /**
      *@return rolename
      */
    public String getRoleName() {
        return getName();
    }

    /**
      *@param rolename
      */
    public void setRoleName(String roleName) {
        setName(roleName);
    }

    /**
      * Return the specific element tag for the name of this object
      */
    protected String nameTag() {
        return ROLE_NAME;
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RUN_AS);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        return e;
    }
}