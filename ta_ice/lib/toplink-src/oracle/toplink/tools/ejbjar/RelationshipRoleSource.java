// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class RelationshipRoleSource extends Description {
    public String ejbName;// Required 

    public RelationshipRoleSource() {
        super();
        ejbName = "";
    }

    /**
      * @return String the ejb name, or null if not set
      */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Only one of ejbName, remoteEjbName, or dependentName may be set.
     * @param ejbName the ejb name
     */
    public void setEjbName(String newEjbName) {
        ejbName = newEjbName;
    }

    /**
     * The name attribute is required by the DTD but we do not
     * know, however, which of the possible ones that it is.
     * @param e the DOM element that may contain a name element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        String n = stringFromElement(e, EJB_NAME);

        if (n != null) {
            setEjbName(n);
        }
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RELATIONSHIP_ROLE_SOURCE);
        inheritedFields(doc, e);

        if (getEjbName() != null) {
            e.appendChild(createElement(doc, EJB_NAME, getEjbName()));
        }

        return e;
    }
}