// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 * Represents ejb-local-ref element tag
 */
public class EjbLocalReference extends NameAndDescription {
    String refType;// Required
    String localHome;// Required
    String local;// Required
    String ejbLink;// Optional

    /**
      * Return the specific element tag for the name of this object
      */
    protected String nameTag() {
        return EJB_REF_NAME;
    }

    /**
      * @return String the reference type String
      */
    public String getRefType() {
        return refType;
    }

    /**
     * @param refTyp the reference type String
     */
    public void setRefType(String refTyp) {
        refType = refTyp;
    }

    /**
     * @return String the ejbLink String, or null if not set
     */
    public String getEjbLink() {
        return ejbLink;
    }

    /**
     * @param link the ejbLink String
     */
    public void setEjbLink(String link) {
        ejbLink = link;
    }

    /**
     * @return String the localHome String
     */
    public String getLocalHome() {
        return localHome;
    }

    /**
     * @param h the localHome String
     */
    public void setLocalHome(String h) {
        localHome = h;
    }

    /**
     * @return String the local String
     */
    public String getLocal() {
        return local;
    }

    /**
     * @param rem the local String
     */
    public void setLocal(String rem) {
        local = rem;
    }

    /**
      * Load the data for this instance from the specified element.
      * @param e the DOM element
      */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);// description and name are loaded from super
        refType = stringFromElement(e, EJB_REF_TYPE);
        localHome = stringFromElement(e, LOCAL_HOME);
        local = stringFromElement(e, LOCAL);
        ejbLink = optionalStringFromElement(e, EJB_LINK);
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(EJB_LOCAL_REF);
        inheritedFields(doc, e);

        addText(doc, e, nameTag(), getName());
        addText(doc, e, EJB_REF_TYPE, refType);
        addText(doc, e, LOCAL_HOME, localHome);
        addText(doc, e, LOCAL, local);
        optionallyAddText(doc, e, EJB_LINK, ejbLink);
        return e;
    }
}