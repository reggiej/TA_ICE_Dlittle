// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 * Represents ejb-ref element tag
 */
public class EjbReference extends NameAndDescription {
    String refType;// Required
    String home;// Required
    String remote;// Required
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
     * @return String the home String
     */
    public String getHome() {
        return home;
    }

    /**
     * @param h the home String
     */
    public void setHome(String h) {
        home = h;
    }

    /**
     * @return String the remote String
     */
    public String getRemote() {
        return remote;
    }

    /**
     * @param rem the remote String
     */
    public void setRemote(String rem) {
        remote = rem;
    }

    /**
      * Load the data for this instance from the specified element.
      * @param e the DOM element
      */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);// description and name are loaded from super
        refType = stringFromElement(e, EJB_REF_TYPE);
        home = stringFromElement(e, HOME);
        remote = stringFromElement(e, REMOTE);
        ejbLink = optionalStringFromElement(e, EJB_LINK);
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(EJB_REF);
        inheritedFields(doc, e);

        addText(doc, e, nameTag(), getName());
        addText(doc, e, EJB_REF_TYPE, refType);
        addText(doc, e, HOME, home);
        addText(doc, e, REMOTE, remote);
        optionallyAddText(doc, e, EJB_LINK, ejbLink);
        return e;
    }
}