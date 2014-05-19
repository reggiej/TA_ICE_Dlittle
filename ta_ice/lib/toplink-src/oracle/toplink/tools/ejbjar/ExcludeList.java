// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 */
public class ExcludeList extends Description {
    Vector methods;// 1 or more Method objects

    /**
     * @return Vector the collection of Method objects
     */
    public Vector getMethods() {
        return methods;
    }

    /**
     * @param Vector the collection of Method objects
     */
    public void setMethods(Vector v) {
        methods = v;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        methods = objectsFromElement(e, METHOD, new Method());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(EXCLUDE_LIST);
        inheritedFields(doc, e);

        addCollection(doc, e, methods);
        return e;
    }
}