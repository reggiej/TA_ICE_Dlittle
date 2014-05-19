// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 */
public class ResourceEnvReference extends NameAndDescription {
    String refType;// Required

    /**
     *
     */
    public ResourceEnvReference() {
    }

    /**
     *
     * @return String the reference type String
     */
    public String getRefType() {
        return refType;
    }

    /**
     * Load the data for this instance from the specified element.
     *
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        refType = stringFromElement(e, RESOURCE_ENV_REF_TYPE);
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return RESOURCE_ENV_REF_NAME;
    }

    /**
     *
     * @param refTyp the reference type String
     */
    public void setRefType(String refTyp) {
        refType = refTyp;
    }

    /**
     * Return the data from this instance as a DOM element.
     *
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RESOURCE_ENV_REF);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        addText(doc, e, RESOURCE_ENV_REF_TYPE, refType);
        return e;
    }
}