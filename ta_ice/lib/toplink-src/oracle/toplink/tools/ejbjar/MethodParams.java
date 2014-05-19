// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Vector;

/**
 * INTERNAL:
 * Represents method-params tag object
 */
public class MethodParams extends DomObject {
    Vector params;// 0 or more String objects

    /**
     * Default constructor
     */
    public MethodParams() {
        setParams(new Vector());
    }

    public void addParam(String paramName) {
        if (params == null) {
            params = new Vector();
        }
        params.add(paramName);
    }

    /**
     * @return Vector the collection of param String, or null if not set
     */
    public Vector getParams() {
        return params;
    }

    /**
     * @param parms the collection of param String
     */
    public void setParams(Vector parms) {
        params = parms;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        params = optionalStringsFromElement(e, METHOD_PARAM);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(METHOD_PARAMS);
        optionallyAddTextCollection(doc, e, METHOD_PARAM, params);
        return e;
    }
}