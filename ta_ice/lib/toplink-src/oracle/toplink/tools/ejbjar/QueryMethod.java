// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Vector;

/**
 * INTERNAL:
 */
public class QueryMethod extends DomObject {
    String methodName;// Required
    MethodParams methodParams;// Required

    /**
     * Default constructor
     */
    public QueryMethod() {
        methodName = "";
        methodParams = new MethodParams();
    }

    public void addParam(String paramName) {
        methodParams.addParam(paramName);
    }

    /**
     * @return String the method name String
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Vector the collection of param String
     */
    public Vector getParams() {
        return methodParams.getParams();
    }

    /**
     * @param methName the method name String
     */
    public void setMethodName(String methName) {
        methodName = methName;
    }

    /**
     * @param parms a collection of param Strings
     */
    public void setParams(Vector parms) {
        if (methodParams == null) {
            methodParams = new MethodParams();
        }
        methodParams.setParams(parms);
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        methodName = stringFromElement(e, METHOD_NAME);
        methodParams = (MethodParams)objectFromElement(e, METHOD_PARAMS, new MethodParams());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(QUERY_METHOD);
        addText(doc, e, METHOD_NAME, methodName);
        e.appendChild(methodParams.toElement(doc));
        return e;
    }
}