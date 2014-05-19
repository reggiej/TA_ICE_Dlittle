// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 * Represents method tag object
 */
public class Method extends NameAndDescription {
    String ejbName;// Required
    String methodIntf;// Optional

    // method-name defined in super class //required
    MethodParams methodParams;// Optional

    /**
     * @return String the ejb name String
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * @return String the methodIntf String, or null if not set
     */
    public String getMethodIntf() {
        return methodIntf;
    }

    /**
     *
     * @return Vector the collection of param String, or null if not set
     */
    public Vector getParams() {
        return (methodParams == null) ? null : methodParams.getParams();
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return METHOD_NAME;
    }

    /**
     * @param ejbNm the ejb name String
     */
    public void setEjbName(String ejbNm) {
        ejbName = ejbNm;
    }

    /**
     * @param methIntf the methodIntf String
     */
    public void setMethodIntf(String methIntf) {
        methodIntf = methIntf;
    }

    /**
     *
     * @param v the collection of param String, or null if not set
     */
    public void setParams(Vector v) {
        if (methodParams == null) {
            methodParams = new MethodParams();
        }
        methodParams.setParams(v);
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        ejbName = stringFromElement(e, EJB_NAME);
        methodIntf = optionalStringFromElement(e, METHOD_INTF);
        methodParams = (MethodParams)optionalObjectFromElement(e, METHOD_PARAMS, new MethodParams());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(METHOD);
        inheritedFields(doc, e);
        addText(doc, e, EJB_NAME, ejbName);
        optionallyAddText(doc, e, METHOD_INTF, methodIntf);
        addText(doc, e, nameTag(), getName());
        if (methodParams != null) {
            e.appendChild(methodParams.toElement(doc));
        }
        return e;
    }
}