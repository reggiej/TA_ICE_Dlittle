// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 * Since a number of elements have optional description
 * sub-elements and required name sub-elements this abstract
 * class can be subclassed to inherit the name and description
 * element support.
 */
public abstract class NameAndDescription extends Description {
    String name;// Required

    /**
     * @return String the name String
     */
    public String getName() {
        return name;
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
        super.inheritedFields(doc, e);

        // had to do the following for only one class Dependent because the
        // order in which it builds the DOM according to the DTD.
        //	addText(doc, e, nameTag(), getName());   
    }

    /**
     * Load the data for this instance from the specified element.
     *
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        this.name = stringFromElement(e, nameTag());
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected abstract String nameTag();

    /**
     * @param nm the name String
     */
    public void setName(String nm) {
        name = nm;
    }
}