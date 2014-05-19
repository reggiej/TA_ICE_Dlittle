// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 */
public class ContainerTransaction extends Description {
    Vector methods;// 1 or more Method objects
    String transAttribute;// Required

    /**
     * @return Vector the collection of Method objects
     */
    public Vector getMethods() {
        return methods;
    }

    /**
     * @return String the transaction attribute String
     */
    public String getTransAttribute() {
        return transAttribute;
    }

    /**
     * @param Vector the collection of Method objects
     */
    public void setMethods(Vector v) {
        methods = v;
    }

    /**
     * @param txnAttr the transaction attribute String
     */
    public void setTransAttribute(String txnAttr) {
        transAttribute = txnAttr;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        methods = objectsFromElement(e, METHOD, new Method());
        transAttribute = stringFromElement(e, TRANS_ATTRIBUTE);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(CONTAINER_TRANSACTION);
        inheritedFields(doc, e);

        addCollection(doc, e, methods);
        addText(doc, e, TRANS_ATTRIBUTE, transAttribute);
        return e;
    }
}