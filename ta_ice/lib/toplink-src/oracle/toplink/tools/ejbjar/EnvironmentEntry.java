// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 * Represents env-entry tag object.
 */
public class EnvironmentEntry extends NameAndDescription {
    String entryType;// Required
    String entryValue;// Optional

    /**
     * @return String the entry type String
     */
    public String getEntryType() {
        return entryType;
    }

    /**
     * @return String the entry value String, or null if not set
     */
    public String getEntryValue() {
        return entryValue;
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return ENV_ENTRY_NAME;
    }

    /**
     * @param entryTyp the entry type String
     */
    public void setEntryType(String entryTyp) {
        entryType = entryTyp;
    }

    /**
     * @param entryVal the entry value String
     */
    public void setEntryValue(String entryVal) {
        entryValue = entryVal;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        entryType = stringFromElement(e, ENV_ENTRY_TYPE);
        entryValue = optionalStringFromElement(e, ENV_ENTRY_VALUE);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(ENV_ENTRY);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());

        addText(doc, e, ENV_ENTRY_TYPE, entryType);
        optionallyAddText(doc, e, ENV_ENTRY_VALUE, entryValue);
        return e;
    }
}