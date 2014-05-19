// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 */
public class ResourceReference extends NameAndDescription {
    String resType;// Required
    String resAuth;// Required
    String resSharingScope;// Optional

    /**
     * @return String the resource auth String
     */
    public String getResAuth() {
        return resAuth;
    }

    /**
     * @return String the resource sharing scope String, or null if not set
     */
    public String getResSharingScope() {
        return resSharingScope;
    }

    /**
     * @return String the resource type String
     */
    public String getResType() {
        return resType;
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return RES_REF_NAME;
    }

    /**
     * @param rAuth the resource auth String
     */
    public void setResAuth(String rAuth) {
        resAuth = rAuth;
    }

    /**
     * @param shareScope the resource sharing scope String
     */
    public void setResSharingScope(String shareScope) {
        resSharingScope = shareScope;
    }

    /**
     * @param resTyp the resource type String
     */
    public void setResType(String resTyp) {
        resType = resTyp;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        resType = stringFromElement(e, RES_TYPE);
        resAuth = stringFromElement(e, RES_AUTH);
        resSharingScope = optionalStringFromElement(e, RES_SHARING_SCOPE);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RESOURCE_REF);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        addText(doc, e, RES_TYPE, resType);
        addText(doc, e, RES_AUTH, resAuth);
        optionallyAddText(doc, e, RES_SHARING_SCOPE, resSharingScope);
        return e;
    }
}