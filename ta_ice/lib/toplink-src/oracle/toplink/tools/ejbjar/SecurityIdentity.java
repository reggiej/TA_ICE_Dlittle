// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class SecurityIdentity extends Description {
    // Exactly one of the following two is required.
    RunAs runAs;

    // OR
    boolean useCallerIdentity;

    public SecurityIdentity() {
        useCallerIdentity = false;
    }

    /**
      * @return RunAs the identity object to run under, or null if not used
      */
    public RunAs getRunAs() {
        return runAs;
    }

    /**
     * @return boolean true if caller identity should be used, false if not [specified]
     */
    public boolean getUseCallerIdentity() {
        return useCallerIdentity;
    }

    /**
     * @param runAsIdent the RunAs object
     */
    public void setRunAs(RunAs runAsIdent) {
        runAs = runAsIdent;
    }

    /**
     * @param flag true if caller identity should be used, false if not [specified]
     */
    public void setUseCallerIdentity(boolean flag) {
        useCallerIdentity = flag;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        // This option gets set simply by defining it in XML (no value req'd)
        useCallerIdentity = (getFirstElementByTagName(USE_CALLER_IDENTITY, e) != null);
        if (!getUseCallerIdentity()) {
            runAs = (RunAs)objectFromElement(e, RUN_AS, new RunAs());
        }
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RELATIONSHIP_ROLE_SOURCE);
        inheritedFields(doc, e);
        // If this is set then we need only create an empty element.
        if (useCallerIdentity) {
            e.appendChild(doc.createElement(USE_CALLER_IDENTITY));
        } else {
            e.appendChild(runAs.toElement(doc));
        }
        return e;
    }
}