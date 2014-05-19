// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 * Since a number of elements have optional description
 * sub-elements this abstract class can be subclassed
 * to inherit the description element support.
 */
public abstract class Description extends DomObject {
    String description;// Optional

    /**
     * @return String the description String or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Insert elements for the fields that subclasses inherit
     * from this object.
     *
     * Note: This means a class needs to know if it has subclasses.
     *
     * @param doc a Document instance used to create elements
     * @param e the parent Element in which to insert the field elements
     */
    public void inheritedFields(Document doc, Element e) {
        optionallyAddText(doc, e, DESCRIPTION, description);
    }

    /**
     * Load the data for this instance from the specified element.
     *
     * @param e the *parent* DOM element that may contain a description
     */
    public void loadFromElement(Element e) {
        description = optionalStringFromElement(e, DESCRIPTION);
    }

    /**
     *
     * @param desc description String
     */
    public void setDescription(String desc) {
        description = desc;
    }
}